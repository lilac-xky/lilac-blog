package com.lilac.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 灵感表
 */
@TableName(value ="spark")
@Data
public class Spark implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建人id(灵感归属人，private 时只有本人可查看)
     */
    private Long userId;

    /**
     * 灵感内容
     */
    private String content;

    /**
     * 灵感状态(默认spark)
     */
    private String status;

    /**
     * 灵感是否公开(默认private)
     */
    private String visibility;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 0正常，1删除
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}