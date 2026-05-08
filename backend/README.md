<h1 align="center">🪴 Lilac Blog Backend</h1>

<p align="center">
  基于 <b>Spring Boot 3</b> + <b>MyBatis-Plus</b> + <b>Sa-Token</b> + <b>Redis</b> 的博客后端服务
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?logo=springboot&logoColor=white">
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.8+-C71A36?logo=apachemaven&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white">
  <img alt="Redis" src="https://img.shields.io/badge/Redis-7.x-DC382D?logo=redis&logoColor=white">
</p>

---

## 📖 简介

Lilac Blog 后端承担所有业务逻辑与数据持久化，对外提供统一的 REST JSON API，供管理后台与用户前台调用。当前已实现：用户/管理员认证、文章模块（含分类与标签）、邮箱验证码注册、阿里云 OSS 上传。

## ✨ 特性

- 🔐 **Sa-Token 双账号体系**：`StpKit.ADMIN` / `StpKit.USER` 独立管理两套鉴权，Header 分别为 `admin-token` / `user-token`
- 🧷 **Redis 会话持久化**：`sa-token-redis-jackson` 接管会话存储，重启后登录态保留
- 📝 **文章模块**：文章 + 分类 + 标签三表 + 中间表 `article_tag`，支持草稿 / 待审核 / 已发布
- 📧 **邮箱验证码**：`spring-boot-starter-mail` + 异步发送，注册需 6 位验证码校验
- ☁️ **OSS 抽象层**：`FileStorageService` 封装本地 / 阿里云 OSS 两套实现，按 `storage.type` 切换
- 🧱 **统一响应封装**：`Result<T>` + `GlobalExceptionHandler`，错误码集中枚举
- 🗑️ **逻辑删除**：MyBatis-Plus `isDeleted` 字段自动过滤
- 🧰 **代码精简**：Lombok + Hutool + FastJSON 加速开发
- 🔄 **CORS 全开**：本地开发无需代理，生产请按需收紧

## 🛠️ 技术栈

| 组件                   | 版本     | 说明               |
| ---------------------- | -------- | ------------------ |
| JDK                    | 17       | 语言               |
| Spring Boot            | 3.5.13   | Web / 依赖注入     |
| MyBatis-Plus           | 3.5.15   | ORM                |
| Sa-Token               | 1.45.0   | 认证授权           |
| sa-token-redis-jackson | 1.45.0   | 会话持久化（Redis）|
| Spring Data Redis      | -        | Redis 客户端       |
| Spring Boot Mail       | -        | SMTP 邮件          |
| Aliyun OSS SDK         | 3.18.4   | 对象存储           |
| MySQL                  | 8.x      | 数据库             |
| Redis                  | 7.x      | 缓存 / 会话        |
| Hutool                 | 5.8.44   | 通用工具           |
| FastJSON               | 1.2.83   | JSON 序列化        |
| Lombok                 | -        | 样板代码           |

## 📦 目录结构

```
backend/
├── pom.xml
├── src/main/java/com/lilac/
│   ├── BackendApplication.java        # 启动类
│   ├── common/                        # PageRequest / DeleteRequest
│   ├── config/                        # CORS / MyBatis-Plus / Async / OSS / Storage / Jackson
│   ├── constant/                      # UserConstant / FileConstant
│   ├── controller/
│   │   ├── AdminController.java       # 管理员相关
│   │   ├── UserController.java        # 用户相关
│   │   ├── ArticleController.java     # 文章
│   │   ├── CategoryController.java    # 分类
│   │   ├── TagController.java         # 标签
│   │   └── FileController.java        # 文件上传
│   ├── domain/
│   │   ├── dto/                       # 入参 DTO（user/article/category/tag/file）
│   │   ├── entity/                    # User / Article / ArticleTag / Category / Tag
│   │   ├── result/                    # 统一响应 Result<T>
│   │   └── vo/                        # LoginUserVO / UserVO / ArticleVO / CategoryVO / TagVO
│   ├── enums/                         # HttpsCodeEnum / UploadTypeEnum
│   ├── exception/                     # 业务异常 + 全局处理
│   ├── manager/
│   │   ├── auth/                      # StpKit / StpInterfaceImpl
│   │   ├── email/                     # MailService
│   │   └── upload/                    # FileStorageService / OSS / Local 实现
│   ├── mapper/                        # MyBatis-Plus Mapper
│   ├── service/impl/                  # Service + 实现
│   └── utils/                         # ThrowUtils 等
└── src/main/resources/
    ├── application.yml                # 主配置
    ├── application-dev.yml            # 开发环境（已 gitignore）
    ├── application-prod.yml           # 生产环境（已 gitignore）
    └── mapper/                        # MyBatis XML（user/article/category/tag/article_tag）
```

## 🚀 快速开始

### 前置条件

- JDK **17+**
- Maven **3.8+**
- MySQL **8.x**（本地或远程）
- Redis **7.x**（用于 Sa-Token 会话）

### 配置 `application-dev.yml`

```yaml
server:
  port: 9090

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/lilac-blog
    username: <your-mysql-username>
    password: <your-mysql-password>
  data:
    redis:
      host: localhost
      port: 6379
      password: <your-redis-password>
  mail:
    host: smtp.qq.com
    port: 587
    username: <your-mailbox>
    password: <your-smtp-authcode>
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

oss:
  client:
    endpoint: oss-cn-beijing.aliyuncs.com
    access-key-id: <your-ak>
    access-key-secret: <your-sk>
    bucket-name: <your-bucket>
    region: cn-beijing
```

