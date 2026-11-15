// useHoverCollapseSelect：多选下拉「鼠标移出即收起」通用逻辑
// antd 的多选 Select 选完一项不会收起，方便连续勾选；但用户不想要「选完就收起」，
// 也不想要「一直开着」，而是「鼠标离开这块区域再收起」。
// 这里统一封装：受控 open + 给触发器与弹出面板绑定 mouseenter/mouseleave。
import { onBeforeUnmount, ref, type Ref } from 'vue';

export interface HoverCollapseSelectOptions {
  /** 收起前的延迟（毫秒）：给用户从输入框移到下拉面板的移动时间 */
  closeDelay?: number;
}

export function useHoverCollapseSelect(options: HoverCollapseSelectOptions = {}) {
  const { closeDelay = 180 } = options;

  // 下拉展开状态，直接绑定到 a-select 的 :open
  const open = ref(false);

  // 收起定时器：在输入框与面板之间移动时会短暂离开，用延迟避免闪断
  let closeTimer: ReturnType<typeof setTimeout> | null = null;

  /**
   * 清理待执行的收起定时器
   * 副作用：清除 setTimeout，不影响 open 状态
   */
  function clearTimer() {
    if (closeTimer) {
      clearTimeout(closeTimer);
      closeTimer = null;
    }
  }

  /**
   * 取消收起：鼠标进到触发器或面板上
   * 副作用：清除定时器，保持展开
   */
  function cancelClose() {
    clearTimer();
  }

  /**
   * 触发收起（带延迟）
   * 副作用：closeDelay 后把 open 置为 false；期间若鼠标移回来则被 cancelClose 取消
   */
  function scheduleClose() {
    clearTimer();
    closeTimer = setTimeout(() => {
      open.value = false;
      closeTimer = null;
    }, closeDelay);
  }

  /**
   * 处理 a-select 的展开状态变化
   * 输入：antd 期望的新状态
   * 副作用：写回 open；展开时取消在途的收起，避免刚点开就被收起
   *
   * @param visible antd 期望的展开状态
   */
  function onVisibleChange(visible: boolean) {
    if (visible) {
      clearTimer();
    }
    open.value = visible;
  }

  /**
   * 手动收起并清理状态，供提交成功后重置表单等场景调用
   * 副作用：清除定时器并把 open 置为 false
   */
  function close() {
    clearTimer();
    open.value = false;
  }

  onBeforeUnmount(clearTimer);

  return {
    open: open as Ref<boolean>,
    onVisibleChange,
    cancelClose,
    scheduleClose,
    close,
  };
}
