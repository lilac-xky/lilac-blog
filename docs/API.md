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

## 灵感模块（`/spark`）

灵感按 `visibility` 区分可见性，服务端强制校验，请求体中的 `visibility` 无法越权：

- `public`：任何人可查
- `private`（默认）：只有创建者本人可查，他人查询详情返回「资源不存在」

### 添加灵感

- **URL**：`POST /api/spark/add`
- **鉴权**：需要 `admin-token`
- **权限**：`spark:add`

**请求体**

| 字段       | 类型   | 必填 | 说明                                                |
| ---------- | ------ | ---- | --------------------------------------------------- |
| content    | string | 是   | 灵感内容                                            |
| status     | string | 否   | 默认 `spark`，可选 `brewing` / `doing` / `done` / `paused` / `dropped` |
| visibility | string | 否   | 默认 `private`，可选 `public` / `private`           |

创建人 `userId` 由服务端写入当前登录用户，请求体传入无效。

**响应**

```json
{ "code": 200, "data": 9001, "message": "ok" }
```

### 修改灵感

- **URL**：`POST /api/spark/update`
- **鉴权**：需要 `admin-token`
- **权限**：`spark:update`
- **请求体**：`{ "id": 9001, "content": "...", "status": "doing", "visibility": "public" }`

### 删除灵感

- **URL**：`POST /api/spark/delete`
- **鉴权**：需要 `admin-token`
- **权限**：`spark:delete`
- **请求体**：`{ "id": 9001 }`

### 后台分页列表

- **URL**：`POST /api/spark/list/page`
- **鉴权**：需要 `admin-token`
- **权限**：`spark:list`
- **可见范围**：公开 + 私有全部可见

**请求体**（继承 `PageRequest`）

| 字段       | 类型   | 必填 | 说明                 |
| ---------- | ------ | ---- | -------------------- |
| id         | long   | 否   | 灵感 id              |
| content    | string | 否   | 内容模糊匹配         |
| status     | string | 否   | 灵感状态             |
| visibility | string | 否   | `public` / `private` |
| userId     | long   | 否   | 创建人 id            |

返回 `Page<Spark>`（实体分页，字段含 `userId`、`createTime`、`isDeleted`）。

### 前台分页列表

- **URL**：`POST /api/spark/list/page/vo`
- **鉴权**：否（携带 `user-token` 时可额外看到自己的私有灵感）
- **限制**：`pageSize` ≤ 20

**可见范围**：所有 `public` 灵感 + 当前登录用户自己的 `private` 灵感；请求体中的 `visibility` 会被服务端忽略。

请求字段同上一接口（`visibility`、`userId` 过滤对前台不生效）。

### 灵感详情

- **URL**：`GET /api/spark/get?id=<id>`
- **鉴权**：否（携带 `user-token` 时才能取到自己的私有灵感）

**响应**

```json
{
  "code": 200,
  "data": {
    "id": 9001,
    "content": "...",
    "status": "spark",
    "visibility": "public",
    "userId": 1024,
    "createTime": "2026-05-01T12:00:00"
  },
  "message": "ok"
}
```

`private` 灵感非本人请求时返回查不到：

```json
{ "code": 400006, "data": null, "message": "灵感不存在" }
```

---

## 碎知模块（`/note`）

碎片全部**私有**，所有接口都以当前登录用户为边界，请求体里的 `userId` 一律不可信、由服务端强制改写。
他人碎片统一按「资源不存在」处理，不泄露内容。

鉴权约定：

- 除后台接口外，`/note/**` 均要求 `user-token`；
- 权限点见 `docs/sql/note.sql`；
- 后台接口 `/note/list/page/admin` 要求 `admin-token`，不受上述用户登录拦截约束。

### 添加碎片（随手记）

- **URL**：`POST /api/note/add`
- **鉴权**：需要 `user-token`
- **权限**：`note:add`

**请求体**

| 字段    | 类型     | 必填 | 说明                                       |
| ------- | -------- | ---- | ------------------------------------------ |
| content | string   | 是   | 知识点正文，≤ 10000 字符                   |
| insight | string   | 否   | 我的一句话理解，≤ 500 字符                 |
| source  | string   | 否   | 来源：书名 / 链接 / 课程，≤ 255 字符       |
| tagIds  | long[]   | 否   | 主题标签 ID 列表，**第一个即整理时的分组依据** |

