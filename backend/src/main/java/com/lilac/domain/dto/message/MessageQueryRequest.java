package com.lilac.domain.dto.message;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 站内消息查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessageQueryRequest extends PageRequest implements Serializable {

    /**
     * 消息类型（可选）
     */
    private Integer type;

    /**
     * 是否已读（可选）：0未读 1已读
     */
    private Integer isRead;

    private static final long serialVersionUID = 1L;
}
