package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.article.ArticleAddRequest;
import com.lilac.domain.dto.article.ArticleQueryRequest;
import com.lilac.domain.dto.article.ArticleUpdateRequest;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.ArticleVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.auth.anotation.SaAdminPermission;
import com.lilac.service.impl.ArticleService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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
    @SaAdminPermission("article:add")
    public Result<Long> addArticle(@RequestBody ArticleAddRequest articleAddRequest) {
        ThrowUtils.throwIf(articleAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
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
    @SaAdminPermission("article:list")
    public Result<Page<ArticleVO>> listArticleByPage(@RequestBody ArticleQueryRequest articleQueryRequest) {
        ThrowUtils.throwIf(articleQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Page<ArticleVO> articleVOPage = articleService.listArticleAdminVOPage(articleQueryRequest);
        return Result.success(articleVOPage);
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
    @SaAdminPermission("article:update")
    public Result<Boolean> updateArticle(@RequestBody ArticleUpdateRequest articleUpdateRequest) {
        ThrowUtils.throwIf(articleUpdateRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean update = articleService.updateArticle(articleUpdateRequest);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 删除文章
     *
     * @param deleteRequest 删除参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaAdminPermission("article:delete")
    public Result<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean delete = articleService.deleteArticle(deleteRequest.getId());
        ThrowUtils.throwIf(!delete, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 获取文章详情
     *
     * @param id 文章id
     * @return 文章
     */
    @GetMapping("/get")
    public Result<ArticleVO> getArticle(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(articleService.getArticleVO(id));
    }
}
