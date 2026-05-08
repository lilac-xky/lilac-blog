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

| code   | 含义                  |
| ------ | --------------------- |
| 200    | 操作成功              |
| 400    | 无效的请求参数        |
| 401    | 未授权                |
| 404    | 请求的资源不存在      |
| 500    | 系统内部错误          |
| 400001 | 需要登录              |
| 400002 | 参数错误              |
| 400003 | 操作失败              |
| 400004 | 用户名或密码错误      |
| 400005 | 用户已存在            |
| 400006 | 未找到该资源          |

### 认证方式

- **用户端**：请求头携带 `user-token: <uuid>`
- **管理端**：请求头携带 `admin-token: <uuid>`

Token 在登录接口返回的 `LoginUserVO.token` 字段中获取。Sa-Token 会话持久化到 Redis，服务重启不影响登录态。

### 通用请求体

#### `DeleteRequest`

```json
{ "id": 1024 }
```

#### `PageRequest`（其他查询请求继承）

```json
{
  "current": 1,
  "pageSize": 10,
  "sortOrder": "descend"
}
```

---

## 用户模块（`/user`）

### 用户注册

- **URL**：`POST /api/user/register`
- **鉴权**：否

**请求体**

| 字段          | 类型   | 必填 | 说明                       |
| ------------- | ------ | ---- | -------------------------- |
| userAccount   | string | 是   | 账号                       |
| email         | string | 是   | 邮箱                       |
| password      | string | 是   | 密码                       |
| checkPassword | string | 是   | 确认密码（需与 password 一致） |
| code          | string | 是   | 邮箱验证码（先调用 `/user/sendCode`）|

**响应**

```json
{ "code": 200, "data": 1024, "message": "ok" }
```

`data` 为新用户 ID。

### 发送邮箱验证码

- **URL**：`GET /api/user/sendCode?email=<email>`
- **鉴权**：否

**响应**

```json
{ "code": 200, "data": true, "message": "ok" }
```

服务端会向目标邮箱异步发送 6 位数字验证码（5 分钟有效）。

### 用户登录

- **URL**：`POST /api/user/login`
- **鉴权**：否

**请求体**

| 字段     | 类型   | 必填 | 说明 |
| -------- | ------ | ---- | ---- |
| account  | string | 是   | 账号 |
| password | string | 是   | 密码 |

**响应**

