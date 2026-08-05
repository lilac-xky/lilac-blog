package com.lilac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.domain.dto.article.ArticleAddRequest;
import com.lilac.domain.dto.article.ArticleQueryRequest;
import com.lilac.domain.dto.article.ArticleReviewRequest;
import com.lilac.domain.dto.article.ArticleUpdateRequest;
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
     * 获取文章列表（前台，只返回已审核文章）
     * @param articleQueryRequest 查询参数
     * @return 文章列表
     */
    Page<ArticleVO> listArticleByVOPage(ArticleQueryRequest articleQueryRequest);

    /**
     * 获取文章列表（管理员，不限制状态）
     * @param articleQueryRequest 查询参数
     * @return 文章列表
     */
    Page<ArticleVO> listArticleAdminVOPage(ArticleQueryRequest articleQueryRequest);

    /**
     * 获取查询条件
     * @param articleQueryRequest 获取查询条件参数
     * @return 查询条件
     */
    LambdaQueryWrapper<Article> getQueryWrapper(ArticleQueryRequest articleQueryRequest);

    /**
     * 获取文章详情
     *
     * @param articleUpdateRequest 修改参数
     * @return 文章详情
     */
    Boolean updateArticle(ArticleUpdateRequest articleUpdateRequest);

    /**
     * 删除文章
     *
     * @param id 文章id
     * @return 删除结果
     */
    Boolean deleteArticle(Long id);

    /**
     * 获取文章详情（含分类名称和标签）
     *
     * @param id 文章id
     * @return 文章VO
     */
    ArticleVO getArticleVO(Long id);

    /**
     * 普通用户提交文章（强制 status=1 待审核）
     *
     * @param request 提交参数
     * @return 文章id
     */
    long submitMyArticle(ArticleAddRequest request);

    /**
     * 普通用户更新自己的文章；若设置 status=1 则进入待审核并清空 rejectReason
     *
     * @param request 更新参数
     */
    Boolean updateMyArticle(ArticleUpdateRequest request);

    /**
     * 普通用户删除自己的文章
     *
     * @param id 文章ID
     * @return 删除结果
     */
    Boolean deleteMyArticle(Long id);

    /**
     * 分页查询当前登录用户提交的文章（含所有状态）
     *
     * @param request 查询参数
     * @return 文章列表
     */
    Page<ArticleVO> listMyArticles(ArticleQueryRequest request);

    /**
     * 管理员审核文章（通过/驳回），并产生站内消息
     *
     * @param request 审核参数
     * @return 审核结果
     */
    Boolean reviewArticle(ArticleReviewRequest request);

    /**
     * 获取文章详情并增加浏览量
     * @param id 文章ID
     * @return ArticleVO
     */
    ArticleVO getArticleVOWithView(Long id);

    /**
     * 获取文章总浏览量
     *
     * @return 总浏览量
     */
    long getTotalViewCount();
}
