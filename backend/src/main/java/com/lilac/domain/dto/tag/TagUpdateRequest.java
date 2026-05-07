package com.lilac.domain.dto.tag;

import lombok.Data;

/**
 * 添加标签请求
 */
@Data
public class TagUpdateRequest {
    /**
     * 标签id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;
}