```json
{
  "code": 200,
  "data": {
    "id": 1024,
    "userAccount": "lilac",
    "email": "lilac@example.com",
    "username": "紫丁香",
    "avatar": "",
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

### 修改个人资料

- **URL**：`POST /api/user/update`
- **鉴权**：需要 `user-token`

**请求体**

| 字段        | 类型   | 必填 | 说明        |
| ----------- | ------ | ---- | ----------- |
| id          | long   | 是   | 用户 ID     |
| userAccount | string | 否   | 账号        |
| email       | string | 否   | 邮箱        |
| username    | string | 否   | 昵称        |
| avatar      | string | 否   | 头像 URL    |

接口同时刷新 Sa-Token 会话内的 `LoginUserVO`。

### 注销自己

- **URL**：`POST /api/user/delete`
- **鉴权**：需要 `user-token`
- **请求体**：`{ "id": <自己的 id> }`（仅允许删除自己）

---

## 管理员模块（`/admin`）

### 管理员登录

- **URL**：`POST /api/admin/login`
- **鉴权**：否
- **权限**：仅 `role = "admin"` 的账号可成功登录

**请求体**

| 字段     | 类型   | 必填 | 说明 |
| -------- | ------ | ---- | ---- |
| account  | string | 是   | 账号 |
| password | string | 是   | 密码 |

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

非管理员账号登录会返回错误码。

### 管理员登出

- **URL**：`GET /api/admin/logout`
- **鉴权**：需要 `admin-token`

### 校验当前 token

- **URL**：`GET /api/admin/currentUser`
- **鉴权**：需要 `admin-token`
- **用途**：前端启动时检测登录态是否仍有效

### 用户分页列表

- **URL**：`POST /api/admin/list/page/vo`
- **鉴权**：需要 `admin-token`

**请求体**（继承 `PageRequest`）

| 字段        | 类型   | 必填 | 说明 |
| ----------- | ------ | ---- | ---- |
| id          | long   | 否   |      |
| userAccount | string | 否   |      |
| email       | string | 否   |      |
| username    | string | 否   |      |
| role        | string | 否   |      |
| status      | int    | 否   |      |

返回 `Page<UserVO>`。

### 修改用户

- **URL**：`POST /api/admin/update`
- **鉴权**：需要 `admin-token`
- 字段同 `UserUpdateRequest`：id / userAccount / email / username / avatar / role / status

### 启用 / 禁用用户

- **URL**：`POST /api/admin/updateStatus`
- **鉴权**：需要 `admin-token`
- **请求体**：`{ "id": 1024, "status": 1 }`

### 删除用户

- **URL**：`POST /api/admin/delete`
- **鉴权**：需要 `admin-token`
- **请求体**：`{ "id": 1024 }`（不允许删除自己）

---

## 文章模块（`/article`）

### 添加文章

- **URL**：`POST /api/article/add`
- **鉴权**：需要登录

**请求体**

| 字段       | 类型   | 必填 | 说明                      |
| ---------- | ------ | ---- | ------------------------- |
| title      | string | 是   | 标题                      |
| summary    | string | 否   | 摘要                      |
| content    | string | 是   | Markdown 正文             |
| categoryId | long   | 否   | 关联分类 id               |
| coverUrl   | string | 否   | 封面图 URL                |
| status     | int    | 否   | 0 草稿 / 1 待审核 / 2 已发布 |
| isTop      | int    | 否   | 1 置顶 / 0 普通           |
| tagIds     | long[] | 否   | 标签 id 数组              |

**响应**

```json
{ "code": 200, "data": 7102, "message": "ok" }
```

`data` 为新文章 ID。

### 更新文章

- **URL**：`POST /api/article/update`
- **鉴权**：需要登录
- 字段同 `ArticleUpdateRequest`（含 `id` + 上述字段）

### 删除文章

- **URL**：`POST /api/article/delete`
- **鉴权**：需要登录
- **请求体**：`{ "id": 7102 }`

### 后台分页列表

- **URL**：`POST /api/article/list/page`
- **鉴权**：需要 `admin-token`

**请求体**（继承 `PageRequest`）

| 字段       | 类型   | 必填 | 说明 |
| ---------- | ------ | ---- | ---- |
| id         | long   | 否   |      |
| title      | string | 否   |      |
| categoryId | long   | 否   |      |
| userId     | long   | 否   |      |
| isTop      | int    | 否   |      |
| status     | int    | 否   |      |
| tagIds     | long[] | 否   | 命中任一标签 |

返回 `Page<ArticleVO>`（含 `categoryName`、`tags`）。

### 前台分页列表

- **URL**：`POST /api/article/list/page/vo`
- **鉴权**：否
- **限制**：`pageSize` ≤ 20

字段与上一接口一致，仅返回已发布文章。

### 文章详情

- **URL**：`GET /api/article/get?id=<id>`
- **鉴权**：否

**响应**

```json
{
  "code": 200,
  "data": {
    "id": 7102,
    "title": "...",
    "summary": "...",
    "content": "...",
    "categoryId": 3,
    "categoryName": "随笔",
    "coverUrl": "https://...",
    "userId": 1024,
    "viewCount": 128,
    "isTop": 0,
    "status": 2,
    "createTime": "2026-05-01T12:00:00",
    "tags": [
      { "id": 1, "tagName": "Vue" },
      { "id": 5, "tagName": "Spring" }
    ]
  },
  "message": "ok"
}
```

---

## 分类模块（`/category`）

### 添加分类

- **URL**：`POST /api/category/add`
- **请求体**：`{ "categoryName": "前端" }`

### 修改分类

- **URL**：`POST /api/category/update`
- **请求体**：`{ "id": 1, "categoryName": "前端开发" }`

### 删除分类

- **URL**：`POST /api/category/delete`
- **请求体**：`{ "id": 1 }`

### 后台分页列表

- **URL**：`POST /api/category/list/page`
- **请求体**：继承 `PageRequest`，可加 `id` / `categoryName`
- **返回**：`Page<Category>`

### 前台分页列表

- **URL**：`POST /api/category/list/page/vo`
- **返回**：`Page<CategoryVO>`（仅 `id` + `categoryName`）

---

## 标签模块（`/tag`）

接口形态与分类完全一致，将路径中的 `/category` 替换为 `/tag`，字段名 `categoryName` → `tagName`。

| 方法 | 路径                | 说明        |
| ---- | ------------------- | ----------- |
| POST | `/tag/add`          | 新增标签    |
| POST | `/tag/update`       | 修改标签    |
| POST | `/tag/delete`       | 删除标签    |
| POST | `/tag/list/page`    | 后台分页    |
| POST | `/tag/list/page/vo` | 前台 VO 分页 |

---

## 文件模块（`/file`）

### 本地文件上传

- **URL**：`POST /api/file/upload`
- **Content-Type**：`multipart/form-data`
- **鉴权**：需要登录

| 参数 | 类型 | 必填 | 说明                                    |
| ---- | ---- | ---- | --------------------------------------- |
| file | File | 是   | 文件本体                                |
| type | string | 否 | `avatar` / `cover` / `content`，默认 `avatar` |

**响应**

```json
{
  "code": 200,
  "data": {
    "url": "https://lilacs.oss-cn-beijing.aliyuncs.com/cover/xxx.jpg",
    "thumbnailUrl": "https://lilacs.oss-cn-beijing.aliyuncs.com/cover/xxx.jpg?x-oss-process=style/cover"
  },
  "message": "ok"
}
```

### 通过外链 URL 上传

- **URL**：`POST /api/file/uploadByUrl`
- **参数**：`url` + `type`
- **响应**：同上

---

## 错误示例

### 未登录

```json
{ "code": 400001, "data": null, "message": "需要登录" }
```

### 参数校验失败

```json
{ "code": 400002, "data": null, "message": "两次输入的密码不一致" }
```

### 用户名或密码错误

```json
{ "code": 400004, "data": null, "message": "用户名或密码错误" }
```

### 账号重复

```json
{ "code": 400005, "data": null, "message": "用户已存在" }
```

---

## 调试建议

- 推荐使用 [Apifox](https://apifox.com/) / Postman 调试
- 后端默认端口 `9090`，上下文路径 `/api`
- 本地前端默认通过 `http://localhost:9090` 直连后端（见前端 `request.ts`），无需额外代理
- 文件上传时若使用 OSS，需要正确配置 `application-dev.yml` 中的 OSS 凭证
