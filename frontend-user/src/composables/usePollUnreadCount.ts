// usePollUnreadCount：未读消息计数轮询
// 触发刷新的来源有三个：定时轮询、路由切换、登录态变化（登录/登出）
// silentError: true 避免轮询失败弹错提示骚扰用户
import { onMounted, onUnmounted, ref, watch, type Ref } from 'vue';
import { useRoute } from 'vue-router';
import { getUnreadCount } from '@/api/messageController';

export interface PollUnreadOptions {
  isLoggedIn: Ref<boolean>;
  intervalMs?: number;
}

export function usePollUnreadCount({ isLoggedIn, intervalMs = 60_000 }: PollUnreadOptions) {
  const unreadCount = ref(0);
  let timer: ReturnType<typeof setInterval> | null = null;
  const route = useRoute();

  // 未登录直接清零，避免重复请求接口被后端拦截
  async function refresh() {
    if (!isLoggedIn.value) {
      unreadCount.value = 0;
      return;
    }
    try {
      const res = await getUnreadCount({ silentError: true });
      unreadCount.value = Number(res.data?.data ?? 0);
    } catch {
      // ignore
    }
  }

  function stop() {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  }

  onMounted(() => {
    refresh();
    timer = setInterval(refresh, intervalMs);
  });

  onUnmounted(stop);

  watch(() => route.path, () => {
    refresh();
  });

  watch(isLoggedIn, () => {
    refresh();
  });

  return { unreadCount, refresh, stop };
}
