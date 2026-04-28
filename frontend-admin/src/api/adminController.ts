// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 删除用户 POST /api/admin/delete */
export async function deleteUser(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/admin/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 分页获取用户列表 POST /api/admin/list/page/vo */
export async function listUserVoByPage(
  body: API.UserQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageUserVO>("/api/admin/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 管理员登录 POST /api/admin/login */
export async function login(
  body: API.UserLoginRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLoginUserVO>("/api/admin/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 用户注销 GET /api/admin/logout */
export async function logout(options?: { [key: string]: any }) {
  return request<API.ResultBoolean>("/api/admin/logout", {
    method: "GET",
    ...(options || {}),
  });
}

/** 更新用户信息 POST /api/admin/update */
export async function updateUser(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/admin/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