归属人 `userId` 由服务端写入当前登录用户，请求体传入无效。

**响应**

```json
{ "code": 200, "data": "1001", "message": "ok" }
```

> 主键为雪花 ID，后端已配置 `Long → String` 序列化，前端始终按字符串处理。

### 修改碎片

- **URL**：`POST /api/note/update`
- **鉴权**：需要 `user-token`
- **权限**：`note:update`

**请求体**

| 字段    | 类型   | 必填 | 说明                                                     |
| ------- | ------ | ---- | -------------------------------------------------------- |
| id      | long   | 是   | 碎片 ID                                                  |
| content | string | 否   | 传则更新                                                 |
| insight | string | 否   | 传空串表示清空                                           |
| source  | string | 否   | 传空串表示清空                                           |
| tagIds  | long[] | 否   | **传 null 表示不改动标签；传空数组表示清空标签**         |

### 删除碎片

- **URL**：`POST /api/note/delete`
- **鉴权**：需要 `user-token`
- **权限**：`note:delete`
- **请求体**：`{ "id": 1001 }`
- **副作用**：逻辑删除 `note`，物理清理 `note_tag` 关联

### 碎片详情

- **URL**：`GET /api/note/get?id=<id>`
- **鉴权**：需要 `user-token`
- **权限**：`note:list`

**响应**

```json
{
  "code": 200,
  "data": {
    "id": "1001",
    "userId": "1024",
    "content": "过期时间不是「到点删除」，而是惰性删除 + 定期删除兜底",
    "insight": null,
    "source": "《Redis 设计与实现》第 8 章",
    "status": 0,
    "articleId": null,
    "createTime": "2026-11-08T10:20:00",
    "tags": [{ "id": "10", "tagName": "Redis" }]
  },
  "message": "ok"
}
```

他人碎片返回：

```json
{ "code": 400006, "data": null, "message": "碎片不存在" }
```

### 我的碎片分页列表

- **URL**：`POST /api/note/list/page`
- **鉴权**：需要 `user-token`
- **权限**：`note:list`
- **限制**：`pageSize` ≤ 50

**请求体**（继承 `PageRequest`）

| 字段      | 类型   | 必填 | 说明                             |
| --------- | ------ | ---- | -------------------------------- |
| content   | string | 否   | 正文模糊匹配                     |
| status    | int    | 否   | `0` 未整理 / `1` 已归档          |
| tagIds    | long[] | 否   | 命中任意一个主题即返回           |
| articleId | long   | 否   | 归档到的文章 ID                  |

`userId` 由服务端强制写入当前登录用户，请求传入无效。

### 整理预览

把一批碎片渲染成 Markdown 骨架，**不落库**，用于前端预览与人工调整。

- **URL**：`POST /api/note/aggregate`
- **鉴权**：需要 `user-token`
- **权限**：`note:compose`

**请求体**

| 字段        | 类型   | 必填 | 说明                                                     |
| ----------- | ------ | ---- | -------------------------------------------------------- |
| noteIds     | long[] | 否   | 指定要整理的碎片，**优先于时间区间**                     |
| fromTime    | date   | 否   | 按创建时间区间筛选，起始（含）                           |
| toTime      | date   | 否   | 按创建时间区间筛选，结束（含）                           |
| tagIds      | long[] | 否   | 只整理这些主题下的碎片，为空表示不限                     |
| title       | string | 否   | 自定义标题，为空则按规则生成                             |
| insertHints | bool   | 否   | 是否插入过渡提示注释，**默认 true**                      |

选取规则：`noteIds` 非空时优先使用；否则用时间区间；两者都没有则取全部未整理碎片。
参与整理的碎片**强制限定为当前用户 + `status=0` 未整理**，单次上限 200 条。

**响应**

