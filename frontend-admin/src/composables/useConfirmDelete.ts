// useConfirmDelete：列表行删除通用确认逻辑
// 统一弹「确认 - 调接口 - 提示 - 刷新」流程，免去每个管理页重复写 Modal.confirm
import { Modal, message } from 'ant-design-vue';

export interface ConfirmDeleteOptions<TRecord> {
  titleOf: (record: TRecord) => string;
  contentOf?: (record: TRecord) => string;
  deleteApi: (params: { id: any }) => Promise<any>;
  onSuccess?: () => void;
  successMessage?: string;
}

export function useConfirmDelete<TRecord extends Record<string, any>>(
  options: ConfirmDeleteOptions<TRecord>,
) {
  const { titleOf, contentOf, deleteApi, onSuccess, successMessage = '删除成功' } = options;

  function confirmDelete(record: TRecord) {
    const modal = Modal.confirm({
      title: titleOf(record),
      content: contentOf?.(record),
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      async onOk() {
        try {
          const res = await deleteApi({ id: record.id });
          if (res?.data?.data) {
            message.success(successMessage);
            onSuccess?.();
          }
        } catch {
          modal.destroy();
        }
      },
    });
  }

  return { confirmDelete };
}
