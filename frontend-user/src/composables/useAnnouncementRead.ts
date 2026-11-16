/*
 * useAnnouncementRead：公告已读状态的本地存储封装
 * - 游客没有服务端身份，已读只能记在 localStorage
 * - 登录用户除了写 localStorage，还需要额外调用 /announcement/read 写服务端记录
 * - 公告 id 在后端被序列化为字符串，统一按字符串存取，避免雪花 id 精度丢失
 * - readIds 是模块级单例，公告弹窗、公告中心、顶部角标共享同一份状态
 *
 * 关于存储分桶：
 * 本地已读按「游客 / 各登录用户」分开存（storageKeyFor），否则会出现两个问题：
 *   1. A 用户在 A 设备读过的公告，B 用户登录后被当成已读而不再提示；
 *   2. 登录后把游客期间的本地已读同步给服务端时，会把上一个账号的记录也推过去。
 */
import { ref, type Ref } from 'vue';

// 游客与未分桶时期的旧键名，保持兼容以免老用户已读记录丢失
const STORAGE_KEY_ANONYMOUS = 'lilac-blog-read-announcement-ids';

// 登录用户键名前缀：实际键名为 prefix + userId
const STORAGE_KEY_USER_PREFIX = 'lilac-blog-read-announcement-ids:';

// 本地最多保留的已读公告数，超出后丢弃最旧的，避免 localStorage 无限增长
const MAX_STORED_IDS = 200;

// 当前生效的存储键，随登录态切换而变
let currentStorageKey = STORAGE_KEY_ANONYMOUS;

/**
 * 根据登录用户 id 计算存储键
 * 游客或 id 缺失时落到匿名桶，保证未登录也能正常去重
 *
 * @param userId 当前登录用户 id，游客传 undefined
 * @returns localStorage 键名
 */
function storageKeyFor(userId?: number | string): string {
  if (userId === undefined || userId === null || userId === '') {
    return STORAGE_KEY_ANONYMOUS;
  }
  return `${STORAGE_KEY_USER_PREFIX}${String(userId)}`;
}

/**
 * 从指定键读取已读公告 id 列表
 * 浏览器禁用存储或数据损坏时返回空数组，不能抛错影响公告弹窗
 *
 * @param key localStorage 键名
 * @returns 已读公告 id 字符串数组
 */
function loadReadIds(key: string): string[] {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.map((item) => String(item));
  } catch {
    return [];
  }
}

// 模块级共享状态：多次调用 useAnnouncementRead 拿到的是同一个 ref
const readIds = ref<string[]>(loadReadIds(currentStorageKey));

/**
 * 把内存中的已读列表写回当前存储桶
 * 副作用：覆盖本地存储；写入失败（隐私模式/配额超限）静默忽略
 */
function persist() {
  try {
    const trimmed = readIds.value.slice(-MAX_STORED_IDS);
    localStorage.setItem(currentStorageKey, JSON.stringify(trimmed));
  } catch {
    // 存储不可用时只影响下次访问的去重，不影响当前弹窗
  }
}

/**
 * 切换本地已读所属的存储桶（登录/登出时调用）
 * 副作用：改写 currentStorageKey 并从新桶重新载入 readIds；不写回旧桶
 *
 * 登录时会额外把游客桶里的记录并进来并落盘：
 * 用户未登录时关掉过公告，登录后不该再被同一则公告打扰，
 * 合并后这些 id 既能在本地去重，也会被上层同步到服务端。
 *
 * @param userId 当前登录用户 id，游客传 undefined
 */
function switchScope(userId?: number | string) {
  const nextKey = storageKeyFor(userId);
  if (nextKey === currentStorageKey) return;
  const wasAnonymous = currentStorageKey === STORAGE_KEY_ANONYMOUS;
  currentStorageKey = nextKey;
  const own = loadReadIds(nextKey);
  if (!wasAnonymous || userId === undefined || userId === null || userId === '') {
    readIds.value = own;
    return;
  }
  // 从游客切到登录：把游客期间的已读并进该用户自己的桶
  const merged = new Set([...own, ...loadReadIds(STORAGE_KEY_ANONYMOUS)]);
  readIds.value = Array.from(merged);
  persist();
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
  switchScope: (userId?: number | string) => void;
}

/**
 * 获取公告已读状态
 * 返回值指向模块级单例，任何组件写入后其它组件都会同步更新
 *
 * @returns 已读 id 列表与读写方法
 */
export function useAnnouncementRead(): AnnouncementReadState {
  return { readIds, isRead, markLocalRead, switchScope };
}
