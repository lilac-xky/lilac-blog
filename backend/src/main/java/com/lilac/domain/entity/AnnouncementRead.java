package com.lilac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告已读记录
 */
@TableName(value = "announcement_read")
@Data
public class AnnouncementRead implements Serializable {

    /**
     * 记录id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 公告id
     */
    private Long announcementId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 已读时间
     */
    private Date readTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
