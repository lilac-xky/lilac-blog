declare namespace API {
  type LoginUserVO = {
    id?: number;
    /** 用户名 */
    userAccount?: string;
    /** 邮箱 */
    email?: string;
    /** 密码 */
    password?: string;
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

  type sendRegisterCodeParams = {
    /** 邮箱 */
    email: string;
  };

  type UserLoginRequest = {
    /** 账号 */
    account?: string;
    /** 密码 */
    password?: string;
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

  type ArticleQueryRequest = {
    current?: number;
    pageSize?: number;
    sortOrder?: string;
    id?: number;
    title?: string;
    categoryId?: number;
    userId?: number;
    isTop?: number;
    status?: number;
    tagIds?: number[];
  };

  type ArticleVO = {
    id?: number;
    title?: string;
    summary?: string;
    content?: string;
    categoryId?: number;
    coverUrl?: string;
    userId?: number;
    viewCount?: number;
    isTop?: number;
    status?: number;
    createTime?: string;
    categoryName?: string;
    tags?: TagVO[];
  };

  type PageArticleVO = {
    records?: ArticleVO[];
    total?: number;
    size?: number;
    current?: number;
  };

  type ResultArticleVO = {
    code?: number;
    msg?: string;
    data?: ArticleVO;
  };

  type ResultPageArticleVO = {
    code?: number;
    msg?: string;
    data?: PageArticleVO;
  };

  type getArticleParams = {
    id?: string | number;
  };

  type CategoryVO = {
    id?: number;
    categoryName?: string;
  };

  type CategoryQueryRequest = {
    current?: number;
    pageSize?: number;
    sortOrder?: string;
    id?: number;
    categoryName?: string;
  };

  type PageCategoryVO = {
    records?: CategoryVO[];
    total?: number;
    size?: number;
    current?: number;
  };

  type ResultPageCategoryVO = {
    code?: number;
    msg?: string;
    data?: PageCategoryVO;
  };

  type TagVO = {
    id?: number;
    tagName?: string;
  };

  type TagQueryRequest = {
    current?: number;
    pageSize?: number;
    sortOrder?: string;
    id?: number;
    tagName?: string;
  };

  type PageTagVO = {
    records?: TagVO[];
    total?: number;
    size?: number;
    current?: number;
  };

  type ResultPageTagVO = {
    code?: number;
    msg?: string;
    data?: PageTagVO;
  };
}
