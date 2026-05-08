# 系统架构

本文档描述 Lilac Blog 的整体架构设计、模块划分与关键技术决策。

## 总体架构

Lilac Blog 采用 **前后端分离 + 单体后端 + 双前端** 的架构模式：

```
┌─────────────────────────┐      ┌─────────────────────────┐
│   frontend-user (Vue3)  │      │  frontend-admin (Vue3)  │
│   用户前台（博客浏览）   │      │   管理后台（内容管理）   │
└────────────┬────────────┘      └────────────┬────────────┘
             │                                 │
             │ user-token                      │ admin-token
             │                                 │
             └──────────────┬──────────────────┘
                            │
                            │ HTTP / JSON
                            ▼
             ┌──────────────────────────────────────────┐
             │       backend (Spring Boot 3.5)          │
             │   ┌──────────────────────────────────┐   │
             │   │ Controller 层                    │   │
             │   │  ├─ UserController               │   │
             │   │  ├─ AdminController              │   │
             │   │  ├─ ArticleController            │   │
             │   │  ├─ CategoryController           │   │
             │   │  ├─ TagController                │   │
             │   │  └─ FileController               │   │
             │   ├──────────────────────────────────┤   │
             │   │ Service / Manager                │   │
             │   │  ├─ Sa-Token (StpKit)            │   │
             │   │  ├─ MailService（验证码）        │   │
             │   │  └─ FileStorageService（OSS）    │   │
             │   ├──────────────────────────────────┤   │
             │   │ Mapper 层（MyBatis-Plus）        │   │
             │   └──────────────────────────────────┘   │
             └────┬─────────────┬────────────┬──────────┘
                  │ JDBC        │ Redis      │ OSS / SMTP
                  ▼             ▼            ▼
          ┌───────────┐  ┌───────────┐  ┌─────────────┐
          │ MySQL 8.x │  │ Redis 7.x │  │ Aliyun OSS  │
          │           │  │（会话）   │  │ + QQ SMTP   │
          └───────────┘  └───────────┘  └─────────────┘
```

## 模块职责

### backend

基于 Spring Boot 3.5.13，承担所有业务逻辑与数据持久化：

- **Controller 层**：对外暴露 REST API，按业务垂直划分（user / admin / article / category / tag / file）
- **Service 层**：业务逻辑，封装对 Sa-Token、Redis、OSS、邮件的调用
- **Manager 层**：基础能力封装
  - `manager/auth`：`StpKit` 多账号体系入口
  - `manager/email`：`MailService` 异步发送验证码
  - `manager/upload`：`FileStorageService` 抽象本地 / 阿里云 OSS 实现
- **Mapper 层**：MyBatis-Plus 数据访问，配合 XML 自定义 SQL
- **认证体系**：Sa-Token 双账号 + Redis 会话（`sa-token-redis-jackson`）

### frontend-admin

基于 Vue 3 + Ant Design Vue 的管理后台：

- 管理员登录、登出
- 用户管理（列表 / 启停 / 删除）
- 文章管理 + Markdown 写作（md-editor-v3）
- 分类、标签管理
- 文件上传（头像 / 封面 / 正文）
- 全局路由守卫（未登录跳转 `/login`，首次导航远端校验 token）
- 请求拦截器注入 `admin-token`

### frontend-user

基于 Vue 3 + Ant Design Vue 的用户前台：

- 用户注册（邮箱验证码）、登录、登出
- 博客首页 / 文章列表 / 文章详情（Markdown 渲染）/ 归档
- 氛围组件：MusicPlayer、StarrySky、LyricsMarquee
- 请求拦截器注入 `user-token`
- 无强制登录守卫（支持游客浏览）
- 使用 `json-bigint` 解析后端 Long 类型 ID

## 双账号体系设计

项目采用 Sa-Token 的 **StpLogic 多账号体系** 实现 admin/user 隔离：

```java
public class StpKit {
    public static final StpLogic ADMIN = new StpLogic("admin") {
        @Override public String getTokenName() { return "admin-token"; }
    };
    public static final StpLogic USER  = new StpLogic("user") {
        @Override public String getTokenName() { return "user-token"; }
    };
}
```

- 两套账号各自独立的 Token、Session、踢人下线逻辑互不干扰
- 请求 Header：`admin-token` 与 `user-token` 分别对应两套体系
- 一个自然人可以同时以 admin 与 user 身份登录，互不影响

## 会话持久化

- 引入 `sa-token-redis-jackson`，Sa-Token 自动将 Token-Session、登录信息存入 Redis
- 服务重启后，已登录用户无感继续工作
- Redis 同时可承担后续业务缓存（待加入）

## 认证与授权流程

```
 用户                     前端                     后端                Redis
  │                        │                        │                    │
  │  填写账号密码          │                        │                    │
  │────────────────────────>                        │                    │
  │                        │ POST /user/login       │                    │
  │                        │────────────────────────>                    │
  │                        │                        │ MD5+盐 校验        │
  │                        │                        │ StpKit.USER.login()│
  │                        │                        │ 写 Token-Session ─>│
  │                        │ LoginUserVO + token    │                    │
  │                        │<────────────────────────                    │
  │                        │ 写入 Pinia + localStorage                   │
  │                        │                        │                    │
  │  后续请求              │                        │                    │
  │────────────────────────>                        │                    │
  │                        │ header: user-token=... │                    │
  │                        │────────────────────────>                    │
  │                        │                        │ 读 Token-Session <─│
  │                        │       业务响应         │                    │
  │                        │<────────────────────────                    │
```

