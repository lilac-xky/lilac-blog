<h1 align="center">🌐 Lilac Blog User</h1>

<p align="center">
  Lilac Blog 用户前台 — 基于 <b>Vue 3</b> + <b>Vite</b> + <b>Ant Design Vue</b>
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

Lilac Blog 面向访客与注册用户的前台站点，提供博客浏览、用户注册登录、收藏 / 订阅 / 评论等互动能力（部分功能建设中）。

## ✨ 特性

- ⚡ **Vite 8** 极速启动与热更新
- 🎨 **Ant Design Vue 4** + `@ant-design/icons-vue` 图标集
- 🧭 **游客友好**：默认允许未登录浏览，互动功能再校验
- 🧾 **Pinia 状态持久化**：登录信息写入 `localStorage("lilac-blog-user")`
- 🧠 **TypeScript 严格模式** + `@/*` 路径别名
- 🕒 **dayjs** 轻量时间处理

## 🛠️ 技术栈

| 依赖                     | 版本    | 用途         |
| ------------------------ | ------- | ------------ |
| vue                      | 3.5.31  | 框架         |
| vite                     | 8.0.3   | 构建工具     |
| typescript               | 6.0     | 类型系统     |
| ant-design-vue           | 4.2.6   | UI 组件库    |
| @ant-design/icons-vue    | 7.0.1   | 图标         |
| pinia                    | 3.0.4   | 状态管理     |
| vue-router               | 5.0.4   | 路由         |
| axios                    | 1.15    | HTTP 客户端  |
| dayjs                    | 1.11.13 | 时间处理     |

## 📦 目录结构

```
frontend-user/
├── src/
│   ├── main.ts                 # 入口
│   ├── App.vue                 # 根组件
│   ├── request.ts              # Axios 实例（注入 user-token）
│   ├── api/                    # 接口客户端
│   │   ├── userController.ts
│   │   └── typings.d.ts
│   ├── assets/
│   ├── components/             # 通用组件
│   ├── layouts/
│   │   └── BasicLayout.vue     # 主布局
│   ├── router/
│   │   └── index.ts            # 路由
│   ├── stores/
│   │   └── user.ts             # 当前登录用户（Pinia）
│   └── views/
│       ├── Home.vue            # 博客首页
│       └── login/
│           ├── Login.vue       # 登录
│           └── Register.vue    # 注册
├── vite.config.ts
├── tsconfig.*.json
└── package.json
```

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

默认运行在 `http://localhost:5173`（若与管理后台冲突会自动改用其他端口）。

### 生产构建

```bash
npm run build
```

产物位于 `dist/`。

## 📜 可用脚本

| 脚本                 | 说明                       |
| -------------------- | -------------------------- |
| `npm run dev`        | 启动本地开发服务           |
| `npm run build`      | 类型检查 + 生产构建        |
| `npm run build-only` | 仅构建，不做类型检查       |
| `npm run type-check` | 运行 `vue-tsc --build`     |
| `npm run preview`    | 本地预览生产构建           |

## 🔐 登录约定

- 请求头：`user-token: <uuid>`（由 `src/request.ts` 自动注入）
- 登录态存储：Pinia + `localStorage` key `"lilac-blog-user"`
- 认证失败（`401` / `400001`）：拦截器仅清空本地登录态，**不强制跳转**，访客可继续浏览

## 🔁 已对接接口

| 方法   | 路径                     | 说明     |
| ------ | ------------------------ | -------- |
| POST   | `/api/user/register`     | 注册     |
| POST   | `/api/user/login`        | 登录     |
| GET    | `/api/user/logout`       | 登出     |

更多接口详见 [../docs/API.md](../docs/API.md)。

## 🌐 与后端的连接

- baseURL：`http://localhost:9090`（见 [src/request.ts](src/request.ts)）
- `withCredentials: true` — 若使用 Cookie 鉴权可启用
- 超时：`60000ms`

## 🧑‍💻 推荐 IDE

[VS Code](https://code.visualstudio.com/) + [Vue Official (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.volar)（请禁用 Vetur）。

## 📚 相关文档

- [项目总览](../README.md)
- [API 文档](../docs/API.md)
- [开发指南](../docs/DEVELOPMENT.md)
