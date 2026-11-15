/*
 * useAnnouncementRead：公告已读状态的本地存储封装
 * - 游客没有服务端身份，已读只能记在 localStorage
 * - 登录用户除了写 localStorage，还需要额外调用 /announcement/read 写服务端记录
 * - 公告 id 在后端被序列化为字符串，统一按字符串存取，避免雪花 id 精度丢失
 * - readIds 是模块级单例，公告弹窗、公告中心、顶部角标共享同一份状态
 */
import { ref, type Ref } from 'vue';

const STORAGE_KEY = 'lilac-blog-read-announcement-ids';

// 本地最多保留的已读公告数，超出后丢弃最旧的，避免 localStorage 无限增长
const MAX_STORED_IDS = 200;

/**
 * 从 localStorage 读取已读公告 id 列表
 * 浏览器禁用存储或数据损坏时返回空数组，不能抛错影响公告弹窗
 *
 * @returns 已读公告 id 字符串数组
 */
function loadReadIds(): string[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.map((item) => String(item));
  } catch {
    return [];
  }
}

// 模块级共享状态：多次调用 useAnnouncementRead 拿到的是同一个 ref
const readIds = ref<string[]>(loadReadIds());

/**
 * 把内存中的已读列表写回 localStorage
 * 副作用：覆盖本地存储；写入失败（隐私模式/配额超限）静默忽略
 */
function persist() {
  try {
    const trimmed = readIds.value.slice(-MAX_STORED_IDS);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(trimmed));
  } catch {
    // 存储不可用时只影响下次访问的去重，不影响当前弹窗
  }
}

/**
 * 判断某条公告是否已经读过
 *
 * @param id 公告 id
 * @returns 已读返回 true
 */
function isRead(id?: number | string | null): boolean {
  if (id === undefined || id === null) return false;
  return readIds.value.includes(String(id));
}

/**
 * 批量写入本地已读记录，已存在的 id 不会重复写入
 *
 * @param ids 公告 id 列表
 */
function markLocalRead(ids: Array<number | string>) {
  if (!ids.length) return;
  const merged = new Set(readIds.value);
  ids.forEach((id) => merged.add(String(id)));
  readIds.value = Array.from(merged);
  persist();
}

export interface AnnouncementReadState {
  readIds: Ref<string[]>;
  isRead: (id?: number | string | null) => boolean;
  markLocalRead: (ids: Array<number | string>) => void;
}

/**
 * 获取公告已读状态
 * 返回值指向模块级单例，任何组件写入后其它组件都会同步更新
 *
 * @returns 已读 id 列表与读写方法
 */
export function useAnnouncementRead(): AnnouncementReadState {
  return { readIds, isRead, markLocalRead };
}
