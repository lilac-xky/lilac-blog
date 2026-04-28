declare namespace API {
  type DeleteRequest = {
    /** id */
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

  type uploadByUrlParams = {
    /** 外链图片 URL */
    url: string;
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
    creatTime?: string;
  };
}
