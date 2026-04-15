package com.lilac.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avater;

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
    private Date creattime;

    /**
     * 修改时间
     */
    private Date updatetime;

    /**
     * 编辑时间
     */
    private Date edittime;

    /**
     * 0正常，1删除
     */
    @TableLogic
    private String isdeleted;
}