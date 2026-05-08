# 数据库设计

- **数据库**：MySQL 8.x
- **字符集**：utf8mb4 / utf8mb4_unicode_ci
- **命名**：表名小写单数，字段小驼峰（与实体类保持一致）
- **公共字段**：所有业务表均含 `createTime` / `updateTime` / `editTime` / `isDeleted`
- **删除策略**：逻辑删除（`isDeleted`），由 MyBatis-Plus 自动追加过滤条件
- **主键**：`IdType.ASSIGN_ID`（雪花 ID，长整型）

## 表清单

| 表名         | 说明                              |
| ------------ | --------------------------------- |
| user         | 用户（含管理员，按 `role` 区分）  |
| article      | 文章                              |
| category     | 文章分类                          |
| tag          | 文章标签                          |
| article_tag  | 文章 ↔ 标签 多对多关联表          |

---

## user 表

用户表统一存储管理员与普通用户，通过 `role` 字段区分。

| 字段名        | 类型          | 约束                      | 说明                            |
| ------------- | ------------- | ------------------------- | ------------------------------- |
| `id`          | BIGINT        | PK                        | 主键（雪花 ID）                 |
| `userAccount` | VARCHAR(50)   | UNIQUE, NOT NULL          | 账号                            |
| `email`       | VARCHAR(100)  |                           | 邮箱                            |
| `password`    | VARCHAR(255)  | NOT NULL                  | 密码：`MD5("lilac" + 明文)`     |
| `username`    | VARCHAR(50)   | DEFAULT '用户'            | 昵称                            |
| `avatar`      | VARCHAR(255)  |                           | 头像 URL                        |
| `role`        | VARCHAR(20)   | DEFAULT 'user'            | `admin` / `user`                |
| `status`      | TINYINT       | DEFAULT 1                 | `1` 正常 / `0` 异常             |
| `createTime`  | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP | 创建时间                        |
| `updateTime`  | TIMESTAMP     | ON UPDATE CURRENT_TIMESTAMP | 更新时间                      |
| `editTime`    | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP | 业务侧主动编辑时间              |
| `isDeleted`   | TINYINT       | DEFAULT 0                 | 逻辑删除：`0` 正常 / `1` 已删   |

```sql
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL,
  `userAccount` VARCHAR(50)  NOT NULL COMMENT '账号',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password`    VARCHAR(255) NOT NULL COMMENT '密码（MD5 加盐）',
  `username`    VARCHAR(50)  DEFAULT '用户' COMMENT '昵称',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `role`        VARCHAR(20)  DEFAULT 'user' COMMENT '角色：admin / user',
  `status`      TINYINT      DEFAULT 1 COMMENT '1 正常 / 0 异常',
  `createTime`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `updateTime`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `editTime`    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `isDeleted`   TINYINT      DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_account` (`userAccount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 初始化数据

```sql
-- 管理员账号：admin / 12345678
INSERT INTO `user` (`id`, `userAccount`, `password`, `username`, `role`)
VALUES (1, 'admin', MD5(CONCAT('lilac', '12345678')), '管理员', 'admin');
```

---

## article 表

文章表，支持草稿 / 待审核 / 已发布三态，关联分类与标签。

| 字段名        | 类型          | 约束                      | 说明                            |
| ------------- | ------------- | ------------------------- | ------------------------------- |
| `id`          | BIGINT        | PK                        | 主键                            |
| `title`       | VARCHAR(255)  | NOT NULL                  | 标题                            |
| `summary`     | VARCHAR(500)  |                           | 摘要                            |
| `content`     | LONGTEXT      | NOT NULL                  | Markdown 正文                   |
| `categoryId`  | BIGINT        |                           | 分类 ID                         |
| `coverUrl`    | VARCHAR(255)  |                           | 封面图 URL（WebP 缩略图）       |
| `userId`      | BIGINT        |                           | 投稿人 ID                       |
| `viewCount`   | INT           | DEFAULT 0                 | 浏览量                          |
| `isTop`       | TINYINT       | DEFAULT 0                 | `1` 置顶 / `0` 普通             |
| `status`      | TINYINT       | DEFAULT 0                 | `0` 草稿 / `1` 待审核 / `2` 已发布 |
| `createTime`  | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP | 创建时间                        |
| `updateTime`  | TIMESTAMP     | ON UPDATE CURRENT_TIMESTAMP | 更新时间                      |
| `editTime`    | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP | 业务侧编辑时间                  |
| `isDeleted`   | TINYINT       | DEFAULT 0                 | 逻辑删除                        |

