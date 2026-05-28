// useListPagination：前台「Tab 切换 + 分页」列表通用 hook
// 用于「我的文章」「消息中心」等需要按状态/类型分组的列表
import { ref, type Ref } from 'vue';

export interface ListPaginationOptions<TRecord, TQuery extends Record<string, any>> {
  pageSize?: number;
  initialTab?: string;
  buildQuery: (ctx: { tab: string; current: number; pageSize: number }) => TQuery;
  fetcher: (query: TQuery) => Promise<{ records: TRecord[]; total: number }>;
  onAfterFetch?: () => void;
}

export function useListPagination<TRecord, TQuery extends Record<string, any>>(
  options: ListPaginationOptions<TRecord, TQuery>,
) {
  const { pageSize: defaultPageSize = 15, initialTab = 'all', buildQuery, fetcher, onAfterFetch } = options;

  const records: Ref<TRecord[]> = ref([]) as Ref<TRecord[]>;
  const total = ref(0);
  const current = ref(1);
  const pageSize = ref(defaultPageSize);
  const loading = ref(false);
  const activeTab = ref<string>(initialTab);

  async function fetchList() {
    loading.value = true;
    try {
      const query = buildQuery({ tab: activeTab.value, current: current.value, pageSize: pageSize.value });
      const { records: rs, total: t } = await fetcher(query);
      records.value = rs ?? [];
      total.value = t ?? 0;
    } finally {
      loading.value = false;
    }
    onAfterFetch?.();
  }

  // 切换 Tab 需要回到第一页，否则用户体验上「跨 Tab 但停留在原页码」很奇怪
  function onTabChange() {
    current.value = 1;
    fetchList();
  }

  return {
    records,
    total,
    current,
    pageSize,
    loading,
    activeTab,
    fetchList,
    onTabChange,
  };
}
