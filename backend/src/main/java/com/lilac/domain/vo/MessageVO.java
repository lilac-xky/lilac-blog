package com.lilac.domain.vo;

import cn.hutool.core.bean.BeanUtil;
import com.lilac.domain.entity.Message;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 站内消息 VO
 */
@Data
public class MessageVO implements Serializable {

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
     * obj转vo
     */
    public static MessageVO objToVo(Message message) {
        if (message == null) {
            return null;
        }
        MessageVO vo = new MessageVO();
        BeanUtil.copyProperties(message, vo);
        return vo;
    }

    /**
     * vo转obj
     */
    public static Message voToObj(MessageVO messageVO) {
        if (messageVO == null) {
            return null;
        }
        Message message = new Message();
        BeanUtil.copyProperties(messageVO, message);
        return message;
    }

    private static final long serialVersionUID = 1L;
}
