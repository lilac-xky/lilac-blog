// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 随手记一条碎片 POST /api/note/add */
export async function addNote(
  body: API.NoteAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/note/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 整理预览：返回 Markdown 骨架，不落库 POST /api/note/aggregate */
export async function aggregate(
  body: API.NoteAggregateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultNoteAggregateVO>("/api/note/aggregate", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 确认整理：生成草稿文章并归档碎片 POST /api/note/compose */
export async function compose(
  body: API.NoteAggregateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultNoteComposeVO>("/api/note/compose", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除自己的碎片 POST /api/note/delete */
export async function deleteNote(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/note/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取碎片详情（仅本人可见） GET /api/note/get */
export async function getNote(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getNoteParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultNoteVO>("/api/note/get", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 分页查询我的碎片 POST /api/note/list/page */
export async function listMyNoteByPage(
  body: API.NoteQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageNoteVO>("/api/note/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 后台分页查询全部碎片（跨用户） POST /api/note/list/page/admin */
export async function listNoteAdminByPage(
  body: API.NoteQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageNoteVO>("/api/note/list/page/admin", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 主题分布统计 GET /api/note/topic/stat */
export async function topicStat(options?: { [key: string]: any }) {
  return request<API.ResultNoteTopicStatVO>("/api/note/topic/stat", {
    method: "GET",
    ...(options || {}),
  });
}

/** 修改自己的碎片 POST /api/note/update */
export async function updateNote(
  body: API.NoteUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/note/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