- Token 类型：UUID
- Token 有效期：7 天（`timeout`）
- 活跃有效期：3 天（`active-timeout`，超期需重新登录）
- 会话存储：Redis（Jackson 序列化）
- 密码加密：MD5 + 固定盐 `"lilac"`（后续可升级为 BCrypt）

## 注册与邮箱验证码流程

```
用户         前端         后端              Redis           SMTP
 │  填邮箱   │             │                  │              │
 │──────────>│ /sendCode   │                  │              │
 │           │────────────>│ 生成 6 位 code   │              │
 │           │             │ 写入（5 分钟过期）>│              │
 │           │             │ 异步发送 ────────────────────────>│
 │           │  ok         │                  │              │
 │           │<────────────│                  │              │
 │           │             │                  │              │
 │  填账号 + code + 密码   │                  │              │
 │──────────>│ /register   │                  │              │
 │           │────────────>│ 取 code 比对  <──│              │
 │           │             │ 入库 user        │              │
 │           │  userId     │                  │              │
 │           │<────────────│                  │              │
```

## 文件上传与缩略图

`FileStorageService` 抽象出 `OssStorageServiceImpl` / `LocalStorageServiceImpl` 两种实现，由 `application.yml` 中 `storage.type` 控制：

- `oss`：上传至阿里云 OSS，按 `UploadTypeEnum` 决定路径前缀（avatar / cover / content）和 OSS Style 后缀生成 WebP 缩略图
- `local`：保留本地落盘的能力（用于离线开发 / 测试）

返回值统一为 `UploadPictureResult { url, thumbnailUrl }`。

## 统一响应规范

所有接口返回 `Result<T>` 结构：

```json
{
  "code": 200,
  "data": { ... },
  "message": "ok"
}
```

错误码定义见 `HttpsCodeEnum`：

| code   | 含义                |
| ------ | ------------------- |
| 200    | 成功                |
| 400    | 无效的请求参数      |
| 401    | 未授权              |
| 404    | 请求的资源不存在    |
| 500    | 系统内部错误        |
| 400001 | 需要登录            |
| 400002 | 参数错误            |
| 400003 | 操作失败            |
| 400004 | 用户名或密码错误    |
| 400005 | 用户已存在          |
| 400006 | 未找到该资源        |

前端请求拦截器统一处理 `401` 与 `400001`：清除本地登录态，admin 侧自动跳转登录页，user 侧不强制跳转。

## 跨域策略

后端 `CorsConfig` 放开全部来源以便本地开发：

- `allowedOriginPatterns = "*"`
- `allowCredentials = true`
- 允许方法：GET / POST / PUT / DELETE / OPTIONS

**生产部署需收紧为显式白名单。**

## 数据持久化

- ORM：MyBatis-Plus 3.5.15
- 逻辑删除：`isDeleted` 字段，`0 = 正常 / 1 = 已删`
- 主键策略：`IdType.ASSIGN_ID`（雪花 ID，前端使用 json-bigint 解析）
- 详细表结构见 [DATABASE.md](./DATABASE.md)

## 技术选型理由

| 维度       | 选型                       | 理由                                       |
| ---------- | -------------------------- | ------------------------------------------ |
| 后端框架   | Spring Boot 3.5 + JDK 17   | 主流稳定、生态完善、原生支持 Jakarta EE    |
| 认证       | Sa-Token + Redis           | 轻量、双账号体系、会话持久化即装即用       |
| ORM        | MyBatis-Plus               | CRUD 便利、保留自定义 SQL 灵活性           |
| 对象存储   | 阿里云 OSS                 | 国内可用性高、支持图片处理（WebP 缩略图）  |
| 邮件       | spring-boot-starter-mail   | 标准 SMTP，QQ 邮箱授权码即可               |
| 前端框架   | Vue 3 + TS + Vite          | Composition API、构建速度快、类型安全      |
| UI 库      | Ant Design Vue 4.x         | 企业级组件丰富、中文文档完善               |
| Markdown   | md-editor-v3               | 编辑/渲染同库，前后台一致体验              |
| 状态管理   | Pinia                      | Vue 官方推荐、API 简洁                     |
| API 生成   | @umijs/openapi（admin）    | 后端 OpenAPI → 前端 TS 客户端自动生成      |
| BigInt 解析 | json-bigint（user）        | 后端雪花 ID 超出 JS 安全整数范围           |

## 后续演进方向

- [ ] 评论与点赞
- [ ] 全文搜索（ElasticSearch / MeiliSearch）
- [ ] 浏览量统计与热门文章
- [ ] 日志中心化（ELK / Loki）
- [ ] CI/CD 与容器化部署
- [ ] BCrypt 替代 MD5 密码加密
