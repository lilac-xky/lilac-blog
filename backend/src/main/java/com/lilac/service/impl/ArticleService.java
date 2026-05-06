package com.lilac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.domain.dto.article.ArticleAddRequest;
import com.lilac.domain.dto.article.ArticleQueryRequest;
import com.lilac.domain.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.vo.ArticleVO;

/**
* 文章Service
*/
public interface ArticleService extends IService<Article> {
    /**
     * 添加文章
     * @param articleAddRequest 添加参数
     * @return 文章id
     */
    long addArticle(ArticleAddRequest articleAddRequest);

    /**
     * 获取文章列表
     * @param articleQueryRequest 查询参数
     * @return 文章列表
     */
    Page<ArticleVO> listArticleByVOPage(ArticleQueryRequest articleQueryRequest);

    /**
     * 获取查询条件
     * @param articleQueryRequest 获取查询条件参数
     * @return 查询条件
     */
    LambdaQueryWrapper<Article> getQueryWrapper(ArticleQueryRequest articleQueryRequest);
}
