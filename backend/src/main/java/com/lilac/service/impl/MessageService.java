package com.lilac.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.dto.message.MessageQueryRequest;
import com.lilac.domain.dto.message.MessageReadRequest;
import com.lilac.domain.entity.Message;
import com.lilac.domain.vo.MessageVO;

/**
 * 站内消息 Service
 */
public interface MessageService extends IService<Message> {

    /**
     * 发送一条站内消息（内部调用）
     *
     * @param userId  接收用户ID
     * @param type    消息类型（1 审核通过, 2 审核驳回, 99 系统通知）
     * @param title   标题
     * @param content 正文（可为空）
     * @param refId   关联业务ID
     * @param refType 关联业务类型（如 article）
     * @return 消息ID
     */
    long sendMessage(Long userId, Integer type, String title, String content, Long refId, String refType);

    /**
     * 分页查询我的消息
     *
     * @param request 请求参数
     * @return 分页数据
     */
    Page<MessageVO> listMyMessageByPage(MessageQueryRequest request);

    /**
     * 标记消息已读（按 ids；ids 为空则全部已读）
     *
     * @param request 请求参数
     * @return 影响行数
     */
    int markRead(MessageReadRequest request);

    /**
     * 当前用户未读消息数
     *
     * @return 未读消息数
     */
    long getMyUnreadCount();
}
