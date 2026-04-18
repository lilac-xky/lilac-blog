# 数据库设计

- **数据库**：MySQL 8.x
- **字符集**：utf8mb4 / utf8mb4_unicode_ci
- **命名**：表名小写单数，字段小驼峰（与实体类保持一致）
- **规范**：所有业务表均包含 `creatTime` / `updateTime` / `isDelete` 三个公共字段，采用 **逻辑删除**

## 表清单

| 表名   | 说明         |
| ------ | ------------ |
| user   | 用户（含管理员） |

> 当前项目处于 MVP 阶段，仅实现用户账号体系；后续文章、评论等模块的表结构会在此文档追加。

---

## user 表

用户表统一存储管理员与普通用户，通过 `role` 字段区分。

| 字段名        | 类型          | 约束                   | 说明                           |
| ------------- | ------------- | ---------------------- | ------------------------------ |
| `id`          | BIGINT        | PK, AUTO_INCREMENT     | 主键                           |
| `userAccount` | VARCHAR(50)   | UNIQUE, NOT NULL       | 账号，登录唯一标识             |
| `email`       | VARCHAR(100)  |                        | 邮箱                           |
| `password`    | VARCHAR(255)  | NOT NULL               | 密码，`MD5("lilac" + 明文)`    |
| `username`    | VARCHAR(50)   | DEFAULT '用户'         | 昵称，默认 "用户"              |
| `avater`      | VARCHAR(255)  |                        | 头像 URL（字段保留原拼写）     |
| `role`        | VARCHAR(20)   | DEFAULT 'user'         | 角色：`admin` / `user`         |
| `status`      | TINYINT       | DEFAULT 1              | 状态：`1` 正常 / `0` 禁用      |
| `creatTime`   | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP | 创建时间                    |
| `updateTime`  | TIMESTAMP     | ON UPDATE CURRENT_TIMESTAMP | 更新时间                  |
| `editTime`    | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP | 用户主动修改时间            |
| `isDelete`    | TINYINT       | DEFAULT 0              | 逻辑删除：`0` 正常 / `1` 已删  |

### 建表语句

```sql
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 初始化数据

```sql
-- 管理员账号：admin / 12345678
INSERT INTO `user` (`userAccount`, `password`, `username`, `role`)
VALUES ('admin', MD5(CONCAT('lilac', '12345678')), '管理员', 'admin');
```

## 约定

### 密码存储

- 算法：`MD5(salt + password)`
- 盐值：固定字符串 `"lilac"`（见 `UserServiceImpl`）

> ⚠️ MD5 不是足够安全的密码哈希算法，生产环境建议迁移至 **BCrypt** 或 **Argon2**。

### 逻辑删除

- 字段：`isDelete`
- 值：`0` = 正常，`1` = 已删除
- 由 MyBatis-Plus 通过 `application.yml` 中的 `logic-delete-field` 自动处理，所有查询默认附加 `isDelete = 0` 条件

### 时间字段

| 字段         | 何时更新                       |
| ------------ | ------------------------------ |
| `creatTime`  | 记录创建时                     |
| `updateTime` | 每次任意字段变更时（数据库触发）|
| `editTime`   | 业务层用户主动编辑时           |

## 后续规划

MVP 之后预计新增以下表（字段设计待细化）：

- `article` 文章
- `category` 分类
- `tag` 标签
- `article_tag` 文章-标签关联
- `comment` 评论
- `like_log` 点赞记录
