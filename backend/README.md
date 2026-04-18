<h1 align="center">🪴 Lilac Blog Backend</h1>

<p align="center">
  基于 <b>Spring Boot 3</b> + <b>MyBatis-Plus</b> + <b>Sa-Token</b> 的博客后端服务
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?logo=springboot&logoColor=white">
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.8+-C71A36?logo=apachemaven&logoColor=white">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white">
</p>

---

## 📖 简介

Lilac Blog 后端承担所有业务逻辑与数据持久化，对外提供统一的 REST JSON API，供管理后台与用户前台调用。

## ✨ 特性

- 🔐 **Sa-Token 双账号体系**：`StpKit.ADMIN` / `StpKit.USER` 独立管理两套鉴权，Header 分别为 `admin-token` / `user-token`
- 🧱 **统一响应封装**：`Result<T>` + `GlobalExceptionHandler`，错误码集中枚举
- 🗑️ **逻辑删除**：MyBatis-Plus `isDelete` 字段自动过滤
- 🧰 **代码精简**：Lombok + Hutool + FastJSON 加速开发
- 🔄 **CORS 全开**：本地开发无需代理，生产请按需收紧

## 🛠️ 技术栈

| 组件          | 版本     | 说明               |
| ------------- | -------- | ------------------ |
| JDK           | 17       | 语言               |
| Spring Boot   | 3.5.13   | Web / 依赖注入     |
| MyBatis-Plus  | 3.5.15   | ORM                |
| Sa-Token      | 1.45.0   | 认证授权           |
| MySQL         | 8.x      | 数据库             |
| Hutool        | 5.8.44   | 通用工具           |
| FastJSON      | 1.2.83   | JSON 序列化        |
| Lombok        | -        | 样板代码           |

## 📦 目录结构

```
backend/
├── pom.xml
├── src/main/java/com/lilac/
│   ├── BackendApplication.java        # 启动类
│   ├── common/                        # 通用请求体（分页、删除）
│   ├── config/                        # CORS / MyBatis-Plus 配置
│   ├── constant/                      # 常量（角色等）
│   ├── controller/                    # REST 控制器
│   │   ├── AdminController.java
│   │   └── UserController.java
│   ├── domain/
│   │   ├── dto/                       # 入参 DTO
│   │   ├── entity/                    # 数据库实体
│   │   ├── result/                    # 统一响应 Result<T>
│   │   └── vo/                        # 出参 VO
│   ├── enums/                         # HttpsCodeEnum
│   ├── exception/                     # 业务异常 + 全局处理
│   ├── manager/auth/                  # Sa-Token 封装
│   │   ├── StpKit.java                # 双账号体系入口
│   │   └── StpInterfaceImpl.java      # 权限接口实现
│   ├── mapper/                        # MyBatis-Plus Mapper
│   ├── service/impl/                  # Service + 实现
│   └── utils/                         # ThrowUtils 等
└── src/main/resources/
    ├── application.yml                # 主配置
    ├── application-dev.yml            # 开发环境配置（已 gitignore）
    └── mapper/                        # MyBatis XML
```

## 🚀 快速开始

### 前置条件

- JDK **17+**
- Maven **3.8+**
- MySQL **8.x**（本地或远程）

### 配置数据库

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/lilac-blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: <your-username>
    password: <your-password>
```

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
- 健康检查：`curl http://localhost:9090/api/user/logout` 应返回 `400001 未登录`

## 🔑 认证体系

项目通过 `StpKit` 封装 Sa-Token 的 **StpLogic 多账号体系**：

```java
StpKit.USER.login(userId);      // 用户端登录
StpKit.ADMIN.login(adminId);    // 管理端登录

StpKit.USER.checkLogin();       // 检查用户登录态
StpKit.ADMIN.checkLogin();      // 检查管理员登录态
```

请求头约定：

| 终端     | Header 名       |
| -------- | --------------- |
| 用户前台 | `user-token`    |
| 管理后台 | `admin-token`   |

Token 参数（`application.yml`）：

- 类型：UUID
- 超时：7 天（`timeout: 604800`）
- 活跃超时：3 天（`active-timeout: 259200`）

## 📡 API 列表

### 用户模块

| 方法   | 路径                   | 说明     | 鉴权 |
| ------ | ---------------------- | -------- | ---- |
| POST   | `/api/user/register`   | 用户注册 | 否   |
| POST   | `/api/user/login`      | 用户登录 | 否   |
| GET    | `/api/user/logout`     | 用户登出 | 是   |

### 管理员模块

| 方法   | 路径                   | 说明     | 鉴权 |
| ------ | ---------------------- | -------- | ---- |
| POST   | `/api/admin/login`     | 管理员登录 | 否 |
| GET    | `/api/admin/logout`    | 管理员登出 | 是 |

完整文档见 [../docs/API.md](../docs/API.md)。

## 🧪 构建命令

| 命令                       | 说明           |
| -------------------------- | -------------- |
| `mvn spring-boot:run`      | 启动开发服务   |
| `mvn test`                 | 运行单元测试   |
| `mvn clean package`        | 打包为 jar     |
| `mvn clean package -DskipTests` | 跳过测试打包 |

## 🔒 安全注意事项

- ⚠️ 密码使用 MD5 + 固定盐（`"lilac"`），**生产环境应升级为 BCrypt / Argon2**
- ⚠️ CORS 当前允许全部来源，**生产环境需配置白名单**
- ⚠️ `application-dev.yml` 含数据库明文密码，已加入 `.gitignore`

## 📚 相关文档

- [系统架构](../docs/ARCHITECTURE.md)
- [API 文档](../docs/API.md)
- [数据库设计](../docs/DATABASE.md)
- [开发指南](../docs/DEVELOPMENT.md)
