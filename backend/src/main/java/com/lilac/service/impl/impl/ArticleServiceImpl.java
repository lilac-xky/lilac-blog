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
import com.lilac.domain.dto.article.ArticleReviewRequest;
import com.lilac.domain.dto.article.ArticleUpdateRequest;
import com.lilac.domain.entity.Article;
import com.lilac.domain.entity.ArticleTag;
import com.lilac.domain.entity.Category;
import com.lilac.domain.entity.Tag;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.ArticleVO;
import com.lilac.domain.vo.TagVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.redis.RedisService;
import com.lilac.service.impl.ArticleService;
import com.lilac.mapper.ArticleMapper;
import com.lilac.service.impl.ArticleTagService;
import com.lilac.service.impl.CategoryService;
import com.lilac.service.impl.MessageService;
import com.lilac.service.impl.TagService;
import com.lilac.service.impl.UserService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
    @Resource
    private CategoryService categoryService;
    @Resource
    private TagService tagService;
    @Resource
    private MessageService messageService;
    @Resource
    private RedisService redisService;

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
            articleAddRequest.setStatus(0);
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
     * 查询文章列表（前台，只返回已审核文章）
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
        Page<ArticleVO> articleVOPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        articleVOPage.setRecords(articlePage.getRecords().stream().map(ArticleVO::objToVo).toList());
        fillLatestViewCounts(articleVOPage.getRecords());
        fillArticleNames(articleVOPage.getRecords());
        return articleVOPage;
    }

    /**
     * 查询文章列表（管理员，不限制状态）
     *
     * @param articleQueryRequest 查询文章参数
     * @return 文章列表
     */
    @Override
    public Page<ArticleVO> listArticleAdminVOPage(ArticleQueryRequest articleQueryRequest) {
        ThrowUtils.throwIf(articleQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Page<Article> articlePage = this.page(
                new Page<>(articleQueryRequest.getCurrent(), articleQueryRequest.getPageSize()),
                getQueryWrapper(articleQueryRequest));
        Page<ArticleVO> articleVOPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        articleVOPage.setRecords(articlePage.getRecords().stream().map(ArticleVO::objToVo).toList());
        fillLatestViewCounts(articleVOPage.getRecords());
        fillArticleNames(articleVOPage.getRecords());
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
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), Article::getUserId, userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(isTop), Article::getIsTop, isTop);
        queryWrapper.eq(ObjUtil.isNotEmpty(status), Article::getStatus, status);
        queryWrapper.eq(ObjUtil.isNotEmpty(categoryId), Article::getCategoryId, categoryId);
        // 按标签过滤：先查关联表拿到文章ID列表
        List<Long> tagIds = articleQueryRequest.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> articleIdsByTags = articleTagService
                    .list(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getTagId, tagIds))
                    .stream().map(ArticleTag::getArticleId).distinct().toList();
            // 没有匹配文章时用 -1 确保结果为空而非全量
            queryWrapper.in(Article::getId, articleIdsByTags.isEmpty() ? List.of(-1L) : articleIdsByTags);
        }
        boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
        // 置顶恒置最前，其次按创建时间方向排
        queryWrapper.orderByDesc(Article::getIsTop)
                .orderBy(true, isAsc, Article::getCreateTime);
        return queryWrapper;
    }

    /**
     * 获取文章详情（含分类名称和标签）
     *
     * @param id 文章id
     * @return 文章VO
     */
    @Override
    public ArticleVO getArticleVO(Long id) {
        Article article = this.getById(id);
        ThrowUtils.throwIf(article == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        ArticleVO articleVO = ArticleVO.objToVo(article);
        fillArticleNames(List.of(articleVO));
        return articleVO;
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

    /**
     * 批量填充文章VO的分类名称、标签列表、作者信息（避免N+1查询）
     */
    private void fillArticleNames(List<ArticleVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        // 批量查分类名称
        List<Long> categoryIds = records.stream().map(ArticleVO::getCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> categoryNameMap = categoryIds.isEmpty() ? Collections.emptyMap() : categoryService.listByIds(categoryIds)
                .stream().collect(Collectors.toMap(Category::getId, Category::getCategoryName));

        // 批量查标签关联
        List<Long> articleIds = records.stream().map(ArticleVO::getId).toList();
        List<ArticleTag> articleTags = articleTagService.list(new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).distinct().toList();
        Map<Long, TagVO> tagVOMap = tagIds.isEmpty() ? Collections.emptyMap() : tagService.listByIds(tagIds)
                .stream().collect(Collectors.toMap(Tag::getId, TagVO::objToVo));
        Map<Long, List<TagVO>> articleTagMap = articleTags.stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId,
                        Collectors.mapping(at -> tagVOMap.get(at.getTagId()), Collectors.toList())));

        // 批量查作者信息
        List<Long> userIds = records.stream().map(ArticleVO::getUserId).filter(Objects::nonNull).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() : userService.listByIds(userIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));

        records.forEach(vo -> {
            vo.setCategoryName(categoryNameMap.get(vo.getCategoryId()));
            vo.setTags(articleTagMap.getOrDefault(vo.getId(), Collections.emptyList()));
            User author = userMap.get(vo.getUserId());
            if (author != null) {
                vo.setAuthorName(author.getUsername());
                vo.setAuthorAvatar(author.getAvatar());
            }
        });
    }

    /**
     * 普通用户提交文章：允许 status=0(草稿) 或 1(待审核)，其余非法值强制 1
     */
    @Override
    @Transactional
    public long submitMyArticle(ArticleAddRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        Integer status = request.getStatus();
        if (!Objects.equals(status, ArticleConstant.STATUS_DRAFT)
                && !Objects.equals(status, ArticleConstant.STATUS_AUDIT)) {
            request.setStatus(ArticleConstant.STATUS_AUDIT);
        }
        // 普通用户不允许置顶
        request.setIsTop(0);
        return addArticle(request);
    }

    /**
     * 普通用户更新自己的文章
     */
    @Override
    @Transactional
    public Boolean updateMyArticle(ArticleUpdateRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null, HttpsCodeEnum.PARAMS_ERROR);
        Article old = this.getById(request.getId());
        ThrowUtils.throwIf(old == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(!Objects.equals(old.getUserId(), loginUser.getId()), HttpsCodeEnum.OPERATION_ERROR, "无权限修改他人文章");
        // 已发布的文章用户不能直接改，避免绕过审核
        ThrowUtils.throwIf(Objects.equals(old.getStatus(), ArticleConstant.STATUS_PUBLISH),
                HttpsCodeEnum.OPERATION_ERROR, "已发布的文章请联系管理员修改");

        Article article = new Article();
        BeanUtils.copyProperties(request, article);
        // 用户不能改置顶
        article.setIsTop(null);
        // 用户只能把状态置为草稿(0)或待审核(1)，其余忽略
        if (request.getStatus() != null
                && !Objects.equals(request.getStatus(), ArticleConstant.STATUS_DRAFT)
                && !Objects.equals(request.getStatus(), ArticleConstant.STATUS_AUDIT)) {
            article.setStatus(null);
        }
        // 重新提交审核, 清空驳回原因
        if (Objects.equals(request.getStatus(), ArticleConstant.STATUS_AUDIT)) {
            article.setRejectReason("");
        }

        // 重写标签
        List<Long> tagIds = request.getTagIds();
        if (tagIds != null) {
            articleTagService.remove(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, article.getId()));
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                boolean saveTag = articleTagService.save(at);
                ThrowUtils.throwIf(!saveTag, HttpsCodeEnum.OPERATION_ERROR);
            }
        }
        boolean update = this.updateById(article);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }

    /**
     * 普通用户删除自己的文章
     */
    @Override
    @Transactional
    public Boolean deleteMyArticle(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        Article old = this.getById(id);
        ThrowUtils.throwIf(old == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(!Objects.equals(old.getUserId(), loginUser.getId()), HttpsCodeEnum.OPERATION_ERROR, "无权限删除他人文章");
        boolean remove = this.removeById(id);
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        ThrowUtils.throwIf(!remove, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }

    /**
     * 查询我的文章列表（含所有状态）
     */
    @Override
    public Page<ArticleVO> listMyArticles(ArticleQueryRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        request.setUserId(loginUser.getId());
        Page<Article> articlePage = this.page(new Page<>(request.getCurrent(), request.getPageSize()), getQueryWrapper(request));
        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        voPage.setRecords(articlePage.getRecords().stream().map(ArticleVO::objToVo).toList());
        fillLatestViewCounts(voPage.getRecords());
        fillArticleNames(voPage.getRecords());
        return voPage;
    }

    /**
     * 填充浏览量
     */
    private void fillLatestViewCounts(List<ArticleVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        records.forEach(vo -> {
            Long viewCount = redisService.getLong(ArticleConstant.VIEW_COUNT_KEY + vo.getId());
            if (viewCount != null) {
                vo.setViewCount(viewCount.intValue());
            }
        });
    }

    /**
     * 管理员审核文章：通过 / 驳回，写入站内消息
     */
    @Override
    @Transactional
    public Boolean reviewArticle(ArticleReviewRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getAction() == null, HttpsCodeEnum.PARAMS_ERROR);
        Article article = this.getById(request.getId());
        ThrowUtils.throwIf(article == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!Objects.equals(article.getStatus(), ArticleConstant.STATUS_AUDIT),
                HttpsCodeEnum.OPERATION_ERROR, "该文章不在待审核状态");

        int action = request.getAction();
        if (action == 1) {
            // 通过
            article.setStatus(ArticleConstant.STATUS_PUBLISH);
            article.setRejectReason("");
            boolean ok = this.updateById(article);
            ThrowUtils.throwIf(!ok, HttpsCodeEnum.OPERATION_ERROR);
            messageService.sendMessage(
                    article.getUserId(), 1,
                    "您的文章已通过审核",
                    "《" + (article.getTitle() == null ? "无标题" : article.getTitle()) + "》已成功发布",
                    article.getId(), "article");
        } else if (action == 2) {
            // 驳回
            String reason = request.getRejectReason();
            ThrowUtils.throwIf(StrUtil.isBlank(reason), HttpsCodeEnum.PARAMS_ERROR, "请填写驳回原因");
            article.setStatus(ArticleConstant.STATUS_DRAFT);
            article.setRejectReason(reason);
            boolean ok = this.updateById(article);
            ThrowUtils.throwIf(!ok, HttpsCodeEnum.OPERATION_ERROR);
            messageService.sendMessage(
                    article.getUserId(), 2,
                    "您的文章未通过审核",
                    "《" + (article.getTitle() == null ? "无标题" : article.getTitle()) + "》被驳回，原因：" + reason,
                    article.getId(), "article");
        } else {
            ThrowUtils.throwIf(true, HttpsCodeEnum.PARAMS_ERROR, "未知的审核动作");
        }
        return true;
    }

    /**
     * 获取文章详情并增加浏览量
     * @param id 文章id
     * @return 文章详情
     */
    @Override
    public ArticleVO getArticleVOWithView(Long id) {
        // 查询文章
        Article article = this.getById(id);
        ThrowUtils.throwIf(article == null, HttpsCodeEnum.NOT_FOUND_ERROR);

        // 获取key
        String viewKey = ArticleConstant.VIEW_COUNT_KEY + id;
        Long redisViews = redisService.getLong(viewKey);
        if (redisViews == null) {
            // 如果 Redis 没有，用数据库值回填
            int databaseViews = article.getViewCount() == null ? 0 : article.getViewCount();
            redisService.setIfAbsent(viewKey, String.valueOf(databaseViews), 4, TimeUnit.HOURS);
            redisViews = redisService.getLong(viewKey);
            if (redisViews == null) {
                redisViews = (long) databaseViews;
            }
        }

        // 只有审核通过的文章才增加浏览量
        if (Objects.equals(article.getStatus(), ArticleConstant.STATUS_PUBLISH)) {
            Long newCount = redisService.increment(viewKey);
            article.setViewCount(newCount.intValue());
        } else {
            // 未发布文章不增加，直接使用现有值
            article.setViewCount(redisViews.intValue());
        }

        // 转换 VO 并填充其他信息（分类、标签、作者）
        ArticleVO articleVO = ArticleVO.objToVo(article);
        fillArticleNames(List.of(articleVO));
        return articleVO;
    }

    /**
     * 获取文章总浏览量
     */
    @Override
    public long getTotalViewCount() {
        List<Article> publishedArticles = this.list(new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getViewCount)
                .eq(Article::getStatus, ArticleConstant.STATUS_PUBLISH));
        long total = 0L;
        for (Article article : publishedArticles) {
            Long viewCount = redisService.getLong(ArticleConstant.VIEW_COUNT_KEY + article.getId());
            total += viewCount != null ? viewCount : (article.getViewCount() == null ? 0L : article.getViewCount());
        }
        return total;
    }
}
