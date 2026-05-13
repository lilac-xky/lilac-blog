package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.category.CategoryAddRequest;
import com.lilac.domain.dto.category.CategoryQueryRequest;
import com.lilac.domain.dto.category.CategoryUpdateRequest;
import com.lilac.domain.entity.Category;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.CategoryVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.auth.anotation.SaAdminPermission;
import com.lilac.service.impl.CategoryService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类接口
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 添加分类
     *
     * @param categoryAddRequest 添加分类参数
     * @return 添加结果
     */
    @PostMapping("/add")
    @SaAdminPermission("category:add")
    public Result<Long> addCategory(@RequestBody CategoryAddRequest categoryAddRequest) {
        ThrowUtils.throwIf(categoryAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long category = categoryService.addCategory(categoryAddRequest);
        return Result.success(category);
    }

    /**
     * 获取分类列表(管理员)
     *
     * @param categoryQueryRequest 获取分类列表参数
     * @return 分类列表
     */
    @PostMapping("/list/page")
    @SaAdminPermission("category:list")
    public Result<Page<Category>> listCategoryByPage(@RequestBody CategoryQueryRequest categoryQueryRequest) {
        ThrowUtils.throwIf(categoryQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long current = categoryQueryRequest.getCurrent();
        long size = categoryQueryRequest.getPageSize();
        // 执行分页查询
        Page<Category> categoryPage = categoryService.page(new Page<>(current, size), categoryService.getQueryWrapper(categoryQueryRequest));
        return Result.success(categoryPage);
    }

    /**
     * 获取分类列表(用户)
     *
     * @param categoryQueryRequest 获取分类列表参数
     * @return 分类列表
     */
    @PostMapping("/list/page/vo")
    public Result<Page<CategoryVO>> listCategoryByPageVO(@RequestBody CategoryQueryRequest categoryQueryRequest) {
        ThrowUtils.throwIf(categoryQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long current = categoryQueryRequest.getCurrent();
        long size = categoryQueryRequest.getPageSize();
        // 执行分页查询
        Page<Category> categoryPage = categoryService.page(new Page<>(current, size), categoryService.getQueryWrapper(categoryQueryRequest));
        Page<CategoryVO> categoryVOPage = new Page<>(categoryPage.getCurrent(), categoryPage.getSize(), categoryPage.getTotal());
        categoryVOPage.setRecords(categoryPage.getRecords().stream().map(CategoryVO::objToVo).toList());
        return Result.success(categoryVOPage);
    }

    /**
     * 删除分类
     *
     * @param deleteRequest 删除分类参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaAdminPermission("category:delete")
    public Result<Boolean> deleteCategory(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean delete = categoryService.deleteCategory(deleteRequest.getId());
        ThrowUtils.throwIf(!delete, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 修改分类
     *
     * @param categoryUpdateRequest 修改分类参数
     * @return 修改结果
     */
    @PostMapping("/update")
    @SaAdminPermission("category:update")
    public Result<Boolean> updateCategory(@RequestBody CategoryUpdateRequest categoryUpdateRequest) {
        ThrowUtils.throwIf(categoryUpdateRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Category category = new Category();
        BeanUtils.copyProperties(categoryUpdateRequest, category);
        boolean update = categoryService.updateById(category);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }
}
