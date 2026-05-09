package com.lilac.service.impl.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.domain.dto.category.CategoryAddRequest;
import com.lilac.domain.dto.category.CategoryQueryRequest;
import com.lilac.domain.entity.Article;
import com.lilac.domain.entity.Category;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.impl.ArticleService;
import com.lilac.service.impl.CategoryService;
import com.lilac.mapper.CategoryMapper;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
* 分类Service实现
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    @Lazy
    @Resource
    private ArticleService articleService;

    /**
     * 添加分类
     *
     * @param categoryAddRequest 添加分类参数
     * @return 添加结果
     */
    @Override
    public long addCategory(CategoryAddRequest categoryAddRequest) {
        // 分类是否存在
        String categoryName = categoryAddRequest.getCategoryName();
        long count = this.baseMapper.selectCount(new LambdaQueryWrapper<Category>().eq(Category::getCategoryName, categoryName));
        ThrowUtils.throwIf(count > 0, HttpsCodeEnum.PARAMS_ERROR, "分类已存在");
        // 保存分类
        Category category = new Category();
        BeanUtils.copyProperties(categoryAddRequest, category);
        boolean result = save(category);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return category.getId();
    }

    /**
     * 获取查询参数
     *
     * @param categoryQueryRequest 查询参数
     * @return 查询参数
     */
    @Override
    public Wrapper<Category> getQueryWrapper(CategoryQueryRequest categoryQueryRequest) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        Long id = categoryQueryRequest.getId();
        String categoryName = categoryQueryRequest.getCategoryName();
        String sortOrder = categoryQueryRequest.getSortOrder();
        // 查询拼装
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Category::getId, id);
        queryWrapper.eq(StrUtil.isNotBlank(categoryName), Category::getCategoryName, categoryName);
        boolean isDesc = !"ascend".equalsIgnoreCase(sortOrder);
        queryWrapper.orderBy(true, isDesc, Category::getCreateTime);
        return queryWrapper;
    }

    /**
     * 删除分类
     *
     * @param id 分类id
     * @return 删除结果
     */
    @Override
    public boolean deleteCategory(Long id) {
        // 检查是否有文章关联分类
        long count = articleService.count(new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
        ThrowUtils.throwIf(count > 0, HttpsCodeEnum.OPERATION_ERROR, "该分类下有文章，请先删除文章");
        // 删除分类
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }

}
