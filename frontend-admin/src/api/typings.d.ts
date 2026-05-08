declare namespace API {
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
    /** 创建时间 */
    createTime?: string;
    /** 分类名称 */
    categoryName?: string;
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

  type getArticleParams = {
    /** 文章id */
    id?: number;
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
    /** 1正常，0异常 */
    status?: number;
    /** token */
    token?: string;
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

  type ResultUploadPictureResult = {
    code?: number;
    msg?: string;
    data?: UploadPictureResult;
  };

  type sendRegisterCodeParams = {
    /** 邮箱 */
    email: string;
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
    /** 1正常，0异常 */
    status?: number;
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
    /** 角色(admin管理,user普通用户) */
    role?: string;
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
    /** 角色(admin管理,user普通用户) */
    role?: string;
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
    /** 角色(admin管理,user普通用户) */
    role?: string;
    /** 1正常，0异常 */
    status?: number;
    /** 创建时间 */
    createTime?: string;
  };
}
