# 开发指南

本文档帮助开发者在本地搭建完整的 Lilac Blog 开发环境。

## 环境要求

| 组件     | 版本要求            | 说明                          |
| -------- | ------------------- | ----------------------------- |
| JDK      | 17+                 | 后端运行环境                  |
| Maven    | 3.8+                | 后端构建工具                  |
| Node.js  | ≥ 20.19 或 ≥ 22.12  | 前端运行环境                  |
| npm      | 随 Node 安装        | 或使用 pnpm / yarn            |
| MySQL    | 8.x                 | 数据库                        |
| Redis    | 7.x（兼容 6.x）     | Sa-Token 会话持久化           |
| Git      | 最新版              | 版本控制                      |

可选：

- 阿里云 OSS（用于文件上传，可切换为 `local` 模式跳过）
- 一个支持 SMTP 授权码的邮箱（用于注册验证码，比如 QQ 邮箱）

推荐 IDE：
- 后端：IntelliJ IDEA
- 前端：VS Code + [Vue Official (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) 扩展

## 克隆仓库

```bash
git clone <your-repo-url>
cd lilac-blog
```

## 数据库初始化

### 1. 创建数据库

```sql
CREATE DATABASE `lilac-blog` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行建表脚本

完整 DDL 见 [DATABASE.md](./DATABASE.md)。需要建立的表：

- `user`
- `article`
- `category`
- `tag`
- `article_tag`

### 3. 初始化管理员

```sql
INSERT INTO `user` (`id`, `userAccount`, `password`, `username`, `role`)
VALUES (1, 'admin', MD5(CONCAT('lilac', '12345678')), '管理员', 'admin');
-- 账号 admin / 密码 12345678
```

## Redis 启动

```bash
# Windows: 下载 Redis-x64 或 Memurai
redis-server

# macOS / Linux
brew services start redis        # 或 apt / pacman
```

默认监听 `127.0.0.1:6379`，如果设置了密码请同步到 `application-dev.yml`。

## 后端启动

### 1. 配置 `backend/src/main/resources/application-dev.yml`

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
    password: <smtp-authcode>     # 注意是授权码，不是登录密码
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

> 不想用 OSS？把 `application.yml` 中 `storage.type` 改为 `local`，文件会落到运行目录。

### 2. 启动

```bash
cd backend
mvn spring-boot:run
```

或在 IDEA 中运行 `BackendApplication`。启动后访问 `http://localhost:9090/api` 即可联通。

## 前端启动

### 管理后台

```bash
cd frontend-admin
npm install
npm run dev
```

默认运行在 `http://localhost:5173`。

### 用户前台

```bash
cd frontend-user
npm install
npm run dev
```

默认运行在 `http://localhost:5174`（或 Vite 自动分配）。

> 两个前端均直连 `http://localhost:9090`，如需修改见各自 `src/request.ts`。

## 生产构建

### 后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 前端

```bash
cd frontend-admin   # 或 frontend-user
npm run build
```

构建产物位于 `dist/`，可部署至 Nginx / CDN。

## 常用命令速查

| 模块           | 命令                  | 说明              |
| -------------- | --------------------- | ----------------- |
| backend        | `mvn spring-boot:run` | 启动开发服务      |
| backend        | `mvn clean package`   | 打包              |
| frontend-admin | `npm run dev`         | 启动开发服务      |
| frontend-admin | `npm run build`       | 生产构建          |
| frontend-admin | `npm run type-check`  | 类型检查          |
| frontend-admin | `npm run openapi`     | 同步后端 API 类型 |
| frontend-user  | 同 admin（无 openapi） | —                 |

## OpenAPI 类型同步

`frontend-admin` 通过 [@umijs/openapi](https://github.com/umijs/openapi) 根据后端 OpenAPI 规范自动生成 TS 客户端：

```bash
cd frontend-admin
npm run openapi
```

配置见 `openapi.config.js`，生成文件在 `src/api/`。

## 文件上传开发

| `storage.type` | 行为                                                     |
| -------------- | -------------------------------------------------------- |
| `oss`（默认）  | 上传至阿里云 OSS，返回 OSS URL + Style 缩略图            |
| `local`        | 上传至本地磁盘，返回相对路径 URL（仅适合开发调试）       |

上传场景（`type` 参数）：

| 值        | 用途      | OSS 路径前缀 | 缩略图 Style                |
| --------- | --------- | ------------ | --------------------------- |
| `avatar`  | 用户头像  | `avatar/`    | `THUMBNAIL_STYLE_AVATAR`    |
| `cover`   | 文章封面  | `cover/`     | `THUMBNAIL_STYLE_COVER`     |
| `content` | 正文图片  | `content/`   | 无（保留原图）              |

## 邮箱验证码

注册流程依赖 `/api/user/sendCode`：

1. 前端弹窗收集邮箱后调 `sendCode`
2. 后端生成 6 位验证码，存入 Redis（5 分钟有效），异步通过 SMTP 发送
3. 用户填入验证码后调 `register`，服务端从 Redis 取出比对

> 启动时若未配置邮箱，验证码相关接口将无法成功发送邮件。可在 `dev` 阶段绕过：手动从后端日志读取生成的 code。

## 代码规范

- 后端：遵循阿里巴巴 Java 开发手册，Lombok 简化样板代码
- 前端：ESLint + TypeScript 严格模式
- Git 提交信息：建议使用 [Conventional Commits](https://www.conventionalcommits.org/) 约定

## 常见问题

### 启动后端报连接数据库失败

检查：
- MySQL 是否启动
- `application-dev.yml` 中用户名、密码、数据库名是否正确
- 数据库时区可在 URL 里加 `serverTimezone=Asia/Shanghai`

### 启动后端报 Redis 连接失败

- 确认 Redis 服务已起：`redis-cli ping` 应返回 `PONG`
- 检查 `spring.data.redis.password` 是否与实际匹配

### 前端登录后请求仍然 401 / 400001

检查：
- `request.ts` 中 `baseURL` 是否与后端一致
- 浏览器 Network 面板请求头是否携带 `user-token` / `admin-token`
- Token 是否已过期（默认 7 天 / 活跃 3 天）
- Redis 是否被清空（会话存在 Redis 中，flushall 会让所有人退出登录）

### 前端文章 ID 显示为科学计数法 / 截断

- 后端使用雪花 ID（19 位长整型），超过 JS 安全整数范围
- `frontend-user` 已通过 `json-bigint` 解析；`frontend-admin` 走 OpenAPI 生成的客户端，必要时可同样接入

### 跨域问题

后端已全局放开 CORS，若仍报错，确认：
- 是否通过 `http://localhost:xxx` 访问（而非 `127.0.0.1`）
- 浏览器是否禁用第三方 Cookie
- 生产环境部署时记得收紧 `CorsConfig`

### 文件上传失败

- 若使用 OSS：检查 AK/SK、bucket、endpoint 是否正确，bucket 是否可公网访问
- 若临时切到本地：在 `application.yml` 把 `storage.type` 改为 `local`
