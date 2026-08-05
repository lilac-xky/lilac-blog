// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 添加标签 POST /api/tag/add */
export async function addTag(
  body: API.TagAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/tag/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除标签 POST /api/tag/delete */
export async function deleteTag(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/tag/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取标签列表(管理员) POST /api/tag/list/page */
export async function listTagByPage(
  body: API.TagQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageTag>("/api/tag/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取标签列表(用户) POST /api/tag/list/page/vo */
export async function listTagByPageVo(
  body: API.TagQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageTagVO>("/api/tag/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 修改标签 POST /api/tag/update */
export async function updateTag(
  body: API.TagUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/tag/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
