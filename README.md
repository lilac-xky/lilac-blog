<h1 align="center">🌸 Lilac Blog</h1>

<p align="center">
  一个基于 <b>Spring Boot 3</b> + <b>Vue 3</b> 的现代化双端博客系统
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?logo=springboot&logoColor=white">
  <img alt="MyBatis Plus" src="https://img.shields.io/badge/MyBatis--Plus-3.5.15-red">
  <img alt="Sa-Token" src="https://img.shields.io/badge/Sa--Token-1.45.0-ff69b4">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3.5-42b883?logo=vuedotjs&logoColor=white">
  <img alt="Vite" src="https://img.shields.io/badge/Vite-8.0-646cff?logo=vite&logoColor=white">
  <img alt="Ant Design Vue" src="https://img.shields.io/badge/Ant%20Design%20Vue-4.2-1890ff">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-blue.svg">
</p>

---

## 📖 简介

**Lilac Blog** 是一个前后端分离的博客平台，由三个独立模块组成：

- 🪴 **backend** — 基于 Spring Boot 3 的 REST API 服务
- 🛠️ **frontend-admin** — 面向博主的管理后台
- 🌐 **frontend-user** — 面向访客的博客前台

项目采用 **Sa-Token 双账号体系**，让 admin 与 user 的鉴权互相隔离；前端通过 OpenAPI 自动同步后端类型，全栈 TypeScript 友好。

## ✨ 特性

- 🔐 **双账号体系**：管理员 / 普通用户分别拥有独立 Token、互不影响
- 🧩 **前后端分离**：后端仅提供 JSON API，前端自由部署
- 📦 **OpenAPI 打通**：后端接口变更 → 前端 TS 客户端一键同步
- 🎨 **开箱即用的 UI**：Ant Design Vue 4 + 自定义主题
- 🚀 **现代化工具链**：Vite 8 + Vue 3.5 + Pinia 3 + TypeScript 6
- 🛡️ **统一异常处理**：全局 `Result<T>` + 错误码枚举
- 🗑️ **逻辑删除**：MyBatis-Plus 自动附加 `isDelete` 过滤条件

## 🏗️ 项目结构

```
lilac-blog/
├── backend/              # Spring Boot 后端
├── frontend-admin/       # Vue 3 管理后台
├── frontend-user/        # Vue 3 用户前台
├── docs/                 # 项目文档
│   ├── ARCHITECTURE.md   # 系统架构
│   ├── API.md            # API 文档
│   ├── DATABASE.md       # 数据库设计
│   └── DEVELOPMENT.md    # 开发指南
└── README.md
```

## 🛠️ 技术栈

### 后端

| 技术         | 版本     | 用途                 |
| ------------ | -------- | -------------------- |
| JDK          | 17       | 运行环境             |
| Spring Boot  | 3.5.13   | Web 框架             |
| MyBatis-Plus | 3.5.15   | ORM                  |
| Sa-Token     | 1.45.0   | 认证 / 授权          |
| MySQL        | 8.x      | 数据库               |
| Hutool       | 5.8.44   | 工具库               |
| Lombok       | -        | 样板代码生成         |

### 前端

| 技术            | 版本   | 用途          |
| --------------- | ------ | ------------- |
| Vue             | 3.5.31 | 框架          |
| Vite            | 8.0.3  | 构建工具      |
| TypeScript      | 6.0    | 类型系统      |
| Ant Design Vue  | 4.2.6  | UI 组件库     |
| Pinia           | 3.0.4  | 状态管理      |
| Vue Router      | 5.0.4  | 路由          |
| Axios           | 1.15   | HTTP 客户端   |
| @umijs/openapi  | 1.14   | API 类型生成  |

## 🚀 快速开始

### 前置条件

- JDK **17+**
- Maven **3.8+**
- Node.js **≥ 20.19** 或 **≥ 22.12**
- MySQL **8.x**

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd lilac-blog
```

### 2. 初始化数据库

```sql
CREATE DATABASE `lilac-blog` DEFAULT CHARSET utf8mb4;
```

执行 [docs/DATABASE.md](./docs/DATABASE.md) 中的建表语句。

### 3. 启动后端

```bash
cd backend
# 修改 src/main/resources/application-dev.yml 中的数据库账号密码
mvn spring-boot:run
```

默认端口 `9090`，上下文路径 `/api`。

### 4. 启动管理后台

```bash
cd frontend-admin
npm install
npm run dev
```

### 5. 启动用户前台

```bash
cd frontend-user
npm install
npm run dev
```

## 📚 文档

完整文档请查看 [docs/](./docs/) 目录：

- [系统架构](./docs/ARCHITECTURE.md)
- [API 文档](./docs/API.md)
- [数据库设计](./docs/DATABASE.md)
- [开发指南](./docs/DEVELOPMENT.md)

各模块自身的 README：

- [backend/README.md](./backend/README.md)
- [frontend-admin/README.md](./frontend-admin/README.md)
- [frontend-user/README.md](./frontend-user/README.md)

## 🗺️ Roadmap

- [x] 用户 / 管理员认证体系
- [x] 管理后台、用户前台脚手架
- [ ] 文章模块（CRUD、草稿、发布）
- [ ] 分类与标签
- [ ] 评论与点赞
- [ ] 文件 / 图片上传（OSS）
- [ ] Redis 缓存层
- [ ] 容器化与 CI/CD

## 🤝 贡献

欢迎提交 Issue 与 PR！提交前请先阅读 [开发指南](./docs/DEVELOPMENT.md)。

## 📄 License

[MIT](./LICENSE) © Lilac
