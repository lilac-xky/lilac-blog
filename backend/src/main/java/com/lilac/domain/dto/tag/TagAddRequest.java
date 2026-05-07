package com.lilac.domain.dto.tag;

import lombok.Data;

/**
 * 添加标签请求
 */
@Data
public class TagAddRequest {

    /**
     * 标签名称
     */
    private String tagName;
}
