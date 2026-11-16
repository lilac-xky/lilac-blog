/*
 * useAnnouncementCenter：公告弹窗的全局开关与展示模式
 * - 顶部导航与布局层弹窗组件都需要控制同一个弹窗，因此状态放在模块级单例
 * - mode = 'pending'：首次进入自动弹出的未读模式，读完自动关闭
 * - mode = 'all'：用户手动从导航打开的公告中心，展示全部已发布公告，可随时关闭
 *
 * 关于 autoShown：
 * 只做「内存单例」，刻意不写 sessionStorage/localStorage。
 * 原因：公告的语义是「每次打开站点都应检查一次未读」，一次会话只弹一次会让用户
 * 刷新页面后再也看不到新公告。登录页与主站是两个不同布局，BasicLayout 只在
 * 进入主站时挂载一次，模块级单例已足够跨这个边界去重。
 */
import { ref, type Ref } from 'vue';

export type AnnouncementMode = 'pending' | 'all';

// 弹窗开关：由导航按钮或自动弹窗流程置为 true
const visible = ref(false);

// 当前展示模式，默认未读模式
const mode = ref<AnnouncementMode>('pending');

// 是否已经执行过首次自动弹出，避免同一布局生命周期内重复自动弹
const autoShown = ref(false);

export interface AnnouncementCenterState {
  visible: Ref<boolean>;
  mode: Ref<AnnouncementMode>;
  autoShown: Ref<boolean>;
  openPending: () => void;
  openAll: () => void;
  close: () => void;
}

/**
 * 获取公告弹窗控制器
 * 返回值指向模块级单例，导航与弹窗组件共享同一份开关状态
 *
 * @returns 弹窗开关、模式与打开/关闭方法
 */
export function useAnnouncementCenter(): AnnouncementCenterState {
  /**
   * 以未读模式打开（首次进入自动调用）
   * 副作用：把 visible 置为 true，弹窗内部会自行拉取待读列表
   */
  function openPending() {
    mode.value = 'pending';
    visible.value = true;
  }

  /**
   * 以公告中心模式打开（用户从导航手动进入）
   * 副作用：把 visible 置为 true，弹窗内部会拉取全部已发布公告
   */
  function openAll() {
    mode.value = 'all';
    visible.value = true;
  }

  /**
   * 关闭弹窗
   */
  function close() {
    visible.value = false;
  }

  return { visible, mode, autoShown, openPending, openAll, close };
}
