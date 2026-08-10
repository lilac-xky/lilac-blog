// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 添加角色 POST /api/role/add */
export async function addRole(
  body: API.RoleAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/role/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 角色权限连接 POST /api/role/addPermission */
export async function addPermission(
  body: API.RoleAddPermissionsRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/role/addPermission", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 角色批量添加权限 POST /api/role/batchAddPermissions */
export async function batchAddPermissions(
  body: API.RoleBatchAddPermissionsRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/role/batchAddPermissions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 角色批量移除权限 POST /api/role/batchRemovePermissions */
export async function batchRemovePermissions(
  body: API.RoleBatchRemovePermissionsRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/role/batchRemovePermissions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除角色 POST /api/role/delete */
export async function deleteRole(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/role/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取角色列表 POST /api/role/list */
export async function getRoleList(
  body: API.RoleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageRole>("/api/role/list", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 查询角色的权限列表 POST /api/role/permissions */
export async function getRolePermissions(
  body: API.RolePermissionQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultListPermission>("/api/role/permissions", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 角色移除权限 POST /api/role/removePermission */
export async function removePermission(
  body: API.RoleRemovePermissionRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/role/removePermission", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 修改角色 POST /api/role/update */
export async function updateRole(
  body: API.RoleUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/role/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
