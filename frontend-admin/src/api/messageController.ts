// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 分页查询我的消息 POST /api/message/my/page */
export async function listMyMessageByPage(
  body: API.MessageQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageMessageVO>("/api/message/my/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 标记消息已读（ids 为空则全部已读） POST /api/message/read */
export async function markRead(
  body: API.MessageReadRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultInteger>("/api/message/read", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 当前用户未读消息数 GET /api/message/unread/count */
export async function getUnreadCount(options?: { [key: string]: any }) {
  return request<API.ResultLong>("/api/message/unread/count", {
    method: "GET",
    ...(options || {}),
  });
}
