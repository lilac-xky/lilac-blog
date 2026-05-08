<h1 align="center">🛠️ Lilac Blog Admin</h1>

<p align="center">
  Lilac Blog 管理后台 — 基于 <b>Vue 3</b> + <b>Vite</b> + <b>Ant Design Vue</b>
</p>

<p align="center">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3.5-42b883?logo=vuedotjs&logoColor=white">
  <img alt="Vite" src="https://img.shields.io/badge/Vite-8.0-646cff?logo=vite&logoColor=white">
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-6.0-3178C6?logo=typescript&logoColor=white">
  <img alt="Ant Design Vue" src="https://img.shields.io/badge/Ant%20Design%20Vue-4.2-1890ff">
  <img alt="Pinia" src="https://img.shields.io/badge/Pinia-3.0-ffd859">
</p>

---

## 📖 简介

Lilac Blog 的管理员后台，用于站点内容管理。当前已支持：管理员登录与守卫、用户管理、文章发布与管理（Markdown 编辑器）、分类与标签管理、文件上传。

## ✨ 特性

- ⚡ **Vite 8** 极速冷启动与 HMR
- 🎨 **Ant Design Vue 4** 企业级 UI，自定义主题
- ✍️ **md-editor-v3** Markdown 编辑器，支持图片上传、预览
- 🔐 **路由守卫**：未登录跳转 `/login?redirect=<from>`，并通过 `/admin/currentUser` 远端校验 token
- 🧾 **Pinia 状态持久化**：登录信息写入 `localStorage("lilac-blog-admin")`
- 🔄 **OpenAPI 类型生成**：一键同步后端接口到 `src/api/`
- 🧠 **TypeScript 严格模式** + `@/*` 路径别名

## 🛠️ 技术栈

| 依赖              | 版本    | 用途              |
| ----------------- | ------- | ----------------- |
| vue               | 3.5.31  | 框架              |
| vite              | 8.0.3   | 构建工具          |
| typescript        | 6.0     | 类型系统          |
| ant-design-vue    | 4.2.6   | UI 组件库         |
| md-editor-v3      | 6.5.0   | Markdown 编辑器   |
| pinia             | 3.0.4   | 状态管理          |
| vue-router        | 5.0.4   | 路由              |
| axios             | 1.15    | HTTP 客户端       |
| @umijs/openapi    | 1.14    | API 生成          |

## 📦 目录结构

```
frontend-admin/
├── src/
│   ├── main.ts                       # 入口：注册 Pinia / Router / AntD
│   ├── App.vue                       # 根组件，主题配置
│   ├── request.ts                    # Axios 实例（注入 admin-token）
│   ├── api/                          # 由 OpenAPI 生成的接口客户端
│   │   ├── adminController.ts
│   │   ├── userController.ts
│   │   ├── articleController.ts
│   │   ├── categoryController.ts
│   │   ├── tagController.ts
│   │   ├── fileController.ts
│   │   ├── index.ts
│   │   └── typings.d.ts
│   ├── assets/
│   ├── components/
│   │   ├── GlobalHeader.vue          # 顶部栏
│   │   └── GlobalSider.vue           # 侧边栏（含菜单）
│   ├── layouts/
│   │   └── BasicLayout.vue           # 登录后主布局
│   ├── router/
│   │   └── index.ts                  # 路由 + 全局守卫（远端校验 token）
│   ├── stores/
│   │   └── user.ts                   # 当前登录用户（Pinia）
│   └── views/
│       ├── Home.vue                  # 仪表盘
│       ├── login/Login.vue           # 管理员登录
│       ├── user/UserManagement.vue   # 用户管理
│       ├── article/ArticleManagement.vue  # 文章列表 / 审核
│       ├── article/WriteBlog.vue          # 写博客（Markdown）
│       ├── category/CategoryManagement.vue # 分类管理
│       └── tag/TagManagement.vue           # 标签管理
├── openapi.config.js                 # OpenAPI 生成配置
├── vite.config.ts
├── tsconfig.*.json
└── package.json
```

## 🧭 路由总览

| 路径               | 页面                  | 说明           |
| ------------------ | --------------------- | -------------- |
| `/login`           | Login.vue             | 管理员登录     |
| `/`                | Home.vue              | 仪表盘         |
| `/user/manage`     | UserManagement.vue    | 用户管理       |
| `/article/manage`  | ArticleManagement.vue | 文章管理       |
| `/write-blog`      | WriteBlog.vue         | Markdown 写作  |
| `/blog/category`   | CategoryManagement.vue| 分类管理       |
| `/blog/tag`        | TagManagement.vue     | 标签管理       |

## 🚀 快速开始

### 前置条件

- Node.js **≥ 20.19** 或 **≥ 22.12**
- 已启动的后端服务（默认 `http://localhost:9090`）

### 安装

```bash
npm install
```

### 开发

```bash
npm run dev
```

默认运行在 `http://localhost:5173`。

### 生产构建

```bash
npm run build
```

产物位于 `dist/`。

### 预览生产构建

```bash
npm run preview
```

## 📜 可用脚本

| 脚本                 | 说明                                     |
| -------------------- | ---------------------------------------- |
| `npm run dev`        | 启动本地开发服务                         |
| `npm run build`      | 类型检查 + 生产构建                      |
| `npm run build-only` | 仅构建，不做类型检查                     |
| `npm run type-check` | 运行 `vue-tsc --build`                   |
| `npm run preview`    | 本地预览生产构建                         |
| `npm run openapi`    | 根据后端 OpenAPI 规范重新生成 `src/api/` |

## 🔐 登录约定

- 请求头：`admin-token: <uuid>`（由 `src/request.ts` 自动注入）
- 登录态存储：Pinia + `localStorage` key `"lilac-blog-admin"`
- 启动后首次导航会调用 `/admin/currentUser` 校验 token，失败则清除登录态并跳转 `/login`
- 认证失败码：`401` / `400001`，拦截器自动清空登录态

## 🌐 与后端的连接

- baseURL：`http://localhost:9090`（见 [src/request.ts](src/request.ts)）
- 接口契约由后端 OpenAPI 文档生成，见 `openapi.config.js`
- 文件上传走 `/api/file/upload`（`type=avatar | cover | content`）

## 🎨 主题

应用级主题通过 Ant Design Vue 的 `ConfigProvider` 设置（见 [App.vue](src/App.vue)）：

- 主色：`#1a1a1a`
- 圆角：`10px`
- 语言：`zh_CN`

## 🧑‍💻 推荐 IDE

[VS Code](https://code.visualstudio.com/) + [Vue Official (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.volar)（请禁用 Vetur）。

## 📚 相关文档

- [项目总览](../README.md)
- [API 文档](../docs/API.md)
- [开发指南](../docs/DEVELOPMENT.md)
