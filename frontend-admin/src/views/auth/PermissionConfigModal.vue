<template>
  <a-modal v-model:open="visible" title="配置权限" width="800px" :confirm-loading="loading" @ok="handleOk"
    @cancel="handleCancel">
    <a-spin :spinning="loading">
      <a-transfer v-model:target-keys="targetKeys" :data-source="dataSource" :titles="['可用权限', '已分配权限']"
        :render="(item: TransferItem) => item.title" show-search :filter-option="filterOption" :locale="{
          itemUnit: '项',
          itemsUnit: '项',
          searchPlaceholder: '搜索权限',
          notFoundContent: '列表为空'
        }" />
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { getPermissionList } from '@/api/permissionController';
import { batchAddPermissions, batchRemovePermissions, getRolePermissions } from '@/api/roleController';

interface TransferItem {
  key: string;
  title: string;
  disabled?: boolean;
}

const props = defineProps<{
  open: boolean;
  roleId?: string;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'success'): void;
}>();

const visible = ref(false);
const loading = ref(false);
const dataSource = ref<TransferItem[]>([]);
const targetKeys = ref<string[]>([]);
const originalTargetKeys = ref<string[]>([]);

// 监听 open 变化，初始化数据
watch(
  () => props.open,
  (val) => {
    visible.value = val;
    if (val && props.roleId) {
      loadData();
    }
  }
);

// 监听 visible 变化，关闭时重置数据
watch(visible, (val) => {
  if (!val) {
    emit('update:open', false);
  }
});

// 加载所有权限和角色已分配权限
const loadData = async () => {
  if (!props.roleId) return;
  loading.value = true;
  try {
    const [allPermissionsRes, rolePermissionsRes] = await Promise.all([
      getPermissionList({ current: 1, pageSize: 1000 }),
      getRolePermissions({ roleId: Number(props.roleId) })
    ]);

    const allPermissions = allPermissionsRes.data?.data?.records ?? [];
    const rolePermissions = rolePermissionsRes.data?.data ?? [];

    dataSource.value = allPermissions.map((p: API.Permission) => ({
      key: String(p.id),
      title: `${p.name} (${p.permissionKey})`
    }));

    const rolePermissionIds = rolePermissions.map((p: API.Permission) => String(p.id));
    targetKeys.value = [...rolePermissionIds];
    originalTargetKeys.value = [...rolePermissionIds];
  } catch (error) {
    message.error('加载数据失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 搜索权限
const filterOption = (inputValue: string, item: TransferItem) => {
  return item.title.toLowerCase().includes(inputValue.toLowerCase());
};

// 提交
const handleOk = async () => {
  if (!props.roleId) return;

  const currentTargetKeys = new Set(targetKeys.value);
  const originalKeys = new Set(originalTargetKeys.value);

  const toAdd = targetKeys.value.filter(key => !originalKeys.has(key));
  const toRemove = originalTargetKeys.value.filter(key => !currentTargetKeys.has(key));

  if (toAdd.length === 0 && toRemove.length === 0) {
    message.info('未做任何修改');
    handleCancel();
    return;
  }

  loading.value = true;
  try {
    const promises: Promise<any>[] = [];

    if (toAdd.length > 0) {
      promises.push(
        batchAddPermissions({
          roleId: Number(props.roleId),
          permissionIds: toAdd.map(id => Number(id))
        })
      );
    }

    if (toRemove.length > 0) {
      promises.push(
        batchRemovePermissions({
          roleId: Number(props.roleId),
          permissionIds: toRemove.map(id => Number(id))
        })
      );
    }

    await Promise.all(promises);
    message.success('权限配置成功');
    emit('success');
    handleCancel();
  } catch (error) {
    message.error('权限配置失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  visible.value = false;
  dataSource.value = [];
  targetKeys.value = [];
  originalTargetKeys.value = [];
};
</script>
