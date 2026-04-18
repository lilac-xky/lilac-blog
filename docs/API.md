# API 文档

本文档描述 Lilac Blog 后端对外提供的 REST API。

- **Base URL**：`http://localhost:9090/api`
- **数据格式**：JSON
- **字符集**：UTF-8

## 通用约定

### 统一响应结构

所有接口均返回如下结构：

```json
{
  "code": 200,
  "data": {},
  "message": "ok"
}
```

| 字段    | 类型   | 说明           |
| ------- | ------ | -------------- |
| code    | int    | 业务状态码     |
| data    | any    | 业务数据       |
| message | string | 描述信息       |

### 错误码

| code   | 含义                |
| ------ | ------------------- |
| 200    | 成功                |
| 400001 | 未登录 / 登录失效   |
| 400002 | 参数错误            |
| 400003 | 无权限              |
| 400004 | 资源不存在          |
| 400005 | 禁止访问            |
| 400006 | 操作失败            |
| 500    | 系统异常            |

### 认证方式

- **用户端**：请求头携带 `user-token: <uuid>`
- **管理端**：请求头携带 `admin-token: <uuid>`

Token 在登录接口返回的 `LoginUserVO.token` 字段中获取。

---

## 用户模块

### 用户注册

- **URL**：`POST /api/user/register`
- **鉴权**：否

**请求体**

| 字段          | 类型   | 必填 | 说明                 |
| ------------- | ------ | ---- | -------------------- |
| userAccount   | string | 是   | 账号，最长 50 字符   |
| password      | string | 是   | 密码，6-20 字符      |
| checkPassword | string | 是   | 确认密码，需与上一致 |
| email         | string | 否   | 邮箱                 |

**响应**

```json
{
  "code": 200,
  "data": 1024,
  "message": "ok"
}
```

`data` 为新用户 ID。

### 用户登录

- **URL**：`POST /api/user/login`
- **鉴权**：否

**请求体**

| 字段        | 类型   | 必填 | 说明 |
| ----------- | ------ | ---- | ---- |
| userAccount | string | 是   | 账号 |
| password    | string | 是   | 密码 |

**响应**

```json
{
  "code": 200,
  "data": {
    "id": 1024,
    "userAccount": "lilac",
    "email": "lilac@example.com",
    "username": "紫丁香",
    "avater": "",
    "role": "user",
    "status": 1,
    "token": "a3f9d2e8-...-..."
  },
  "message": "ok"
}
```

客户端应将 `token` 保存后置于后续请求的 `user-token` 头中。

### 用户登出

- **URL**：`GET /api/user/logout`
- **鉴权**：需要 `user-token`

**响应**

```json
{ "code": 200, "data": true, "message": "ok" }
```

---

## 管理员模块

### 管理员登录

- **URL**：`POST /api/admin/login`
- **鉴权**：否
- **权限**：仅 `role = "admin"` 的账号可成功登录

**请求体**

| 字段        | 类型   | 必填 | 说明   |
| ----------- | ------ | ---- | ------ |
| userAccount | string | 是   | 账号   |
| password    | string | 是   | 密码   |

**响应**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "userAccount": "admin",
    "username": "管理员",
    "role": "admin",
    "status": 1,
    "token": "..."
  },
  "message": "ok"
}
```

非管理员账号登录会返回 `400003 无权限`。

### 管理员登出

- **URL**：`GET /api/admin/logout`
- **鉴权**：需要 `admin-token`

**响应**

```json
{ "code": 200, "data": true, "message": "ok" }
```

---

## 错误示例

### 未登录

请求需要登录的接口但未携带合法 token：

```json
{
  "code": 400001,
  "data": null,
  "message": "未登录"
}
```

### 参数校验失败

```json
{
  "code": 400002,
  "data": null,
  "message": "两次输入的密码不一致"
}
```

### 账号重复

```json
{
  "code": 400006,
  "data": null,
  "message": "账号已存在"
}
```

---

## 调试建议

- 推荐使用 [Apifox](https://apifox.com/) / Postman 调试
- 后端默认端口 `9090`，上下文路径 `/api`
- 本地前端默认通过 `http://localhost:9090` 直连后端（见前端 `request.ts`），无需额外代理
