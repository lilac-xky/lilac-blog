package com.lilac.domain.dto.category;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryQueryRequest extends PageRequest {
    /**
     * 分类id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;
}
