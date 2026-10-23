// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 删除用户 POST /api/user/delete */
export async function deleteUser(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

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

/** 通过邮箱验证码重置密码 POST /api/user/resetPassword */
export async function resetPassword(
  body: API.ForgotPasswordRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/resetPassword", {
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

/** 发送原邮箱验证码 用于修改邮箱时的身份验证；原邮箱已不可用时，前端改为让用户填写当前密码 POST /api/user/sendOriginalEmailCode */
export async function sendOriginalEmailCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.sendOriginalEmailCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/sendOriginalEmailCode", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 发送重置密码验证码 GET /api/user/sendResetCode */
export async function sendResetCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.sendResetCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/sendResetCode", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 更新用户信息 POST /api/user/update */
export async function updateUser(
  body: API.UserEditRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 更新用户邮箱 POST /api/user/updateEmail */
export async function updateUserEmail(
  body: API.UserUpdateEmailRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/updateEmail", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 发送新邮箱验证码 仅登录用户可调用，验证码发往新邮箱，用于证明对新邮箱的所有权 POST /api/user/updateEmailCode */
export async function updateEmailCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateEmailCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/updateEmailCode", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 更新用户邮箱 身份验证二选一：原邮箱验证码或当前密码；新邮箱验证码必填 POST /api/user/updateEmailMethod */
export async function updateEmailMethod(
  body: API.UpdateEmailMethodRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/updateEmailMethod", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 已登录用户修改密码 POST /api/user/updatePassword */
export async function updatePassword(
  body: API.UpdatePasswordRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/user/updatePassword", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 验证邮箱所有权 POST /api/user/verifyEmailIdentity */
export async function verifyEmailIdentity(
  body: API.VerifyEmailIdentityRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultString>("/api/user/verifyEmailIdentity", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
