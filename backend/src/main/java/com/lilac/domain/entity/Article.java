package com.lilac.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文章表
 */
@TableName(value ="article")
@Data
public class Article implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 关联分类
     */
    private Long categoryId;

    /**
     * 封面图片URL（WebP缩略图）
     */
    private String coverUrl;

    /**
     * 投稿人id
     */
    private Long userId;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 1置顶，0普通
     */
    private Integer isTop;

    /**
     * 0草稿，1待审核，2审核
     */
    private Integer status;

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