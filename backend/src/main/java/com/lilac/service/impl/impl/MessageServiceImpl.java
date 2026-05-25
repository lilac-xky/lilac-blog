package com.lilac.service.impl.impl;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.domain.dto.message.MessageQueryRequest;
import com.lilac.domain.dto.message.MessageReadRequest;
import com.lilac.domain.entity.Message;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.MessageVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.mapper.MessageMapper;
import com.lilac.service.impl.MessageService;
import com.lilac.service.impl.UserService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 站内消息 Service 实现
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private UserService userService;

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
    @Override
    public long sendMessage(Long userId, Integer type, String title, String content, Long refId, String refType) {
        ThrowUtils.throwIf(userId == null || type == null, HttpsCodeEnum.PARAMS_ERROR);
        Message message = new Message();
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setRefId(refId);
        message.setRefType(refType);
        message.setIsRead(0);
        boolean save = this.save(message);
        ThrowUtils.throwIf(!save, HttpsCodeEnum.OPERATION_ERROR);
        return message.getId();
    }

    /**
     * 分页查询我的消息
     *
     * @param request 请求参数
     * @return 分页结果
     */
    @Override
    public Page<MessageVO> listMyMessageByPage(MessageQueryRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, loginUser.getId())
                .eq(ObjUtil.isNotEmpty(request.getType()), Message::getType, request.getType())
                .eq(ObjUtil.isNotEmpty(request.getIsRead()), Message::getIsRead, request.getIsRead())
                .orderByDesc(Message::getCreateTime);
        Page<Message> page = this.page(new Page<>(request.getCurrent(), request.getPageSize()), wrapper);
        Page<MessageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(MessageVO::objToVo).toList());
        return voPage;
    }

    /**
     * 标记消息已读（ids 为空则全部已读）
     *
     * @param request 请求参数
     * @return 影响行数
     */
    @Override
    public int markRead(MessageReadRequest request) {
        User loginUser = userService.getLoginUser();
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<Message>()
                .eq(Message::getUserId, loginUser.getId())
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1);
        if (request != null && request.getIds() != null && !request.getIds().isEmpty()) {
            wrapper.in(Message::getId, request.getIds());
        }
        return this.baseMapper.update(null, wrapper);
    }

    /**
     * 当前用户未读消息数
     *
     * @return 未读消息数
     */
    @Override
    public long getMyUnreadCount() {
        User loginUser = userService.getLoginUser();
        return this.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, loginUser.getId())
                .eq(Message::getIsRead, 0));
    }
}