```json
{
  "code": 200,
  "data": {
    "title": "学习速记 · 2026-11",
    "markdown": "## Redis\n\n- 过期时间不是「到点删除」，而是惰性删除 + 定期删除兜底。\n  `2026-11-08` `《Redis 设计与实现》第 8 章`\n\n## MySQL\n\n- 联合索引遵循最左前缀，跳过最左列就用不上索引。\n  `2026-11-10` `极客时间《MySQL 实战》`",
    "noteCount": 2,
    "untaggedCount": 0,
    "topics": [
      { "tagId": "10", "tagName": "Redis", "count": 1 },
      { "tagId": "11", "tagName": "MySQL", "count": 1 }
    ],
    "tagIds": ["10", "11"],
    "noteIds": ["1001", "1002"]
  },
  "message": "ok"
}
```

**生成规则**（确定性，同一份输入永远产出同一份输出）：

1. **分组**：按每条碎片关联的**第一个标签**归组；无标签的归入「杂项」。
2. **排序**：组间按组内碎片数**降序**，「杂项」恒排最后；组内按 `createTime` **升序**。
3. **渲染**：每个主题一个 `## 小节`，碎片渲染为 `- 正文` + 缩进的日期/来源行内代码 + `> 理解` 引用块。
4. **过渡提示**：相邻小节之间插入 `<!-- 写一句过渡：... -->` 注释，编辑器可见、渲染时隐藏。
5. **标题**：单主题 → `{主题} 学习笔记`；多主题 → `学习速记 · {YYYY-MM}`（跨月时为 `{起} ~ {止}`）。

### 确认整理（生成草稿）

- **URL**：`POST /api/note/compose`
- **鉴权**：需要 `user-token`
- **权限**：`note:compose`
- **请求体**：同 `/note/aggregate`

**响应**

```json
{
  "code": 200,
  "data": { "articleId": "2001", "title": "学习速记 · 2026-11", "noteCount": 2 },
  "message": "ok"
}
```

**副作用**：

1. 新建 `article`，`status=0`（草稿），正文为生成的 Markdown；
2. 本次涉及的主题标签自动写入 `article_tag`；
3. 参与整理的碎片回写 `status=1`、`articleId=<新文章 ID>`。

前端拿到 `articleId` 后直接跳转 `/user/write?id=<articleId>`，**复用现有编辑器**润色，如需发布再走原有审核流程。

### 主题分布统计

- **URL**：`GET /api/note/topic/stat`
- **鉴权**：需要 `user-token`
- **权限**：`note:list`

**响应**

```json
{
  "code": 200,
  "data": {
    "rawTotal": 12,
    "archivedTotal": 30,
    "topics": [
      { "tagId": "10", "tagName": "Redis", "count": 5 },
      { "tagId": "11", "tagName": "MySQL", "count": 3 },
      { "tagId": null, "tagName": "杂项", "count": 4 }
    ]
  },
  "message": "ok"
}
```

`rawTotal` / `archivedTotal` 为精确计数；`topics` 只统计最近 2000 条未整理碎片，个人博客场景足够。

### 后台碎片分页列表

- **URL**：`POST /api/note/list/page/admin`
- **鉴权**：需要 `admin-token`
- **权限**：`note:list:admin`
- **说明**：跨用户查询全部碎片，请求体同「我的碎片分页列表」，此时 `userId` 作为**筛选条件**生效

---

## 公告模块（`/announcement`）

系统公告正文使用 **Markdown**。管理端负责新增、编辑、删除、发布/下线和分页查询；用户端负责待读列表与批量已读。

已读规则：

- 登录用户：服务端根据 `announcement_read` 过滤已读公告；
- 游客：`pending` 返回已发布公告，前端用 `localStorage` 记录已读；
- 关闭弹窗即视为已读；同一公告不会重复弹，管理员发布新公告后会再次进入待读列表。

### 后台新增公告

- **URL**：`POST /api/announcement/add`
- **鉴权**：需要 `admin-token`
- **权限**：`announcement:add`
- **说明**：新增后状态为草稿（`status=0`），需再调用发布接口

**请求体**

| 字段    | 类型   | 必填 | 说明               |
| ------- | ------ | ---- | ------------------ |
| title   | string | 是   | 公告标题，≤150 字符 |
| content | string | 是   | Markdown 正文      |

**响应**

