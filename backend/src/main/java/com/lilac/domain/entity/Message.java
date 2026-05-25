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
 * 站内消息
 */
@TableName(value = "message")
@Data
public class Message implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 消息类型：1 审核通过, 2 审核驳回, 99 系统通知
     */
    private Integer type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文
     */
    private String content;

    /**
     * 关联业务ID（如文章ID）
     */
    private Long refId;

    /**
     * 关联业务类型：article 等
     */
    private String refType;

    /**
     * 0未读 1已读
     */
    private Integer isRead;

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
     * 0正常 1删除
     */
    @TableLogic
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}