> 上传场景默认使用 `oss`，可在 `application.yml` 的 `storage.type` 切换为 `local`。

初始化脚本见 [../docs/DATABASE.md](../docs/DATABASE.md)。

### 启动

```bash
# 方式一：Maven 插件
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

启动成功后：

- 服务端口：`9090`
- 上下文路径：`/api`
- 健康检查：`curl http://localhost:9090/api/user/logout` 应返回 `400001 需要登录`

## 🔑 认证体系

项目通过 `StpKit` 封装 Sa-Token 的 **StpLogic 多账号体系**：

```java
StpKit.USER.login(userId);      // 用户端登录
StpKit.ADMIN.login(adminId);    // 管理端登录

StpKit.USER.checkLogin();       // 检查用户登录态
StpKit.ADMIN.checkLogin();      // 检查管理员登录态
```

请求头约定：

| 终端     | Header 名     |
| -------- | ------------- |
| 用户前台 | `user-token`  |
| 管理后台 | `admin-token` |

Token 参数（`application.yml`）：

- 类型：UUID
- 超时：7 天（`timeout: 604800`）
- 活跃超时：3 天（`active-timeout: 259200`）
- 持久化：Redis（`sa-token-redis-jackson`）

## 📡 API 列表

> 所有接口的统一前缀为 `/api`。

### 用户模块（`/user`）

| 方法 | 路径               | 说明           | 鉴权 |
| ---- | ------------------ | -------------- | ---- |
| POST | `/user/register`   | 用户注册（需验证码） | 否 |
| POST | `/user/login`      | 用户登录       | 否 |
| GET  | `/user/logout`     | 用户登出       | 是 |
| GET  | `/user/sendCode`   | 发送邮箱验证码 | 否 |
| POST | `/user/update`     | 修改个人资料   | 是 |
| POST | `/user/delete`     | 注销自己       | 是 |

### 管理员模块（`/admin`）

| 方法 | 路径                  | 说明         | 鉴权 |
| ---- | --------------------- | ------------ | ---- |
| POST | `/admin/login`        | 管理员登录   | 否 |
| GET  | `/admin/logout`       | 管理员登出   | 是 |
| GET  | `/admin/currentUser`  | 校验 token   | 是 |
| POST | `/admin/list/page/vo` | 用户分页列表 | 是 |
| POST | `/admin/update`       | 修改用户     | 是 |
| POST | `/admin/updateStatus` | 启用 / 禁用  | 是 |
| POST | `/admin/delete`       | 删除用户     | 是 |

### 文章模块（`/article`）

| 方法 | 路径                    | 说明            | 鉴权 |
| ---- | ----------------------- | --------------- | ---- |
| POST | `/article/add`          | 发布文章        | 是 |
| POST | `/article/update`       | 修改文章        | 是 |
| POST | `/article/delete`       | 删除文章        | 是 |
| POST | `/article/list/page`    | 后台分页列表    | 是 |
| POST | `/article/list/page/vo` | 前台分页列表（≤20/页） | 否 |
| GET  | `/article/get`          | 文章详情        | 否 |

### 分类模块（`/category`）

| 方法 | 路径                     | 说明        | 鉴权 |
| ---- | ------------------------ | ----------- | ---- |
| POST | `/category/add`          | 新增分类    | 是 |
| POST | `/category/update`       | 修改分类    | 是 |
| POST | `/category/delete`       | 删除分类    | 是 |
| POST | `/category/list/page`    | 后台列表    | 是 |
| POST | `/category/list/page/vo` | 前台 VO 列表 | 否 |

### 标签模块（`/tag`）

| 方法 | 路径                | 说明        | 鉴权 |
| ---- | ------------------- | ----------- | ---- |
| POST | `/tag/add`          | 新增标签    | 是 |
| POST | `/tag/update`       | 修改标签    | 是 |
| POST | `/tag/delete`       | 删除标签    | 是 |
| POST | `/tag/list/page`    | 后台列表    | 是 |
| POST | `/tag/list/page/vo` | 前台 VO 列表 | 否 |

### 文件模块（`/file`）

| 方法 | 路径                | 说明                           | 鉴权 |
| ---- | ------------------- | ------------------------------ | ---- |
| POST | `/file/upload`      | 上传本地文件（type: avatar/cover/content） | 是 |
| POST | `/file/uploadByUrl` | 通过外链 URL 上传              | 是 |

完整文档见 [../docs/API.md](../docs/API.md)。

## 🧪 构建命令

| 命令                            | 说明             |
| ------------------------------- | ---------------- |
| `mvn spring-boot:run`           | 启动开发服务     |
| `mvn test`                      | 运行单元测试     |
| `mvn clean package`             | 打包为 jar       |
| `mvn clean package -DskipTests` | 跳过测试打包     |

## 🔒 安全注意事项

- ⚠️ 密码使用 MD5 + 固定盐（`"lilac"`），**生产环境应升级为 BCrypt / Argon2**
- ⚠️ CORS 当前允许全部来源，**生产环境需配置白名单**
- ⚠️ `application-dev.yml` / `application-prod.yml` 含数据库、Redis、邮箱、OSS 明文密钥，已加入 `.gitignore`
- ⚠️ OSS AccessKey 建议使用 RAM 子账号 + 最小权限，并在生产部署时迁移到环境变量

## 📚 相关文档

- [系统架构](../docs/ARCHITECTURE.md)
- [API 文档](../docs/API.md)
- [数据库设计](../docs/DATABASE.md)
- [开发指南](../docs/DEVELOPMENT.md)
