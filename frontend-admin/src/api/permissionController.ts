// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 添加权限 POST /api/permission/add */
export async function addPermission(
  body: API.PermissionAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/permission/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除权限 POST /api/permission/delete */
export async function deletePermission(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/permission/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取权限列表 POST /api/permission/list */
export async function getPermissionList(
  body: API.PermissionQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPagePermission>("/api/permission/list", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 更新权限 POST /api/permission/update */
export async function updatePermission(
  body: API.PermissionUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/permission/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
