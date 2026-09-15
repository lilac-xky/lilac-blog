// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 添加灵感 POST /api/spark/add */
export async function addSpark(
  body: API.SparkAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/spark/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除灵感 POST /api/spark/delete */
export async function deleteSpark(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/spark/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取灵感详情(公开灵感人人可见，私有灵感仅创建者本人可见) GET /api/spark/get */
export async function getSpark(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSparkParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultSparkVO>("/api/spark/get", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取灵感列表(管理员，公开与私有全部可见) POST /api/spark/list/page */
export async function listSparkByPage(
  body: API.SparkQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageSpark>("/api/spark/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取灵感列表(前台，只返回公开灵感 + 本人私有灵感) POST /api/spark/list/page/vo */
export async function listSparkVoByPage(
  body: API.SparkQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageSparkVO>("/api/spark/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 修改灵感 POST /api/spark/update */
export async function updateSpark(
  body: API.SparkUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/spark/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
