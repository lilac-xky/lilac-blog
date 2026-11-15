// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 查询当前待读公告 GET /api/announcement/pending */
export async function listPendingAnnouncements(options?: { [key: string]: any }) {
  return request<API.ResultListAnnouncementVO>("/api/announcement/pending", {
    method: "GET",
    ...(options || {}),
  });
}

/** 查询全部已发布公告（公告中心回看） GET /api/announcement/published */
export async function listPublishedAnnouncements(options?: { [key: string]: any }) {
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
