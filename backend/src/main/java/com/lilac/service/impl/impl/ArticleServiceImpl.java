package com.lilac.service.impl.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.constant.ArticleConstant;
import com.lilac.domain.dto.article.ArticleAddRequest;
import com.lilac.domain.dto.article.ArticleQueryRequest;
import com.lilac.domain.dto.article.ArticleUpdateRequest;
import com.lilac.domain.entity.Article;
import com.lilac.domain.entity.ArticleTag;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.ArticleVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.impl.ArticleService;
import com.lilac.mapper.ArticleMapper;
import com.lilac.service.impl.ArticleTagService;
import com.lilac.service.impl.UserService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章Service实现
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private UserService userService;
    @Resource
    private ArticleTagService articleTagService;

    /**
     * 添加文章
     *
     * @param articleAddRequest 添加文章参数
     * @return 添加结果
     */
    @Override
    @Transactional
    public long addArticle(ArticleAddRequest articleAddRequest) {
        ThrowUtils.throwIf(articleAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Article article = new Article();
        BeanUtil.copyProperties(articleAddRequest, article);
        User loginUser = userService.getLoginUser();
        article.setUserId(loginUser.getId());
        if (articleAddRequest.getStatus() == null) {
            article.setStatus(0);
        }
        if (userService.isAdmin() && articleAddRequest.getStatus() != 0) {
            article.setStatus(2);
        }
        if (!userService.isAdmin() && articleAddRequest.getStatus() != 0) {
            article.setStatus(1);
        }
        article.setViewCount(0);
        boolean save = this.save(article);
        ThrowUtils.throwIf(!save, HttpsCodeEnum.OPERATION_ERROR);
        // 保存标签关联
        List<Long> tagIds = articleAddRequest.getTagIds();
        if (articleAddRequest.getTagIds() != null && !articleAddRequest.getTagIds().isEmpty()) {
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                boolean saveTag = articleTagService.save(articleTag);
                ThrowUtils.throwIf(!saveTag, HttpsCodeEnum.OPERATION_ERROR);
            }
        }
        return article.getId();
    }

    /**
     * 查询文章列表
     *
     * @param articleQueryRequest 查询文章参数
     * @return 文章列表
     */
    @Override
    public Page<ArticleVO> listArticleByVOPage(ArticleQueryRequest articleQueryRequest) {
        ThrowUtils.throwIf(articleQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        // 前台必须是审核通过的文章
        if (articleQueryRequest.getStatus() == null || !articleQueryRequest.getStatus().equals(ArticleConstant.STATUS_PUBLISH)) {
            articleQueryRequest.setStatus(ArticleConstant.STATUS_PUBLISH);
        }
        Page<Article> articlePage = this.page(new Page<>(articleQueryRequest.getCurrent(),
                articleQueryRequest.getPageSize()), getQueryWrapper(articleQueryRequest));
        // 转换为 VO
        Page<ArticleVO> articleVOPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        articleVOPage.setRecords(articlePage.getRecords().stream().map(ArticleVO::objToVo).toList());
        return articleVOPage;
    }

    /**
     * 获取查询条件
     *
     * @param articleQueryRequest 查询条件
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<Article> getQueryWrapper(ArticleQueryRequest articleQueryRequest) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        ThrowUtils.throwIf(articleQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Long id = articleQueryRequest.getId();
        String title = articleQueryRequest.getTitle();
        Long categoryId = articleQueryRequest.getCategoryId();
        Long userId = articleQueryRequest.getUserId();
        Integer isTop = articleQueryRequest.getIsTop();
        Integer status = articleQueryRequest.getStatus();
        String sortOrder = articleQueryRequest.getSortOrder();
        // 拼装条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Article::getId, id);
        queryWrapper.like(StrUtil.isNotBlank(title), Article::getTitle, title);
        queryWrapper.like(ObjUtil.isNotEmpty(userId), Article::getUserId, userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(isTop), Article::getIsTop, isTop);
        queryWrapper.eq(ObjUtil.isNotEmpty(status), Article::getStatus, status);
        queryWrapper.eq(ObjUtil.isNotEmpty(categoryId), Article::getCategoryId, categoryId);
        // todo 标签待处理
        // 排序处理
        boolean isDesc = "descend".equalsIgnoreCase(sortOrder);
        queryWrapper.orderBy(true, isDesc, Article::getCreateTime);
        return queryWrapper;
    }

    /**
     * 更新文章
     *
     * @param articleUpdateRequest 更新文章参数
     * @return 更新结果
     */
    @Override
    @Transactional
    public Boolean updateArticle(ArticleUpdateRequest articleUpdateRequest) {
        Article article = new Article();
        BeanUtils.copyProperties(articleUpdateRequest, article);
        // 判断是否有该文章
        Article oldArticle = this.getById(article.getId());
        ThrowUtils.throwIf(oldArticle == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        // 先删除标签在新增
        List<Long> tagIds = articleUpdateRequest.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            articleTagService.remove(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
            for (Long tagId : tagIds) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                boolean saveTag = articleTagService.save(articleTag);
                ThrowUtils.throwIf(!saveTag, HttpsCodeEnum.OPERATION_ERROR);
            }
        }
        boolean update = this.updateById(article);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }

    /**
     * 删除文章
     *
     * @param id 文章id
     * @return 删除结果
     */
    @Override
    @Transactional
    public Boolean deleteArticle(Long id) {
        Article article = this.getById(id);
        ThrowUtils.throwIf(article == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        boolean remove = this.removeById(id);
        // 删除关联标签
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        ThrowUtils.throwIf(!remove, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }
}

