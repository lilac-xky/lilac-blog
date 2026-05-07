package com.lilac.domain.dto.category;

import lombok.Data;

/**
 * 修改文章参数
 */
@Data
public class CategoryUpdateRequest {
    /**
     * 分类id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;
}
