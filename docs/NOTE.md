# 知识碎片（note）栏目设计方案

> 状态：**后端已实现**，前端待 OpenAPI 生成后再做。
> 目标：把平时学习时零散记录的知识点攒起来，定期一键整理成一篇可发布的博客。

---

## 0. 已确认的决策

| 事项         | 决定                                                             |
| ------------ | ---------------------------------------------------------------- |
| 栏目名       | **碎知**（内部表名 `note`）                                      |
| 可见性       | **不公开，仅本人可见**；所有读写按 `userId` 隔离                  |
| 整理方式     | **A：骨架自动 + 人润色**                                         |
| 记录入口     | 前台用户中心「随手记」+ 后台管理页                                |
| 数据模型     | 新建 `note` 表 + `note_tag` 关联表，独立成栏目                    |
| 标题模板     | 单主题 → `{主题} 学习笔记`；多主题 → `学习速记 · {YYYY-MM}`       |
| 自动化       | **暂不做**（Phase 3 的定时整理延后）                              |

### 实施进度

- [x] **Phase 1 后端**：建表脚本、实体/Mapper/DTO/VO、Service、Controller、鉴权配置
- [x] **Phase 2 后端**：`aggregate` / `compose` / `topic/stat` 三个整理接口 + 骨架构建器单测
- [ ] **前端**：等用 Apifox 导入后端接口生成 OpenAPI 后再开始

---

## 1. 背景与要解决的问题

日常学习产生的知识是**碎片化**的：一句话、一个踩坑、一个命令、一段书摘。它们有三个特征：

- **随时产生**，不适合当场写成长文；
- **单条价值低**，单独成文没人看；
- **天然按主题聚集**，同一个知识点会在不同时间反复记录。

现状里已经有两个相近的东西，但都**不适合**承担这件事：

| 现有能力 | 定位 | 为什么不能直接复用 |
| -------- | ---- | ------------------ |
| `spark`（拾灵集） | 一闪而过的**想法** | 有「一闪 / 酝酿中 / 进行中 / 已完成 / 搁置 / 废弃」这套任务式生命周期，且前台是公开瀑布流。想法要的是"跟进状态"，知识点要的是"沉淀归档"，语义打架 |
| `article` | 成文的**博客** | 是产出物而不是原料，直接当碎片容器会让草稿列表被大量一句话占满 |

所以本方案**新建一个独立栏目** `note`，与 `spark`、`article` 三者职责互不重叠：

```
note（原料）──整理──▶ article（产出）
spark（想法）：独立生命周期，不进 note，也不进 article
```

### 要解决的两个痛点

用户原话：**「手动感觉太累，自动也不太方便」**。

- **手动太累** → 分组、排序、小标题、原文格式化、来源标注、日期戳，这些机械劳动必须自动完成。
- **自动不太方便** → 纯自动生成的文章读起来是清单，没有串联、没有删减、没有结论，等于没写。

本方案的解法是**把这两件事切开**：机器只做**确定性**的部分，人只做**判断性**的部分。

---

## 2. 核心思路：主题即聚合键

碎片本身没有结构，硬做「智能归类」必然不可预测，用户会觉得"AI 又乱来了"。

真正稳定的做法是：**在记录的那一刻就挂上一个主题**，让聚合有确定的锚点。

> **主题（tag） = 聚合键；时间（createTime） = 排序键**

于是「整理」不再是一个需要"猜"的过程，而是一次**确定性的 GROUP BY**：

```
一批碎片
   ↓ 按 tag 分组
   ↓ 组内按 createTime 升序
   ↓ 每组渲染成一个 ## 小节
   ↓ 无 tag 的归入「杂项」
一篇 Markdown 草稿
```

**这一步永远是同一份输入产出同一份输出**，不存在"这次生成得怪怪的"。

而真正需要人的地方——**小节之间的过渡句、删掉重复的、补上总结与结论**——恰好是写起来最有价值、也最能体现个人理解的部分。

### 关于主题字段的复用

不新建 `topic` 表，**直接复用现有 `tag` 表**。理由：

- 语义一致：`Redis` 既是文章标签，也是知识主题，不该有两套；
- 将来可以做「标签聚合页」，同一个 `Redis` 标签下同时列出文章与碎片；
- 少一张表、少一套 CRUD、少一套前端管理页。

