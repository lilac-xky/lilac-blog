package com.lilac.domain.vo;

import cn.hutool.core.bean.BeanUtil;
import com.lilac.domain.entity.User;
import lombok.Data;

import java.util.Date;

/**
 * 用户视图
 */
@Data
public class UserVO {
    private Long id;

    /**
     * 用户名
     */
    private String userAccount;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色(admin管理,user普通用户)
     */
    private String role;

    /**
     * 1正常，0异常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date creatTime;

    /**
     * 转换为对象
     * @param userVO
     * @return
     */
    public static User voToObj(UserVO userVO){
        if(userVO == null) {
            return null;
        }
        User user = new User();
        BeanUtil.copyProperties(userVO, user);
        return user;
    }

    /**
     * 转换为VO
     * @param user
     * @return
     */
    public static UserVO objToVo(User user){
        if(user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }
}
