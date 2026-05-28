<template>
  <div class="crud-shell">
    <!-- 搜索面板 -->
    <div class="panel search-panel">
      <div class="panel-header tight">
        <div class="panel-title small">{{ searchTitle }}</div>
        <slot name="toolbar">
          <a-button v-if="showAdd" type="primary" @click="$emit('add')">
            <template #icon>
              <PlusOutlined />
            </template>
            {{ addButtonText }}
          </a-button>
        </slot>
      </div>
      <SearchForm :model="queryForm" :fields="searchSchema" :loading="loading" @search="$emit('search')"
        @reset="$emit('reset')">
        <template v-for="field in searchSchema" :key="`search-${field.name}`" #[`field-${field.name}`]="slotProps">
          <slot :name="`search-${field.name}`" v-bind="slotProps" />
        </template>
      </SearchForm>
    </div>

    <!-- 表格面板 -->
    <div class="panel table-panel">
      <div class="panel-header tight">
        <div class="panel-title small">
          {{ title }}
          <span class="total-badge">共 {{ total }} {{ totalUnit }}</span>
        </div>
        <slot name="table-toolbar" />
      </div>

      <a-table :columns="columns" :data-source="tableData" :loading="loading" :pagination="false" :row-key="rowKey"
        :size="size" :scroll="scroll" :show-sorter-tooltip="false"
        @change="(p: any, f: any, s: any) => $emit('tableChange', p, f, s)">
        <template #bodyCell="{ column, record, index }">
          <!-- 完全替换操作列 -->
          <template v-if="(column as CrudColumn).key === 'action' && hasActionSlot">
            <slot name="action" :record="record" :index="index" />
          </template>

          <!-- 默认操作列 -->
          <template v-else-if="(column as CrudColumn).key === 'action'">
            <OperationCell :show-edit="showEditAction" :show-delete="showDeleteAction" @edit="$emit('edit', record)"
              @delete="$emit('delete', record)">
              <template v-if="hasExtraActionsSlot" #append>
                <slot name="extra-actions" :record="record" :index="index" />
              </template>
            </OperationCell>
          </template>

          <!-- 自定义列插槽兜底 -->
          <template v-else-if="hasColumnSlot((column as CrudColumn).key)">
            <slot :name="`column-${(column as CrudColumn).key as string}`" :record="record" :column="column"
              :index="index" />
          </template>

          <!-- renderer 内置渲染 -->
          <template v-else-if="(column as CrudColumn).renderer">
            <template v-if="(column as CrudColumn).renderer!.type === 'datetime'">
              {{ formatDateTime(getValue(record, column)) }}
            </template>
            <template v-else-if="(column as CrudColumn).renderer!.type === 'statusTag'">
              <StatusTag :value="getValue(record, column)" :map="((column as CrudColumn).renderer as any).map" />
            </template>
            <template v-else-if="(column as CrudColumn).renderer!.type === 'avatar'">
              <a-avatar :src="avatarSrc(getValue(record, column))"
                :size="((column as CrudColumn).renderer as any).size ?? 36">
                <template v-if="((column as CrudColumn).renderer as any).fallbackIcon !== false" #icon>
                  <UserOutlined />
                </template>
              </a-avatar>
            </template>
            <template v-else-if="(column as CrudColumn).renderer!.type === 'switch'">
              <a-switch :checked="((column as CrudColumn).renderer as any).getChecked(record)"
                :loading="switchLoadingFor(record, column)"
                :disabled="((column as CrudColumn).renderer as any).disabled?.(record) ?? false"
                :checked-children="((column as CrudColumn).renderer as any).checkedText ?? '正常'"
                :un-checked-children="((column as CrudColumn).renderer as any).uncheckedText ?? '异常'"
                @change="(v: boolean) => ((column as CrudColumn).renderer as any).onChange(record, v)" />
            </template>
          </template>
        </template>
      </a-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <a-pagination v-model:current="queryForm.current" v-model:page-size="queryForm.pageSize" :total="total"
          :page-size-options="['10', '20', '50']" show-quick-jumper show-size-changer
          :show-total="(t: number) => `共 ${t} 条`" @change="$emit('pageChange')"
          @show-size-change="$emit('pageChange')" />
      </div>
    </div>
  </div>
</template>

<!--
  TableCRUD：后台通用 CRUD 表格外壳
  组合「搜索面板 + 数据表格 + 分页」三段式布局，列表型管理页用此组件可避免重复 DOM。
  - 列定义中通过 renderer 字段声明内置渲染器（datetime/statusTag/avatar/switch），无需写自定义模板
  - 通过 column-${key} / action / extra-actions / search-${name} 插槽支持任意定制
-->
<script setup lang="ts">
import { computed, useSlots } from 'vue';
import { PlusOutlined, UserOutlined } from '@ant-design/icons-vue';
import SearchForm from './SearchForm.vue';
import OperationCell from './OperationCell.vue';
import StatusTag from './StatusTag.vue';
import type { SearchField, CrudColumn } from './types';
import { formatDateTime } from '@/utils/datetime';

const props = withDefaults(
  defineProps<{
    title: string;
    totalUnit: string;
    columns: CrudColumn[];
    tableData: any[];
    total: number;
    loading: boolean;
    queryForm: Record<string, any>;
    searchSchema: SearchField[];
    rowKey?: string;
    size?: 'small' | 'middle' | 'large';
    scroll?: { x?: number | string; y?: number | string };
    showAdd?: boolean;
    addButtonText?: string;
    showEditAction?: boolean;
    showDeleteAction?: boolean;
    switchLoadingId?: string | number | null;
    searchTitle?: string;
  }>(),
  {
    rowKey: 'id',
    size: 'middle',
    showAdd: true,
    addButtonText: '新增',
    showEditAction: true,
    showDeleteAction: true,
    switchLoadingId: null,
    searchTitle: '条件筛选',
  },
);

defineEmits<{
  search: [];
  reset: [];
  add: [];
  edit: [record: any];
  delete: [record: any];
  tableChange: [pagination: any, filters: any, sorter: any];
  pageChange: [];
}>();

const slots = useSlots();
const hasActionSlot = computed(() => !!slots.action);
const hasExtraActionsSlot = computed(() => !!slots['extra-actions']);

function hasColumnSlot(key: any) {
  return key && !!slots[`column-${String(key)}`];
}

function getValue(record: any, column: any) {
  const key = (column.dataIndex ?? column.key) as string;
  return record[key];
}

// 阿里云 OSS 图片附加缩放与 webp 参数，节省流量；非 OSS 链接原样返回
function avatarSrc(url?: string) {
  if (!url) return undefined;
  if (!url.includes('.aliyuncs.com')) return url;
  if (url.includes('x-oss-process')) return url;
  return url + '?x-oss-process=image/resize,w_200/format,webp';
}

// 只让发起切换的那一行 switch 进入 loading 态，其它行不受影响
function switchLoadingFor(record: any, column: any) {
  const renderer = (column as CrudColumn).renderer;
  if (renderer?.type !== 'switch') return false;
  const key = (renderer as any).loadingKey ?? 'id';
  return props.switchLoadingId !== null && record[key] === props.switchLoadingId;
}
</script>

<style scoped>
.crud-shell {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-panel,
.table-panel {
  flex-shrink: 0;
}
</style>
