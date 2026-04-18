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
             ┌──────────────────────────────┐
             │   backend (Spring Boot 3.5)  │
             │   ┌────────────────────────┐ │
             │   │ Controller 层          │ │
             │   │  ├─ UserController     │ │
             │   │  └─ AdminController    │ │
             │   ├────────────────────────┤ │
             │   │ Service 层 + Sa-Token  │ │
             │   │  ├─ StpKit.USER        │ │
             │   │  └─ StpKit.ADMIN       │ │
             │   ├────────────────────────┤ │
             │   │ Mapper 层（MyBatis+）  │ │
             │   └────────────────────────┘ │
             └──────────────┬───────────────┘
                            │ JDBC
                            ▼
                    ┌───────────────┐
                    │   MySQL 8.x   │
                    └───────────────┘
```

## 模块职责

### backend

基于 Spring Boot 3.5.13，承担所有业务逻辑与数据持久化：

- **Controller 层**：对外暴露 REST API，按角色分离为 `UserController` 与 `AdminController`
- **Service 层**：业务逻辑处理，封装对 Sa-Token 的调用
- **Mapper 层**：MyBatis-Plus 数据访问，配合 XML 自定义 SQL
- **认证体系**：Sa-Token 双账号体系（`StpKit.USER` / `StpKit.ADMIN`），通过不同 header（`user-token` / `admin-token`）区分
- **统一响应**：`Result<T>` 封装 + `GlobalExceptionHandler` 全局异常处理

### frontend-admin

基于 Vue 3 + Ant Design Vue 的管理后台：

- 管理员登录、登出
- 仪表盘首页
- 全局路由守卫（未登录自动跳转 `/login`）
- 请求拦截器注入 `admin-token`

### frontend-user

基于 Vue 3 + Ant Design Vue 的用户前台：

- 用户注册、登录、登出
- 博客首页
- 请求拦截器注入 `user-token`
- 无强制登录守卫（支持游客浏览）

## 双账号体系设计

项目采用 Sa-Token 的 **StpLogic 多账号体系** 实现 admin/user 隔离：

```java
public class StpKit {
    public static final String TYPE_ADMIN = "admin";
    public static final String TYPE_USER  = "user";

    public static final StpLogic ADMIN = new StpLogic(TYPE_ADMIN);
    public static final StpLogic USER  = new StpLogic(TYPE_USER);
}
```

- 两套账号各自独立的 Token、Session、踢人下线逻辑互不干扰
- 请求 Header：`admin-token` 与 `user-token` 分别对应两套体系
- 一个自然人可以同时以 admin 与 user 身份登录，互不影响

## 认证与授权流程

```
 用户                     前端                     后端
  │                        │                        │
  │  填写账号密码          │                        │
  │────────────────────────>                        │
  │                        │ POST /user/login       │
  │                        │────────────────────────>
  │                        │                        │ 校验 + MD5 加盐
  │                        │                        │ StpKit.USER.login()
  │                        │ LoginUserVO + token    │
  │                        │<────────────────────────
  │                        │ 写入 Pinia + localStorage
  │                        │                        │
  │  后续请求              │                        │
  │────────────────────────>                        │
  │                        │ header: user-token=... │
  │                        │────────────────────────>
  │                        │                        │ Sa-Token 校验
  │                        │       业务响应         │
  │                        │<────────────────────────
```

- Token 类型：UUID
- Token 有效期：7 天（timeout）
- 活跃有效期：3 天（active-timeout，超期需重新登录）
- 密码加密：MD5 + 固定盐 `"lilac"`（后续可升级为 BCrypt）

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

| code   | 含义          |
| ------ | ------------- |
| 200    | 成功          |
| 400001 | 未登录 / 登录失效 |
| 400002 | 参数错误      |
| 400003 | 无权限        |
| 400004 | 资源不存在    |
| 400005 | 禁止访问      |
| 400006 | 操作失败      |
| 500    | 系统异常      |

前端请求拦截器统一处理 `401` 与 `400001`：清除本地登录态，admin 侧自动跳转登录页。

## 跨域策略

后端 `CorsConfig` 放开全部来源以便本地开发：

- `allowedOriginPatterns = "*"`
- `allowCredentials = true`
- 允许方法：GET / POST / PUT / DELETE / OPTIONS

**生产部署需收紧为显式白名单。**

## 数据持久化

- ORM：MyBatis-Plus 3.5.15
- 逻辑删除：`isDelete` 字段，`0 = 正常 / 1 = 已删`
- 字段策略：`FieldStrategy.NOT_NULL`
- 主键策略：`IdType.AUTO`
- 详细表结构见 [DATABASE.md](./DATABASE.md)

## 技术选型理由

| 维度       | 选型                   | 理由                                       |
| ---------- | ---------------------- | ------------------------------------------ |
| 后端框架   | Spring Boot 3.5 + JDK 17 | 主流稳定、生态完善、原生支持 Jakarta EE   |
| 认证       | Sa-Token               | 轻量、开箱即用双账号体系、国产文档友好    |
| ORM        | MyBatis-Plus           | CRUD 便利、保留自定义 SQL 灵活性          |
| 前端框架   | Vue 3 + TS + Vite      | Composition API、构建速度快、类型安全     |
| UI 库      | Ant Design Vue 4.x     | 企业级组件丰富、中文文档完善              |
| 状态管理   | Pinia                  | Vue 官方推荐、API 简洁                    |
| API 生成   | @umijs/openapi         | 后端 OpenAPI → 前端 TS 客户端自动生成      |

## 后续演进方向

- [ ] 文章模块（草稿、发布、分类、标签）
- [ ] 评论与点赞
- [ ] 文件 / 图片上传（OSS）
- [ ] Redis 缓存热点数据
- [ ] 日志中心化（ELK / Loki）
- [ ] CI/CD 与容器化部署
