package com.lilac.domain.dto.category;

import lombok.Data;

/**
 * 添加分类请求
 */
@Data
public class CategoryAddRequest {

    /**
     * 分类名称
     */
    private String categoryName;
}
