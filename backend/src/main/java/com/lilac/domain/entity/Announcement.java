package com.lilac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统公告
 */
@TableName(value = "announcement")
@Data
public class Announcement implements Serializable {

    /**
     * 公告id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告正文（Markdown）
     */
    private String content;

    /**
     * 状态：0草稿 1已发布 2已下线
     */
    private Integer status;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 业务侧编辑时间
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
