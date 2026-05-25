package com.lilac.domain.dto.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消息标记已读请求
 */
@Data
public class MessageReadRequest implements Serializable {

    /**
     * 要标记已读的消息ID列表；为空表示全部标记已读
     */
    private List<Long> ids;

    private static final long serialVersionUID = 1L;
}
