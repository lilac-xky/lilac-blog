package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.article.ArticleAddRequest;
import com.lilac.domain.dto.article.ArticleQueryRequest;
import com.lilac.domain.dto.article.ArticleUpdateRequest;
import com.lilac.domain.entity.Article;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.ArticleVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.impl.ArticleService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章接口
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    /**
     * 添加文章
     *
     * @param articleAddRequest 添加文章参数
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Long> addArticle(@RequestBody ArticleAddRequest articleAddRequest) {
        ThrowUtils.throwIf(articleAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        // todo 待处理标签
        long article = articleService.addArticle(articleAddRequest);
        return Result.success(article);
    }

    /**
     * 获取文章列表(管理员)
     *
     * @param articleQueryRequest 查询参数
     * @return 文章列表
     */
    @PostMapping("/list/page")
    public Result<Page<Article>> listArticleByPage(@RequestBody ArticleQueryRequest articleQueryRequest) {
        ThrowUtils.throwIf(articleQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long current = articleQueryRequest.getCurrent();
        long size = articleQueryRequest.getPageSize();
        // 执行分页查询
        Page<Article> articlePage = articleService.page(new Page<>(current, size), articleService.getQueryWrapper(articleQueryRequest));
        return Result.success(articlePage);
    }

    /**
     * 获取文章列表(前台)
     *
     * @param articleQueryRequest 查询参数
     * @return 文章列表
     */
    @PostMapping("/list/page/vo")
    public Result<Page<ArticleVO>> listArticleVOByPage(@RequestBody ArticleQueryRequest articleQueryRequest) {
        ThrowUtils.throwIf(articleQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long current = articleQueryRequest.getCurrent();
        long size = articleQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, HttpsCodeEnum.PARAMS_ERROR);
        // 执行分页查询
        Page<ArticleVO> articleVOPage = articleService.listArticleByVOPage(articleQueryRequest);
        return Result.success(articleVOPage);
    }

    /**
     * 更新文章
     *
     * @param articleUpdateRequest 更新参数
     * @return 更新结果
     */
    @PostMapping("/update")
    public Result<Boolean> updateArticle(@RequestBody ArticleUpdateRequest articleUpdateRequest) {
        ThrowUtils.throwIf(articleUpdateRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Article article = new Article();
        BeanUtils.copyProperties(articleUpdateRequest, article);
        // 判断是否有该文章
        Article oldArticle = articleService.getById(article.getId());
        ThrowUtils.throwIf(oldArticle == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        // 更新
        // todo 待处理标签
        boolean update = articleService.updateById(article);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(update);
    }

    /**
     * 删除文章
     *
     * @param deleteRequest 删除参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        // todo 待处理标签
        boolean delete = articleService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!delete, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success();
    }

    /**
     * 获取文章详情
     *
     * @param id 文章id
     * @return 文章
     */
    @PostMapping("/get")
    public Result<ArticleVO> getArticle(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        Article article = articleService.getById(id);
        ThrowUtils.throwIf(article == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        return Result.success(ArticleVO.objToVo(article));
    }
}
