// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 用户登录 POST /api/user/login */
export async function login(
  body: API.UserLoginRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLoginUserVO>("/api/user/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 用户注销 GET /api/user/logout */
export async function logout(options?: { [key: string]: any }) {
  return request<API.ResultBoolean>("/api/user/logout", {
    method: "GET",
    ...(options || {}),
  });
}

/** 用户注册 POST /api/user/register */
export async function register(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/user/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 发送注册验证码 GET /api/user/sendCode */
export async function sendRegisterCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.sendRegisterCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/sendCode", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
