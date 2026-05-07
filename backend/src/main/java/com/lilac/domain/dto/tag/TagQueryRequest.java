package com.lilac.domain.dto.tag;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TagQueryRequest extends PageRequest {
    /**
     * 标签id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;
}
