// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 新增公告（默认草稿） POST /api/announcement/add */
export async function addAnnouncement(
  body: API.AnnouncementAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/announcement/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除公告 POST /api/announcement/delete */
export async function deleteAnnouncement(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/announcement/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 后台查询公告详情 GET /api/announcement/get */
export async function getAnnouncement(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAnnouncementParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultAnnouncement>("/api/announcement/get", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 后台分页查询公告 POST /api/announcement/list/page */
export async function listAnnouncementByPage(
  body: API.AnnouncementQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageAnnouncement>("/api/announcement/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询当前待读公告 已登录用户返回未读公告；游客返回已发布公告，由前端 localStorage 过滤已读 GET /api/announcement/pending */
export async function listPendingAnnouncements(options?: {
  [key: string]: any;
}) {
  return request<API.ResultListAnnouncementVO>("/api/announcement/pending", {
    method: "GET",
    ...(options || {}),
  });
}

/** 发布或下线公告 POST /api/announcement/publish */
export async function publishAnnouncement(
  body: API.AnnouncementPublishRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/announcement/publish", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询全部已发布公告（公告中心回看） 已登录用户填充 isRead；游客不填充，由前端 localStorage 判断 GET /api/announcement/published */
export async function listPublishedAnnouncements(options?: {
  [key: string]: any;
}) {
  return request<API.ResultListAnnouncementVO>("/api/announcement/published", {
    method: "GET",
    ...(options || {}),
  });
}

/** 批量标记公告已读（仅登录用户） POST /api/announcement/read */
export async function markRead(
  body: API.AnnouncementReadRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultInteger>("/api/announcement/read", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 编辑公告 POST /api/announcement/update */
export async function updateAnnouncement(
  body: API.AnnouncementUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/announcement/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