约定的关键一条：**整理时按「该碎片关联的第一个 tag」作为主主题分组**，其余 tag 作为副标签保留在数据里，不参与分组。

---

## 3. 数据模型

### 3.1 note 表

```sql
CREATE TABLE `note` (
  `id`         BIGINT       NOT NULL                COMMENT '主键（雪花ID）',
  `userId`     BIGINT       NOT NULL                COMMENT '归属用户ID',
  `content`    TEXT         NOT NULL                COMMENT '知识点正文（核心字段）',
  `insight`    VARCHAR(500) DEFAULT NULL            COMMENT '我的一句话理解（可选，写出理解才算沉淀）',
  `source`     VARCHAR(255) DEFAULT NULL            COMMENT '来源：书名 / 链接 / 课程',
  `status`     TINYINT      NOT NULL DEFAULT 0      COMMENT '0未整理 1已归档进文章',
  `articleId`  BIGINT       DEFAULT NULL            COMMENT '归档到的文章ID',
  `createTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `editTime`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isDeleted`  TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_note_user_status` (`userId`, `status`),
  KEY `idx_note_article` (`articleId`),
  KEY `idx_note_create` (`createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识碎片表';
```

### 3.2 note_tag 表

与 `article_tag` 保持完全一致的结构与约定（**不参与逻辑删除**，删除碎片时物理清理）：

```sql
CREATE TABLE `note_tag` (
  `id`     BIGINT NOT NULL AUTO_INCREMENT,
  `noteId` BIGINT NOT NULL,
  `tagId`  BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_tag` (`noteId`, `tagId`),
  KEY `idx_note_tag_note` (`noteId`),
  KEY `idx_note_tag_tag` (`tagId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='碎片-标签关联';
```

### 3.3 两个字段的取舍说明

- **`insight`（我的理解）为什么存在**：只记原文的碎片等于收藏夹；写下一句自己的话才叫知识。但它**可选**，因为记录成本必须压到 3 秒——记得下来比记得好重要。实际使用时，整理阶段正好是"补 insight"的最佳时机。
- **`status` / `articleId` 为什么存在**：防止同一条碎片被反复整理进不同文章。归档后默认从「待整理」列表中消失，需要时可手动翻出已归档碎片。

---

## 4. 整理算法（确定性骨架生成）

### 4.1 输入

```json
{
  "noteIds": [1001, 1002, 1003],          // 方式一：勾选指定碎片
  "fromTime": "2026-11-01 00:00:00",      // 方式二：按时间区间全量捞
  "toTime":   "2026-11-30 23:59:59",
  "tagIds": [10],                          // 可选：限定主题，留空则不限
  "title": "Redis 学习笔记",               // 可选：留空则由后端生成
  "insertHints": true                      // 可选：是否插入过渡提示注释
}
```

### 4.2 生成规则

1. **取数**：按 `userId` 隔离，只取自己的碎片；默认过滤 `status = 0`（未归档）。
2. **分组**：按每条碎片关联的**第一个 tag** 归组；无 tag 的归入 `杂项`。
3. **排序**：
   - 组间按「组内碎片数」**降序**，把内容最多的主题放在最前；
   - 组内按 `createTime` **升序**，形成一条学习推进的时间线。
4. **渲染**（每个主题一组）：

   ```markdown
   ## {tag名}

   - {content 原文}
     `{createTime: YYYY-MM-DD}` `{source}`
     > {insight}
   ```

   - `content` 内的换行保留为列表项内的换行；
   - 日期与来源用**行内代码**做元信息标签，渲染稳定、不抢正文注意力；
   - `source`、`insight` 为空时对应片段整行省略。

5. **过渡提示**：相邻两个小节之间插入一条 HTML 注释（编辑器可见、渲染时隐藏）：

   ```markdown
   <!-- 写一句过渡：从「Redis」怎么引到「MySQL」；写完删掉这条注释 -->
   ```

6. **标题生成**：单主题 → `{tag名} 学习笔记`；多主题 → `学习速记 · {YYYY-MM}`；用户传了 `title` 则以用户为准。
7. **落库**：由「预览」接口返回 Markdown 不落库；确认后走「生成草稿」接口，创建 `status = 0` 的草稿文章，并回写 `note.status = 1`、`note.articleId`。

### 4.3 一个完整示例

假设 2026 年 11 月记了这么 6 条碎片：

| 内容 | 主题 | 来源 | 理解 | 时间 |
| ---- | ---- | ---- | ---- | ---- |
| 过期时间不是「到点删除」，而是惰性删除 + 定期删除兜底 | Redis | 《Redis 设计与实现》第 8 章 | | 11-08 |
| 主从复制是异步的，故障切换可能丢数据 | Redis | | 所以对一致性敏感的场景不能只依赖主从 | 11-09 |
| 缓存穿透 → 布隆过滤器 / 空值缓存 | Redis | | | 11-12 |
| 联合索引遵循最左前缀，跳过最左列就用不上索引 | MySQL | 极客时间《MySQL 实战》 | | 11-10 |
| EXPLAIN 的 type 从好到坏：const > ref > range > index > ALL | MySQL | | | 11-10 |
| 今天调通了本地 Docker 起 Redis 集群，记录一下 | （无） | | | 11-11 |

点一次「整理成文章」，直接得到下面这份草稿：

```markdown
## Redis

- 过期时间不是「到点删除」，而是惰性删除 + 定期删除兜底。
  `2026-11-08` `《Redis 设计与实现》第 8 章`

- 主从复制是异步的，故障切换可能丢数据。
  `2026-11-09`
  > 所以对一致性敏感的场景不能只依赖主从。

- 缓存穿透 → 布隆过滤器 / 空值缓存。
  `2026-11-12`

<!-- 写一句过渡：从「Redis」怎么引到「MySQL」；写完删掉这条注释 -->

## MySQL

- 联合索引遵循最左前缀，跳过最左列就用不上索引。
  `2026-11-10` `极客时间《MySQL 实战》`

- EXPLAIN 的 type 从好到坏：const > ref > range > index > ALL。
  `2026-11-10`

<!-- 写一句过渡：从「MySQL」怎么引到「杂项」；写完删掉这条注释 -->

## 杂项

- 今天调通了本地 Docker 起 Redis 集群，记录一下。
  `2026-11-11`

<!-- 本节碎片未挂主题，建议补上 tag 后重新整理，或整段删除 -->
```

**接下来人要做的事只有三件，而且都是最有价值的：**

1. 补上两处过渡句（各一句话）；
2. 把 `杂项` 那条补个 tag 或删掉；
3. 在开头写一段「这周学了什么」，在结尾写一段「还有什么没搞懂」。

整个过程从「整理 6 条碎片」变成「写 3 段话」，这就是本方案要的效果。

---

## 5. API 设计

所有接口挂在 `/note` 下，沿用现有的 `Result<T>` 包装与 `ThrowUtils` 参数校验风格。

### 5.1 碎片 CRUD（普通用户，`@SaUserPermission`）

| 方法 | 路径 | 说明 | 权限码 |
| ---- | ---- | ---- | ------ |
| POST | `/note/add` | 随手记一条碎片 | `note:add` |
| POST | `/note/update` | 修改自己的碎片 | `note:update` |
| POST | `/note/delete` | 删除自己的碎片 | `note:delete` |
| POST | `/note/list/page` | 我的碎片分页（按 `userId` 隔离） | `note:list` |
| GET  | `/note/get` | 碎片详情（仅本人） | 登录即可 |

`NoteQueryRequest` 支持：`content`（模糊）、`tagIds`、`status`、`sortOrder`、`current`、`pageSize`。

### 5.2 整理相关

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| POST | `/note/aggregate` | **预览骨架**：返回 Markdown 文本 + 统计，**不落库** |
| POST | `/note/compose` | **确认生成**：创建草稿文章，回写 `note` 归档状态，返回 `articleId` |
| GET  | `/note/topic/stat` | 主题分布统计（各 tag 下待整理碎片数），供整理页做入口 |

`/note/aggregate` 的返回：

```json
{
  "code": 0,
  "data": {
    "title": "学习速记 · 2026-11",
    "markdown": "## Redis\n\n- ...",
    "noteCount": 6,
    "untaggedCount": 1,
    "topics": [
      { "tagId": 10, "tagName": "Redis", "count": 3 },
      { "tagId": 11, "tagName": "MySQL", "count": 2 }
    ]
  }
}
```

### 5.3 后台管理（管理员，`@SaAdminPermission`）

| 方法 | 路径 | 说明 | 权限码 |
| ---- | ---- | ---- | ------ |
| POST | `/note/list/page/admin` | 全量碎片（跨用户） | `note:list:admin` |
| POST | `/note/delete/admin` | 管理员删除任意碎片 | `note:delete:admin` |

> 权限点需要写进 `docs/sql/note.sql`，与现有 `spark:*`、`article:*` 的初始化脚本保持同一格式。

---

## 6. 前端交互

### 6.1 前台：随手记（核心，必须够快）

**入口**（三处，都指向同一个轻量组件 `QuickNote.vue`）：

1. 顶栏用户下拉菜单里加一项「记一笔」；
2. 用户中心加一张快捷入口卡；
3. 全局快捷键 `Ctrl + Alt + N` 直接唤起（登录态下生效）。

**弹层结构**（保持极简，目标 3 秒记完）：

```
┌───────────────────────────────────┐
│  记一笔                      [×]  │
│  ┌─────────────────────────────┐  │
│  │ 一个 textarea，自动聚焦      │  │
│  │ Ctrl+Enter 提交，Esc 关闭    │  │
│  └─────────────────────────────┘  │
│  [主题 ▾]  [来源]  ⌄ 补一句理解    │
│                        [ 记下来 ] │
└───────────────────────────────────┘
```

- 主题、来源、理解默认收在第二行，**不填也能提交**；
- 提交成功后弹层**不关闭**、清空输入、给出"已记下 · 再记一条"的提示，便于连续记录；
- 主题下拉直接复用 `listTagByPageVo`，支持输入即过滤。

**独立的碎片列表页** `/user/notes`：

- 时间线倒序，每条卡片显示内容、日期、来源、标签、是否已归档；
- 顶部筛选：主题 / 未整理 / 已归档；
- 支持勾选 + 「整理成文章」按钮 → 跳整理预览页。

### 6.2 前台：整理预览页 `/user/notes/compose`

这一步是**人的唯一决策点**，需要足够直观：

- 左侧：按主题分组的碎片列表，可取消勾选某条（不参与生成）；
- 右侧：Markdown 实时预览（复用 `md-editor-v3` 的预览组件）；
- 顶部：标题输入框（已按规则预填）+ 「生成草稿」按钮；
- 点击生成 → `POST /note/compose` → 拿到 `articleId` → `router.push('/user/write?id=' + articleId)` **直接进入现有编辑器润色**。

> 复用现有 `WriteArticle.vue` 是整个方案的关键收益点：**不新写编辑器**，碎片整理完直接落进已有的写作流程。

### 6.3 后台：碎片管理页 `/note/manage`（frontend-admin）

- 用现有 `TableCRUD` 组件 + `usePaginationQuery` / `useModalForm` / `useConfirmDelete` 三个 composable，与 `SparkManagement.vue` 结构对齐；
- 列为：ID / 内容（省略号 + tooltip）/ 主题 / 状态 / 归属用户 / 创建时间 / 操作；
- 「灵感管理」下方新增侧边栏菜单项「碎片管理」，权限 `note:list:admin`。

---

## 7. 分期实施

按「最小可用 → 好用 → 智能」推进，每期都能独立交付。

### Phase 1：数据通路跑通（必须）

1. `docs/sql/note.sql`：建表 + 权限点初始化脚本；
2. `docs/DATABASE.md` 补 `note` / `note_tag` 两节；`docs/API.md` 补 `note` 接口；
3. 后端：`Note` / `NoteTag` 实体、Mapper、DTO、VO、`NoteService` / `NoteServiceImpl`、`NoteController`；
4. 前端 `frontend-user`：`noteController.ts` + `typings.d.ts` 补类型（手写，待后端跑起来后用 OpenAPI 重新生成）；
5. **随手记弹层 `QuickNote.vue`** + 顶栏入口 + `/user/notes` 列表页。

**验收**：能记下碎片、能按主题筛选、能在列表里看到。

### Phase 2：一键整理（本方案的核心价值）

1. 后端 `aggregate` / `compose` / `topic/stat` 三个接口，聚合逻辑放 `NoteAggregateService`（纯函数式，方便单测）；
2. 前端整理预览页 `/user/notes/compose`，带勾选与实时预览；
3. 生成后跳转 `WriteArticle.vue`，并在该页顶部显示「来自 N 条碎片整理」的提示条。

**验收**：勾 10 条碎片 → 点整理 → 30 秒后得到一份可发布的草稿。

### Phase 3：后台管理与自动化（可选）

1. 后台 `frontend-admin` 碎片管理页；
2. `@Scheduled` 每周一 08:00 扫描「超过 7 天未整理且碎片数 ≥ 5」的主题，预生成草稿并写入一条站内消息提醒；
3. 整理历史页：查看哪些碎片进了哪篇文章，支持撤销归档。

### Phase 4：AI 增强（默认关闭）

在 Phase 2 的确定性骨架上，可选调用大模型做**润色**而非**归类**：

- 输入：已经分好组、排好序的骨架；
- 任务：为每个小节写一句小结、补小节之间的过渡；
- 约束：**不允许新增事实、不允许改动原文**，只允许在原文之外生成说明性文字；
- 开关：`application.yml` 里的 `note.ai.enabled`，未配置 API Key 时整条链路自动跳过。

> 之所以把 AI 放在最后一期且只做润色，是因为**归类的正确性远比文笔重要**。骨架阶段一旦出错，用户需要逐条核对，反而比手写更累。

---

## 8. 与现有模块的关系

| 模块 | 关系 |
| ---- | ---- |
| `tag` | **复用**。碎片的主题就是文章标签，共用一套 `tag` 表与管理页 |
| `article` | **下游**。碎片整理后产出 `status = 0` 的草稿文章，走现有审核流程 |
| `article_tag` | 生成草稿时，把本次涉及的主题 tag 自动写进文章标签，保持前台归档页可检索 |
| `spark` | **无关**。两者字段与语义都不同，不做合并 |
| `message` | Phase 3 定时整理后，用现有消息表通知 |
| `file` | 碎片暂不支持图片；如需支持，复用 `uploadFile({ type: 'content' })` |

---

## 9. 待确认事项

1. **是否现在就做 Phase 3 的定时整理**：已确认**暂不做**，等前两期稳定后再评估。
2. **碎片是否支持图片**：当前不支持，只记文本；如需支持可复用 `uploadFile({ type: 'content' })`。
3. **AI 润色是否要做**：Phase 4，默认关闭，需要额外配 API Key。

---

## 10. 后端实现落点

| 关注点           | 文件                                                                 |
| ---------------- | -------------------------------------------------------------------- |
| 建表 + 权限脚本  | `docs/sql/note.sql`                                                  |
| 常量             | `backend/.../constant/NoteConstant.java`                             |
| 实体             | `backend/.../domain/entity/Note.java`、`NoteTag.java`                |
| DTO              | `backend/.../domain/dto/note/`（Add / Update / Query / Aggregate）   |
| VO               | `backend/.../domain/vo/`（NoteVO / NoteAggregateVO / NoteComposeVO / NoteTopicStatVO） |
| 骨架构建器       | `backend/.../manager/note/NoteMarkdownBuilder.java`（纯函数，带单测） |
| Service          | `backend/.../service/impl/NoteServiceImpl.java`、`NoteTagServiceImpl.java` |
| Controller       | `backend/.../controller/NoteController.java`                         |
| 鉴权             | `SaTokenConfigure` 增加 `/note/**` 的 user 登录校验（后台接口排除）   |
| 标签删除联动     | `TagServiceImpl.deleteTag` 同步清理 `note_tag`                        |

### 关键实现约定

1. **归属隔离由服务端强制**：`NoteServiceImpl.currentUserId()` 从 Sa-Token 会话取用户，
   列表/整理接口一律改写 `userId`，请求体传入无效。
2. **他人资源按「不存在」处理**：`getOwnNoteOrThrow` 在归属不匹配时抛 `NOT_FOUND_ERROR`，
   而不是 `FORBIDDEN`，避免通过错误码探测 ID 是否存在。
3. **主主题 = 第一个标签**：`note_tag.id` 自增，写入顺序即主键顺序，
   `NoteTagServiceImpl.listTagIdsByNoteIds` 按 id 升序还原该语义。
4. **整理只取未整理碎片**：`loadNotesForAggregate` 硬编码 `status=0`，
   归档过的碎片不会被重复整理。
5. **确定性生成**：`NoteMarkdownBuilder` 无状态、无随机、无时间依赖（除日期格式化），
   6 个单测覆盖分组顺序、组内排序、多行正文、标题生成、过渡提示与空输入。
