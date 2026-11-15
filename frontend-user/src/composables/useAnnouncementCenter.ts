/*
 * useAnnouncementCenter：公告弹窗的全局开关与展示模式
 * - 顶部导航与布局层弹窗组件都需要控制同一个弹窗，因此状态放在模块级单例
 * - mode = 'pending'：首次进入自动弹出的未读模式，读完自动关闭
 * - mode = 'all'：用户手动从导航打开的公告中心，展示全部已发布公告，可随时关闭
 */
import { ref, type Ref } from 'vue';

export type AnnouncementMode = 'pending' | 'all';

// 弹窗开关：由导航按钮或自动弹窗流程置为 true
const visible = ref(false);

// 当前展示模式，默认未读模式
const mode = ref<AnnouncementMode>('pending');

// 是否已经执行过首次自动弹出，避免用户手动关闭后又被自动逻辑重新打开
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
