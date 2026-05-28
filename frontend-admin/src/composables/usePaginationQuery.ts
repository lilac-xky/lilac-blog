// usePaginationQuery：列表分页 + 排序 + 搜索 + 重置统一封装
// 配合 TableCRUD 使用，返回值可直接 v-bind 到组件 props/事件
import { reactive, ref, type Ref, type UnwrapNestedRefs } from 'vue';
import type { SortOrder } from '@/components/crud/types';

export interface PaginationQueryOptions<TQuery extends Record<string, any>, TRecord> {
  initialQuery: TQuery;
  fetcher: (query: TQuery) => Promise<{ records: TRecord[]; total: number }>;
  sortColumnKey?: string;
  transformQuery?: (query: TQuery) => any;
}

export function usePaginationQuery<TQuery extends Record<string, any>, TRecord>(
  options: PaginationQueryOptions<TQuery, TRecord>,
) {
  const { initialQuery, fetcher, sortColumnKey = 'createTime', transformQuery } = options;

  // 深拷贝快照用于「重置」回到初始查询条件；不能直接持有 initialQuery 引用，否则会被后续修改污染
  const snapshot = JSON.parse(JSON.stringify(initialQuery)) as TQuery;

  const loading = ref(false);
  const total = ref(0);
  const tableData: Ref<TRecord[]> = ref([]) as Ref<TRecord[]>;
  const queryForm = reactive({ ...initialQuery }) as UnwrapNestedRefs<TQuery>;
  const sortOrder = ref<SortOrder>((initialQuery.sortOrder as SortOrder) ?? 'descend');

  async function fetchData() {
    loading.value = true;
    try {
      const params = transformQuery ? transformQuery(queryForm as TQuery) : { ...queryForm };
      const { records, total: t } = await fetcher(params);
      tableData.value = records ?? [];
      total.value = (t as unknown as number) ?? 0;
    } catch {
      // 由请求拦截器统一提示
    } finally {
      loading.value = false;
    }
  }

  function handleSearch() {
    (queryForm as any).current = 1;
    fetchData();
  }

  function handleReset() {
    Object.keys(queryForm).forEach((k) => {
      (queryForm as any)[k] = (snapshot as any)[k];
    });
    sortOrder.value = (snapshot.sortOrder as SortOrder) ?? 'descend';
    (queryForm as any).sortOrder = sortOrder.value;
    fetchData();
  }

  // 点击表头排序：只支持单列（默认 createTime）切换升/降，回到第一页重新查询
  function handleTableChange(
    _pagination: unknown,
    _filters: unknown,
    sorter: { columnKey?: string; field?: string } | undefined,
  ) {
    if (!sorter) return;
    const key = sorter.columnKey ?? sorter.field;
    if (key !== sortColumnKey) return;
    sortOrder.value = sortOrder.value === 'descend' ? 'ascend' : 'descend';
    (queryForm as any).sortOrder = sortOrder.value;
    (queryForm as any).current = 1;
    fetchData();
  }

  return {
    loading,
    total,
    tableData,
    queryForm,
    sortOrder,
    fetchData,
    handleSearch,
    handleReset,
    handleTableChange,
  };
}
