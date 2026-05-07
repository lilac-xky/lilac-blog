package com.lilac.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lilac.domain.dto.category.CategoryAddRequest;
import com.lilac.domain.dto.category.CategoryQueryRequest;
import com.lilac.domain.dto.category.CategoryUpdateRequest;
import com.lilac.domain.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 分类Service
*/
public interface CategoryService extends IService<Category> {

    /**
     * 添加分类
     *
     * @param categoryAddRequest 添加参数
     * @return 分类id
     */
    long addCategory(CategoryAddRequest categoryAddRequest);

    /**
     * 获取查询参数
     *
     * @param categoryQueryRequest 查询参数
     * @return 查询参数
     */
    Wrapper<Category> getQueryWrapper(CategoryQueryRequest categoryQueryRequest);

    /**
     * 删除分类
     *
     * @param id 分类id
     * @return 删除结果
     */
    boolean deleteCategory(Long id);
}
