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
    avater?: string;
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

  type UserLoginRequest = {
    /** 账号 */
    userAccount?: string;
    /** 邮箱 */
    email?: string;
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
  };
}
