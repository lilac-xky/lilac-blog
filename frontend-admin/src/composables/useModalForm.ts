// useModalForm：弹窗式新增/编辑表单通用状态机
// 维护 visible / loading / mode / formData，并按 mode 路由到 addApi 或 updateApi
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';

export type ModalMode = 'add' | 'edit';

export interface ModalFormOptions<TForm extends Record<string, any>, TRecord = any> {
  defaultForm: () => TForm;
  rules?: Record<string, Rule[]>;
  addApi?: (form: TForm) => Promise<any>;
  updateApi?: (form: TForm) => Promise<any>;
  pickEditForm?: (record: TRecord) => Partial<TForm>;
  onSuccess?: () => void;
  successMessage?: { add?: string; edit?: string };
}

export function useModalForm<TForm extends Record<string, any>, TRecord = any>(
  options: ModalFormOptions<TForm, TRecord>,
) {
  const {
    defaultForm,
    rules,
    addApi,
    updateApi,
    pickEditForm,
    onSuccess,
    successMessage = { add: '新增成功', edit: '更新成功' },
  } = options;

  const visible = ref(false);
  const loading = ref(false);
  const mode = ref<ModalMode>('add');
  const formData = reactive<TForm>(defaultForm());

  // 重置时先清空再赋值，避免 reactive 对象残留上次编辑遗留字段
  function resetForm() {
    const fresh = defaultForm();
    Object.keys(formData).forEach((k) => {
      delete (formData as any)[k];
    });
    Object.assign(formData, fresh);
  }

  function openAdd() {
    mode.value = 'add';
    resetForm();
    visible.value = true;
  }

  // 打开编辑：用 pickEditForm 从表格行抽出表单需要的字段，避免无关字段污染表单
  function openEdit(record: TRecord) {
    mode.value = 'edit';
    resetForm();
    const picked = pickEditForm ? pickEditForm(record) : (record as unknown as Partial<TForm>);
    Object.assign(formData, picked);
    visible.value = true;
  }

  async function handleOk() {
    loading.value = true;
    try {
      const api = mode.value === 'add' ? addApi : updateApi;
      if (!api) return;
      const res = await api({ ...formData } as TForm);
      if (res?.data?.data) {
        message.success(
          mode.value === 'add' ? (successMessage.add ?? '新增成功') : (successMessage.edit ?? '更新成功'),
        );
        visible.value = false;
        onSuccess?.();
      }
    } finally {
      loading.value = false;
    }
  }

  function handleCancel() {
    // 由 FormModal 内部 resetFields
  }

  return {
    visible,
    loading,
    mode,
    formData,
    rules,
    openAdd,
    openEdit,
    handleOk,
    handleCancel,
  };
}
