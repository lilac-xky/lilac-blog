package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.domain.dto.message.MessageQueryRequest;
import com.lilac.domain.dto.message.MessageReadRequest;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.MessageVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.auth.anotation.SaUserPermission;
import com.lilac.service.impl.MessageService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内消息接口（普通用户）
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /**
     * 分页查询我的消息
     */
    @PostMapping("/my/page")
    @SaUserPermission("article:submit")
    public Result<Page<MessageVO>> listMyMessageByPage(@RequestBody MessageQueryRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(messageService.listMyMessageByPage(request));
    }

    /**
     * 标记消息已读（ids 为空则全部已读）
     */
    @PostMapping("/read")
    @SaUserPermission("article:submit")
    public Result<Integer> markRead(@RequestBody MessageReadRequest request) {
        return Result.success(messageService.markRead(request));
    }

    /**
     * 当前用户未读消息数
     */
    @GetMapping("/unread/count")
    @SaUserPermission("article:submit")
    public Result<Long> getUnreadCount() {
        return Result.success(messageService.getMyUnreadCount());
    }
}
