// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 添加分类 POST /api/category/add */
export async function addCategory(
  body: API.CategoryAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/category/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除分类 POST /api/category/delete */
export async function deleteCategory(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/category/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取分类列表(管理员) POST /api/category/list/page */
export async function listCategoryByPage(
  body: API.CategoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageCategory>("/api/category/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取分类列表(用户) POST /api/category/list/page/vo */
export async function listCategoryByPageVo(
  body: API.CategoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageCategoryVO>("/api/category/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 修改分类 POST /api/category/update */
export async function updateCategory(
  body: API.CategoryUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/category/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
