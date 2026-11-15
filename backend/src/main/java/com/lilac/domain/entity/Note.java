package com.lilac.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 知识碎片表
 */
@TableName(value ="note")
@Data
public class Note implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 归属用户id（所有查询一律按此字段隔离，仅本人可见）
     */
    private Long userId;

    /**
     * 知识点正文
     */
    private String content;

    /**
     * 我的一句话理解（可选）
     */
    private String insight;

    /**
     * 来源：书名 / 链接 / 课程（可选）
     */
    private String source;

    /**
     * 0未整理，1已归档进文章
     */
    private Integer status;

    /**
     * 归档到的文章id（status=1 时有值）
     */
    private Long articleId;

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