```json
{ "code": 200, "data": 2121878085048479745, "message": "ok" }
```

`data` 为公告 ID。

### 后台编辑公告

- **URL**：`POST /api/announcement/update`
- **鉴权**：需要 `admin-token`
- **权限**：`announcement:update`
- **说明**：编辑已发布公告不会重置已读记录

**请求体**

```json
{ "id": 2121878085048479745, "title": "公告标题", "content": "## Markdown 正文" }
```

### 后台删除公告

- **URL**：`POST /api/announcement/delete`
- **鉴权**：需要 `admin-token`
- **权限**：`announcement:delete`
- **请求体**：`{ "id": 2121878085048479745 }`
- **说明**：逻辑删除，已读记录保留

### 后台发布/下线公告

- **URL**：`POST /api/announcement/publish`
- **鉴权**：需要 `admin-token`
- **权限**：`announcement:publish`

**请求体**

| 字段    | 类型    | 必填 | 说明                    |
| ------- | ------- | ---- | ----------------------- |
| id      | long    | 是   | 公告 ID                 |
| publish | boolean | 是   | `true` 发布 / `false` 下线 |

首次发布或下线后重新发布会刷新 `publishTime`，公告按发布时间倒序展示。

### 后台分页列表

- **URL**：`POST /api/announcement/list/page`
- **鉴权**：需要 `admin-token`
- **权限**：`announcement:list`

**请求体**（继承 `PageRequest`）

| 字段    | 类型   | 必填 | 说明                          |
| ------- | ------ | ---- | ----------------------------- |
| id      | long   | 否   | 公告 ID                       |
| title   | string | 否   | 标题模糊匹配                  |
| status  | int    | 否   | `0` 草稿 / `1` 已发布 / `2` 已下线 |

返回 `Page<Announcement>`，记录含 `id`、`title`、`content`、`status`、`publishTime`、`createTime` 等字段。

### 后台公告详情

- **URL**：`GET /api/announcement/get?id=<id>`
- **鉴权**：需要 `admin-token`
- **权限**：`announcement:list`
- **说明**：草稿、已发布、已下线均可查询

### 前台待读公告

- **URL**：`GET /api/announcement/pending`
- **鉴权**：否；携带 `user-token` 时按登录用户过滤已读
- **说明**：返回最多 20 条，按 `publishTime` 倒序；游客返回已发布公告，由前端按 `localStorage` 过滤

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "id": "2121878181890764801",
      "title": "公告标题",
      "content": "## Markdown 正文",
      "publishTime": "2026-11-15T08:09:09.000+00:00",
      "createTime": "2026-11-15T08:08:52.000+00:00"
    }
  ],
  "message": "ok"
}
```

没有待读公告时 `data` 为 `[]`。

### 前台公告中心（全部已发布公告）

- **URL**：`GET /api/announcement/published`
- **鉴权**：否；携带 `user-token` 时会填充 `isRead`
- **说明**：用于「公告弹窗读完后再回看」的场景，返回最多 50 条已发布公告，按 `publishTime` 倒序

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "id": "2121891094118658049",
      "title": "公告标题",
      "content": "## Markdown 正文",
      "publishTime": "2026-11-15T09:00:35.000+00:00",
      "createTime": "2026-11-15T09:00:34.000+00:00",
      "isRead": true
    }
  ],
  "message": "ok"
}
```

`isRead` 说明：

- 登录用户：`true` / `false`，表示该用户是否读过；
- 游客：`null`，游客端按 `localStorage` 自行判断。

### 批量标记公告已读

- **URL**：`POST /api/announcement/read`
- **鉴权**：需要 `user-token`
- **说明**：仅登录用户；游客由前端写 `localStorage`，不调用此接口

**请求体**

| 字段             | 类型     | 必填 | 说明                       |
| ---------------- | -------- | ---- | -------------------------- |
| announcementIds  | long[]   | 是   | 公告 ID 列表，最多 100 条  |

```json
{ "announcementIds": ["2121878181890764801", "2121878181760741377"] }
```

**响应**

```json
{ "code": 200, "data": 2, "message": "ok" }
```

`data` 为本次新增的已读记录数；重复标记返回 `0`，不报错。

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
