// @ts-ignore
/* eslint-disable */
import request from "@/request";

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
