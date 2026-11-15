declare namespace API {
  type AdminResetPasswordRequest = {
    /** id */
    id?: number;
    /** 新密码 */
    newPassword?: string;
  };

  type ArticleAddRequest = {
    /** 标题 */
    title?: string;
    /** 摘要 */
    summary?: string;
    /** 文章内容 */
    content?: string;
    /** 关联分类 */
    categoryId?: number;
    /** 封面图片URL（WebP缩略图） */
    coverUrl?: string;
    /** 0草稿，1待审核，2审核 */
    status?: number;
    /** 1置顶，0普通 */
    isTop?: number;
    /** 标签列表 */
    tagIds?: number[];
  };

  type ArticleQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** id */
    id?: number;
    /** 标题 */
    title?: string;
    /** 关联分类 */
    categoryId?: number;
    /** 投稿人id */
    userId?: number;
    /** 1置顶，0普通 */
    isTop?: number;
    /** 0草稿，1待审核，2审核 */
    status?: number;
    /** 标签列表 */
    tagIds?: number[];
  };

  type ArticleReviewRequest = {
    /** 文章id */
    id?: number;
    /** 操作类型  (1通过 2拒绝) */
    action?: number;
    /** 拒绝理由 */
    rejectReason?: string;
  };

  type ArticleUpdateRequest = {
    /** id */
    id?: number;
    /** 标题 */
    title?: string;
    /** 摘要 */
    summary?: string;
    /** 文章内容 */
    content?: string;
    /** 关联分类 */
    categoryId?: number;
    /** 封面图片URL（WebP缩略图） */
    coverUrl?: string;
    /** 1置顶，0普通 */
    isTop?: number;
    /** 0草稿，1待审核，2审核 */
    status?: number;
    /** 标签列表 */
    tagIds?: number[];
  };

  type ArticleVO = {
    /** id */
    id?: number;
    /** 标题 */
    title?: string;
    /** 摘要 */
    summary?: string;
    /** 文章内容 */
    content?: string;
    /** 关联分类 */
    categoryId?: number;
    /** 封面图片URL（WebP缩略图） */
    coverUrl?: string;
    /** 投稿人id */
    userId?: number;
    /** 浏览量 */
    viewCount?: number;
    /** 1置顶，0普通 */
    isTop?: number;
    /** 0草稿，1待审核，2审核 */
    status?: number;
    /** 审核拒绝原因 */
    rejectReason?: string;
    /** 创建时间 */
    createTime?: string;
    /** 分类名称 */
    categoryName?: string;
    /** 作者昵称（后台审核页展示用，前台列表可不填充） */
    authorName?: string;
    /** 作者头像 */
    authorAvatar?: string;
    /** 标签列表 */
    tags?: TagVO[];
  };

  type Category = {
    /** 分类id */
    id?: number;
    /** 分类名称 */
    categoryName?: string;
    /** 创建时间 */
    createTime?: string;
    /** 修改时间 */
    updateTime?: string;
    /** 编辑时间 */
    editTime?: string;
    /** 0正常，1删除 */
    isDeleted?: number;
  };

  type CategoryAddRequest = {
    /** 分类名称 */
    categoryName?: string;
  };

  type CategoryQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** 分类id */
    id?: number;
    /** 分类名称 */
    categoryName?: string;
  };

  type CategoryUpdateRequest = {
    /** 分类id */
    id?: number;
    /** 分类名称 */
    categoryName?: string;
  };

  type CategoryVO = {
    /** 分类id */
    id?: number;
    /** 分类名称 */
    categoryName?: string;
  };

  type DeleteRequest = {
    /** id */
    id?: number;
  };

  type ForgotPasswordRequest = {
    /** 邮箱 */
    email?: string;
    /** 验证码 */
    code?: string;
    /** 新密码 */
    newPassword?: string;
    /** 确认密码 */
    checkPassword?: string;
  };

  type getArticleParams = {
    /** 文章id */
    id?: number;
  };

  type getNoteParams = {
    /** 碎片id */
    id?: number;
  };

  type getSparkParams = {
    /** 灵感id */
    id?: number;
  };

  type Item = {
    /** 标签id；未挂主题的碎片为 null */
    tagId?: number;
    /** 标签名；未挂主题时为「杂项」 */
    tagName?: string;
    /** 该主题下未整理的碎片数 */
    count?: number;
  };

  type AnnouncementReadRequest = {
    /** 需要标记已读的公告id列表 */
    announcementIds?: (number | string)[];
  };

  type AnnouncementVO = {
    /** 公告id */
    id?: number | string;
    /** 公告标题 */
    title?: string;
    /** 公告正文（Markdown） */
    content?: string;
    /** 发布时间 */
    publishTime?: string;
    /** 创建时间 */
    createTime?: string;
    /** 当前用户是否已读（仅公告中心回看接口填充） */
    isRead?: boolean;
  };

  type LoginUserVO = {
    id?: number;
    /** 用户名 */
    userAccount?: string;
    /** 邮箱 */
    email?: string;
    /** 昵称 */
    username?: string;
    /** 头像 */
    avatar?: string;
    /** 角色(admin管理,user普通用户) */
    role?: string;
    /** 角色id */
    roleId?: number;
    /** 1正常，0异常 */
    status?: number;
    /** token */
    token?: string;
    /** 权限列表 */
    permissions?: string[];
    /** 角色列表 */
    roles?: string[];
  };

  type MessageQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** 消息类型（可选） */
    type?: number;
    /** 是否已读（可选）：0未读 1已读 */
    isRead?: number;
  };

  type MessageReadRequest = {
    /** 要标记已读的消息ID列表；为空表示全部标记已读 */
    ids?: number[];
  };

  type MessageVO = {
    id?: number;
    /** 接收用户ID */
    userId?: number;
    /** 消息类型：1 审核通过, 2 审核驳回, 99 系统通知 */
    type?: number;
    /** 消息标题 */
    title?: string;
    /** 消息正文 */
    content?: string;
    /** 关联业务ID（如文章ID） */
    refId?: number;
    /** 关联业务类型：article 等 */
    refType?: string;
    /** 0未读 1已读 */
    isRead?: number;
    /** 创建时间 */
    createTime?: string;
  };

  type NoteAddRequest = {
    /** 知识点正文 */
    content?: string;
    /** 我的一句话理解（可选） */
    insight?: string;
    /** 来源：书名 / 链接 / 课程（可选） */
    source?: string;
    /** 主题标签id列表，第一个作为整理时的分组依据 */
    tagIds?: number[];
  };

  type NoteAggregateRequest = {
    /** 方式一：指定要整理的碎片id列表，优先于时间区间 */
    noteIds?: number[];
    /** 方式二：按创建时间区间筛选，起始时间（含） */
    fromTime?: string;
    /** 方式二：按创建时间区间筛选，结束时间（含） */
    toTime?: string;
    /** 可选：只整理这些主题下的碎片；为空表示不限主题 */
    tagIds?: number[];
    /** 可选：自定义标题；为空时由服务端按规则生成 */
    title?: string;
    /** 可选：是否在小节之间插入过渡提示注释，默认 true */
    insertHints?: boolean;
  };

  type NoteAggregateVO = {
    /** 建议的文章标题（请求里传了标题则以请求为准） */
    title?: string;
    /** 生成好的 Markdown 骨架，尚未落库 */
    markdown?: string;
    /** 参与整理的碎片条数 */
    noteCount?: number;
    /** 其中未挂主题、被归入「杂项」的条数 */
    untaggedCount?: number;
    /** 主题分布，按组内碎片数降序 */
    topics?: TopicItem[];
    /** 本次整理涉及的主题标签id（生成草稿时会写入文章标签） */
    tagIds?: number[];
    /** 参与整理的碎片id，便于前端高亮或二次确认 */
    noteIds?: number[];
  };

  type NoteComposeVO = {
    /** 生成的文章id，前端据此跳转到现有编辑器 */
    articleId?: number;
    /** 文章标题 */
    title?: string;
    /** 本次归档的碎片条数 */
    noteCount?: number;
  };

  type NoteQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** 碎片id */
    id?: number;
    /** 知识点正文（模糊查询） */
    content?: string;
    /** 0未整理，1已归档；为空表示不限 */
    status?: number;
    /** 主题标签id列表；命中任意一个即返回 */
    tagIds?: number[];
    /** 归属用户id；用户端会被服务端强制改写为当前登录用户，传入无效 */
    userId?: number;
    /** 归档到的文章id */
    articleId?: number;
  };

  type NoteTopicStatVO = {
    /** 未整理的碎片总数 */
    rawTotal?: number;
    /** 已归档的碎片总数 */
    archivedTotal?: number;
    /** 各主题下未整理的碎片数，按数量降序 */
    topics?: Item[];
  };

  type NoteUpdateRequest = {
    /** 碎片id */
    id?: number;
    /** 知识点正文 */
    content?: string;
    /** 我的一句话理解（可选） */
    insight?: string;
    /** 来源：书名 / 链接 / 课程（可选） */
    source?: string;
    /** 主题标签id列表；传 null 表示不改动标签，传空列表表示清空标签 */
    tagIds?: number[];
  };

  type NoteVO = {
    /** 碎片id */
    id?: number;
    /** 归属用户id */
    userId?: number;
    /** 知识点正文 */
    content?: string;
    /** 我的一句话理解 */
    insight?: string;
    /** 来源：书名 / 链接 / 课程 */
    source?: string;
    /** 0未整理，1已归档进文章 */
    status?: number;
    /** 归档到的文章id */
    articleId?: number;
    /** 创建时间 */
    createTime?: string;
    /** 主题标签列表，顺序与写入时一致，第一个即整理时的分组依据 */
    tags?: TagVO[];
  };

  type OrderItem = {
    column?: string;
    asc?: boolean;
  };

  type PageArticleVO = {
    records?: ArticleVO[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageCategory = {
    records?: Category[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageCategoryVO = {
    records?: CategoryVO[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageMessageVO = {
    records?: MessageVO[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageNoteVO = {
    records?: NoteVO[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PagePermission = {
    records?: Permission[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageRole = {
    records?: Role[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageSpark = {
    records?: Spark[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageSparkVO = {
    records?: SparkVO[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageTag = {
    records?: Tag[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageTagVO = {
    records?: TagVO[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type PageUserVO = {
    records?: UserVO[];
    total?: number;
    size?: number;
    current?: number;
    orders?: OrderItem[];
    optimizeCountSql?: boolean;
    searchCount?: boolean;
    optimizeJoinOfCountSql?: boolean;
    maxLimit?: number;
    countId?: string;
  };

  type Permission = {
    /** id */
    id?: number;
    /** 权限key */
    permissionKey?: string;
    /** 权限描述 */
    name?: string;
    /** 创建时间 */
    createTime?: string;
    /** 修改时间 */
    updateTime?: string;
    /** 编辑时间 */
    editTime?: string;
    /** 0正常，1删除 */
    isDeleted?: number;
  };

  type PermissionAddRequest = {
    /** 权限key */
    permissionKey?: string;
    /** 权限描述 */
    name?: string;
  };

  type PermissionQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** id */
    id?: number;
    /** 权限key */
    permissionKey?: string;
    /** 权限描述 */
    name?: string;
  };

  type PermissionUpdateRequest = {
    /** id */
    id?: number;
    /** 权限key */
    permissionKey?: string;
    /** 权限描述 */
    name?: string;
  };

  type ResultArticleVO = {
    code?: number;
    msg?: string;
    data?: ArticleVO;
  };

  type ResultBoolean = {
    code?: number;
    msg?: string;
    data?: boolean;
  };

  type ResultInteger = {
    code?: number;
    msg?: string;
    data?: number;
  };

  type ResultListAnnouncementVO = {
    code?: number;
    msg?: string;
    data?: AnnouncementVO[];
  };

  type ResultListPermission = {
    code?: number;
    msg?: string;
    data?: Permission[];
  };

  type ResultLoginUserVO = {
    code?: number;
    msg?: string;
    data?: LoginUserVO;
  };

  type ResultLong = {
    code?: number;
    msg?: string;
    data?: number;
  };

  type ResultNoteAggregateVO = {
    code?: number;
    msg?: string;
    data?: NoteAggregateVO;
  };

  type ResultNoteComposeVO = {
    code?: number;
    msg?: string;
    data?: NoteComposeVO;
  };

  type ResultNoteTopicStatVO = {
    code?: number;
    msg?: string;
    data?: NoteTopicStatVO;
  };

  type ResultNoteVO = {
    code?: number;
    msg?: string;
    data?: NoteVO;
  };

  type ResultPageArticleVO = {
    code?: number;
    msg?: string;
    data?: PageArticleVO;
  };

  type ResultPageCategory = {
    code?: number;
    msg?: string;
    data?: PageCategory;
  };

  type ResultPageCategoryVO = {
    code?: number;
    msg?: string;
    data?: PageCategoryVO;
  };

  type ResultPageMessageVO = {
    code?: number;
    msg?: string;
    data?: PageMessageVO;
  };

  type ResultPageNoteVO = {
    code?: number;
    msg?: string;
    data?: PageNoteVO;
  };

  type ResultPagePermission = {
    code?: number;
    msg?: string;
    data?: PagePermission;
  };

  type ResultPageRole = {
    code?: number;
    msg?: string;
    data?: PageRole;
  };

  type ResultPageSpark = {
    code?: number;
    msg?: string;
    data?: PageSpark;
  };

  type ResultPageSparkVO = {
    code?: number;
    msg?: string;
    data?: PageSparkVO;
  };

  type ResultPageTag = {
    code?: number;
    msg?: string;
    data?: PageTag;
  };

  type ResultPageTagVO = {
    code?: number;
    msg?: string;
    data?: PageTagVO;
  };

  type ResultPageUserVO = {
    code?: number;
    msg?: string;
    data?: PageUserVO;
  };

  type ResultSparkVO = {
    code?: number;
    msg?: string;
    data?: SparkVO;
  };

  type ResultString = {
    code?: number;
    msg?: string;
    data?: string;
  };

  type ResultUploadPictureResult = {
    code?: number;
    msg?: string;
    data?: UploadPictureResult;
  };

  type Role = {
    /** id */
    id?: number;
    /** 角色key */
    roleKey?: string;
    /** 角色名 */
    name?: string;
    /** 登录类型 */
    loginType?: string;
    /** 角色描述 */
    description?: string;
    /** 创建时间 */
    createTime?: string;
    /** 修改时间 */
    updateTime?: string;
    /** 编辑时间 */
    editTime?: string;
    /** 0正常，1删除 */
    isDeleted?: number;
  };

  type RoleAddPermissionsRequest = {
    /** 角色ID */
    roleId?: number;
    /** 权限ID */
    permissionId?: number;
  };

  type RoleAddRequest = {
    /** 角色key */
    roleKey?: string;
    /** 角色名 */
    name?: string;
    /** 登录类型 */
    loginType?: string;
    /** 角色描述 */
    description?: string;
  };

  type RoleBatchAddPermissionsRequest = {
    /** 角色ID */
    roleId?: number;
    /** 权限ID列表 */
    permissionIds?: number[];
  };

  type RoleBatchRemovePermissionsRequest = {
    /** 角色ID */
    roleId?: number;
    /** 权限ID列表 */
    permissionIds?: number[];
  };

  type RolePermissionQueryRequest = {
    /** 角色ID */
    roleId?: number;
  };

  type RoleQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** id */
    id?: number;
    /** 角色key */
    roleKey?: string;
    /** 角色名 */
    name?: string;
    /** 登录类型 */
    loginType?: string;
  };

  type RoleRemovePermissionRequest = {
    /** 角色ID */
    roleId?: number;
    /** 权限ID */
    permissionId?: number;
  };

  type RoleUpdateRequest = {
    /** id */
    id?: number;
    /** 角色key */
    roleKey?: string;
    /** 角色名 */
    name?: string;
    /** 登录类型 */
    loginType?: string;
    /** 角色描述 */
    description?: string;
  };

  type sendOriginalEmailCodeParams = {
    /** 当前登录用户 id */
    id: number;
  };

  type sendRegisterCodeParams = {
    /** 邮箱 */
    email: string;
  };

  type sendResetCodeParams = {
    /** 邮箱 */
    email: string;
  };

  type Spark = {
    /** id */
    id?: number;
    /** 创建人id(灵感归属人，private 时只有本人可查看) */
    userId?: number;
    /** 灵感内容 */
    content?: string;
    /** 灵感状态(默认spark) */
    status?: string;
    /** 灵感是否公开(默认private) */
    visibility?: string;
    /** 创建时间 */
    createTime?: string;
    /** 修改时间 */
    updateTime?: string;
    /** 编辑时间 */
    editTime?: string;
    /** 0正常，1删除 */
    isDeleted?: number;
  };

  type SparkAddRequest = {
    /** 灵感内容 */
    content?: string;
    /** 灵感状态(默认spark) */
    status?: string;
    /** 灵感是否公开(默认private) */
    visibility?: string;
  };

  type SparkQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** 灵感id */
    id?: number;
    /** 灵感内容(模糊查询) */
    content?: string;
    /** 灵感状态(默认spark) */
    status?: string;
    /** 灵感是否公开(默认private，后台可用，前台会被服务端忽略) */
    visibility?: string;
    /** 创建人id(后台按归属人筛选) */
    userId?: number;
  };

  type SparkUpdateRequest = {
    /** 灵感id */
    id?: number;
    /** 灵感内容 */
    content?: string;
    /** 灵感状态(默认spark) */
    status?: string;
    /** 灵感是否公开(默认private) */
    visibility?: string;
  };

  type SparkVO = {
    /** id */
    id?: number;
    /** 灵感内容 */
    content?: string;
    /** 灵感状态(默认spark) */
    status?: string;
    /** 灵感是否公开(默认private) */
    visibility?: string;
    /** 创建人id(灵感归属人) */
    userId?: number;
    /** 创建时间 */
    createTime?: string;
  };

  type Tag = {
    /** 标签id */
    id?: number;
    /** 标签名称 */
    tagName?: string;
    /** 创建时间 */
    createTime?: string;
    /** 修改时间 */
    updateTime?: string;
    /** 编辑时间 */
    editTime?: string;
    /** 0正常，1删除 */
    isDeleted?: number;
  };

  type TagAddRequest = {
    /** 标签名称 */
    tagName?: string;
  };

  type TagQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** 标签id */
    id?: number;
    /** 标签名称 */
    tagName?: string;
  };

  type TagUpdateRequest = {
    /** 标签id */
    id?: number;
    /** 标签名称 */
    tagName?: string;
  };

  type TagVO = {
    /** 标签id */
    id?: number;
    /** 标签名称 */
    tagName?: string;
  };

  type TopicItem = {
    /** 标签id；未挂主题的「杂项」为 null */
    tagId?: number;
    /** 标签名；未挂主题时为「杂项」 */
    tagName?: string;
    /** 该主题下的碎片条数 */
    count?: number;
  };

  type updateEmailCodeParams = {
    /** 新邮箱 */
    email: string;
  };

  type UpdateEmailMethodRequest = {
    /** id */
    id?: number;
    /** 身份验证通过后发放的一次性凭证 */
    ticket?: string;
    /** 邮箱 */
    email?: string;
    /** 邮箱验证码 */
    code?: string;
  };

  type UpdatePasswordRequest = {
    /** 旧密码 */
    oldPassword?: string;
    /** 新密码 */
    newPassword?: string;
    /** 确认密码 */
    checkPassword?: string;
  };

  type uploadByUrlParams = {
    /** 外链图片 URL */
    url: string;
    /** 上传场景：avatar（头像）/ cover（封面）/ content（正文） */
    type: string;
  };

  type uploadFileParams = {
    /** 上传场景：avatar（头像）/ cover（封面）/ content（正文） */
    type: string;
  };

  type UploadPictureResult = {
    /** 图片url */
    url?: string;
    /** 缩略图 url */
    thumbnailUrl?: string;
  };

  type UserEditRequest = {
    /** id */
    id?: number;
    /** 昵称 */
    username?: string;
    /** 头像 */
    avatar?: string;
  };

  type UserLoginRequest = {
    /** 账号 */
    account?: string;
    /** 密码 */
    password?: string;
  };

  type UserQueryRequest = {
    /** 当前页 */
    current?: number;
    /** 页面大小 */
    pageSize?: number;
    /** 排序顺序（默认：升序） */
    sortOrder?: string;
    /** id */
    id?: number;
    /** 用户名 */
    userAccount?: string;
    /** 邮箱 */
    email?: string;
    /** 昵称 */
    username?: string;
    /** 角色id */
    roleId?: number;
    /** 1正常，0异常 */
    status?: number;
  };

  type UserRegisterRequest = {
    /** 账号 */
    userAccount?: string;
    /** 邮箱 */
    email?: string;
    /** 密码 */
    password?: string;
    /** 确认密码 */
    checkPassword?: string;
    /** 验证码 */
    code?: string;
  };

  type UserStatusRequest = {
    id?: number;
    /** 1正常，0异常 */
    status?: number;
  };

  type UserUpdateEmailRequest = {
    /** 用d */
    id?: number;
    /** 邮箱 */
    email?: string;
  };

  type UserUpdateRequest = {
    /** id */
    id?: number;
    /** 用户名 */
    userAccount?: string;
    /** 邮箱 */
    email?: string;
    /** 昵称 */
    username?: string;
    /** 头像 */
    avatar?: string;
    /** 角色id */
    roleId?: number;
    /** 1正常，0异常 */
    status?: number;
  };

  type UserVO = {
    id?: number;
    /** 用户名 */
    userAccount?: string;
    /** 邮箱 */
    email?: string;
    /** 昵称 */
    username?: string;
    /** 头像 */
    avatar?: string;
    /** 角色id */
    roleId?: number;
    /** 1正常，0异常 */
    status?: number;
    /** 创建时间 */
    createTime?: string;
  };

  type VerifyEmailIdentityRequest = {
    /** 用户 id */
    id?: number;
    /** 身份验证方式：email 表示原邮箱验证码，password 表示当前密码 */
    method?: string;
    /** 原邮箱验证码，method 为 email 时必填 */
    originalCode?: string;
    /** 当前密码，method 为 password 时必填 */
    password?: string;
  };
}