```sql
CREATE TABLE `article` (
  `id`          BIGINT       NOT NULL,
  `title`       VARCHAR(255) NOT NULL COMMENT '标题',
  `summary`     VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  `content`     LONGTEXT     NOT NULL COMMENT 'Markdown 正文',
  `categoryId`  BIGINT       DEFAULT NULL COMMENT '分类 ID',
  `coverUrl`    VARCHAR(255) DEFAULT NULL COMMENT '封面 URL',
  `userId`      BIGINT       DEFAULT NULL COMMENT '投稿人 ID',
  `viewCount`   INT          DEFAULT 0 COMMENT '浏览量',
  `isTop`       TINYINT      DEFAULT 0 COMMENT '1 置顶 / 0 普通',
  `status`      TINYINT      DEFAULT 0 COMMENT '0 草稿 / 1 待审 / 2 已发布',
  `createTime`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `updateTime`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `editTime`    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `isDeleted`   TINYINT      DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`categoryId`),
  KEY `idx_user` (`userId`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';
```

---

## category 表

| 字段名         | 类型         | 说明     |
| -------------- | ------------ | -------- |
| `id`           | BIGINT, PK   | 主键     |
| `categoryName` | VARCHAR(50)  | 分类名   |
| `createTime`   | TIMESTAMP    | 创建时间 |
| `updateTime`   | TIMESTAMP    | 更新时间 |
| `editTime`     | TIMESTAMP    | 编辑时间 |
| `isDeleted`    | TINYINT      | 逻辑删除 |

```sql
CREATE TABLE `category` (
  `id`           BIGINT       NOT NULL,
  `categoryName` VARCHAR(50)  NOT NULL,
  `createTime`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `updateTime`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `editTime`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `isDeleted`    TINYINT      DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`categoryName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';
```

---

## tag 表

| 字段名      | 类型         | 说明     |
| ----------- | ------------ | -------- |
| `id`        | BIGINT, PK   | 主键     |
| `tagName`   | VARCHAR(50)  | 标签名   |
| `createTime`| TIMESTAMP    | 创建时间 |
| `updateTime`| TIMESTAMP    | 更新时间 |
| `editTime`  | TIMESTAMP    | 编辑时间 |
| `isDeleted` | TINYINT      | 逻辑删除 |

```sql
CREATE TABLE `tag` (
  `id`         BIGINT       NOT NULL,
  `tagName`    VARCHAR(50)  NOT NULL,
  `createTime` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `updateTime` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `editTime`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
  `isDeleted`  TINYINT      DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tagName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';
```

---

## article_tag 表

文章 ↔ 标签 多对多关联（**该表不参与逻辑删除**，删除文章时直接物理清理对应记录）。

| 字段名      | 类型       | 说明        |
| ----------- | ---------- | ----------- |
| `id`        | BIGINT, PK, AUTO_INCREMENT | 主键 |
| `articleId` | BIGINT     | 文章 ID     |
| `tagId`     | BIGINT     | 标签 ID     |

```sql
CREATE TABLE `article_tag` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT,
  `articleId` BIGINT NOT NULL,
  `tagId`     BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_article` (`articleId`),
  KEY `idx_tag` (`tagId`),
  UNIQUE KEY `uk_article_tag` (`articleId`, `tagId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章-标签关联';
```

---

## 约定

### 密码存储

- 算法：`MD5(salt + password)`
- 盐值：固定字符串 `"lilac"`（见 `UserServiceImpl`）

> ⚠️ MD5 不是足够安全的密码哈希算法，生产环境建议迁移至 **BCrypt** 或 **Argon2**。

### 逻辑删除

- 字段：`isDeleted`
- 值：`0` 正常，`1` 已删
- 由 MyBatis-Plus 通过 `application.yml` 中 `logic-delete-field: isDeleted` 自动处理；所有查询默认追加 `isDeleted = 0`

### 时间字段

| 字段         | 何时更新                          |
| ------------ | --------------------------------- |
| `createTime` | 记录创建时（数据库默认值）        |
| `updateTime` | 任意字段变更时（数据库 ON UPDATE）|
| `editTime`   | 业务层用户主动编辑时              |

### 主键策略

- MyBatis-Plus `IdType.ASSIGN_ID`（雪花 ID）
- 雪花 ID 数值通常超过 JS `Number.MAX_SAFE_INTEGER`，前端使用 `json-bigint` 安全解析

## 后续规划

| 表名        | 用途             |
| ----------- | ---------------- |
| `comment`   | 评论             |
| `like_log`  | 点赞记录         |
| `view_log`  | 浏览记录（可选） |
| `friend_link` | 友链           |
