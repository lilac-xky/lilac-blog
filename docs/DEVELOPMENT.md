# 开发指南

本文档帮助开发者在本地搭建完整的 Lilac Blog 开发环境。

## 环境要求

| 组件     | 版本要求          | 说明                          |
| -------- | ----------------- | ----------------------------- |
| JDK      | 17+               | 后端运行环境                  |
| Maven    | 3.8+              | 后端构建工具                  |
| Node.js  | ≥ 20.19 或 ≥ 22.12 | 前端运行环境                  |
| npm      | 随 Node 安装      | 或使用 pnpm / yarn            |
| MySQL    | 8.x               | 数据库                        |
| Git      | 最新版            | 版本控制                      |

推荐 IDE：
- 后端：IntelliJ IDEA
- 前端：VS Code + [Vue Official (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.volar) 扩展

## 克隆仓库

```bash
git clone <your-repo-url>
cd lilac-blog
```

## 数据库初始化

1. 创建数据库

```sql
CREATE DATABASE `lilac-blog` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 建表（以 user 表为例，实际以 [DATABASE.md](./DATABASE.md) 为准）

```sql
USE `lilac-blog`;

CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `userAccount` VARCHAR(50)  NOT NULL COMMENT '账号',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password`    VARCHAR(255) NOT NULL COMMENT '密码（MD5 加盐）',
  `username`    VARCHAR(50)  DEFAULT '用户' COMMENT '昵称',
  `avater`      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `role`        VARCHAR(20)  DEFAULT 'user' COMMENT '角色：admin / user',
  `status`      TINYINT      DEFAULT 1 COMMENT '状态：1 正常 / 0 禁用',
  `creatTime`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `updateTime`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `editTime`    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `isDelete`    TINYINT      DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_account` (`userAccount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

3. 插入一个管理员账号（密码是 `12345678` 经 `MD5("lilac" + 密码)` 得到）

```sql
INSERT INTO `user` (`userAccount`, `password`, `username`, `role`)
VALUES ('admin', MD5(CONCAT('lilac', '12345678')), '管理员', 'admin');
```

## 后端启动

1. 修改 `backend/src/main/resources/application-dev.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lilac-blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: <your-username>
    password: <your-password>
```

2. 启动：

```bash
cd backend
mvn spring-boot:run
```

或在 IDEA 中运行 `BackendApplication` 主类。

启动成功后访问 `http://localhost:9090/api` 应能与后端联通。

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

| 模块           | 命令                     | 说明               |
| -------------- | ------------------------ | ------------------ |
| backend        | `mvn spring-boot:run`    | 启动开发服务       |
| backend        | `mvn clean package`      | 打包               |
| frontend-admin | `npm run dev`            | 启动开发服务       |
| frontend-admin | `npm run build`          | 生产构建           |
| frontend-admin | `npm run type-check`     | 类型检查           |
| frontend-admin | `npm run openapi`        | 同步后端 API 类型  |
| frontend-user  | 同 admin                 | —                  |

## OpenAPI 类型同步

前端通过 [@umijs/openapi](https://github.com/umijs/openapi) 根据后端 OpenAPI 规范自动生成 TS 客户端：

```bash
cd frontend-admin
npm run openapi
```

配置见 `openapi.config.js`，生成文件在 `src/api/`。

## 代码规范

- 后端：遵循阿里巴巴 Java 开发手册，Lombok 简化样板代码
- 前端：ESLint + TypeScript 严格模式
- Git 提交信息：建议使用 [Conventional Commits](https://www.conventionalcommits.org/) 约定

## 常见问题

### 启动后端报连接数据库失败

检查：
- MySQL 是否启动
- `application-dev.yml` 中用户名、密码、数据库名是否正确
- 数据库时区是否一致（建议 `serverTimezone=Asia/Shanghai`）

### 前端登录后请求仍然 401

检查：
- `request.ts` 中 `baseURL` 是否与后端一致
- 浏览器 Network 面板中请求头是否携带 `user-token` / `admin-token`
- Token 是否已过期（默认 7 天）

### 跨域问题

后端已全局放开 CORS，若仍出现跨域错误，请确认：
- 是否通过 `http://localhost:xxx` 访问（而非 `127.0.0.1`）
- 浏览器是否禁用了第三方 Cookie
