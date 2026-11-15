package com.lilac.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.constant.AnnouncementConstant;
import com.lilac.domain.dto.announcement.AnnouncementAddRequest;
import com.lilac.domain.dto.announcement.AnnouncementPublishRequest;
import com.lilac.domain.dto.announcement.AnnouncementQueryRequest;
import com.lilac.domain.dto.announcement.AnnouncementReadRequest;
import com.lilac.domain.dto.announcement.AnnouncementUpdateRequest;
import com.lilac.domain.entity.Announcement;
import com.lilac.domain.entity.AnnouncementRead;
import com.lilac.domain.vo.AnnouncementVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.auth.StpKit;
import com.lilac.mapper.AnnouncementMapper;
import com.lilac.mapper.AnnouncementReadMapper;
import com.lilac.service.AnnouncementService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统公告 Service 实现
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    /**
     * 标题最大长度
     */
    private static final int TITLE_MAX_LENGTH = 150;

    /**
     * 正文最大长度
     */
    private static final int CONTENT_MAX_LENGTH = 50000;

    @Resource
    private AnnouncementReadMapper announcementReadMapper;

    /**
     * 新增公告，默认保存为草稿
     *
     * @param request 新增参数
     * @return 公告id
     */
    @Override
    public long addAnnouncement(AnnouncementAddRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        String title = normalizeTitle(request.getTitle());
        String content = normalizeContent(request.getContent());
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setStatus(AnnouncementConstant.STATUS_DRAFT);
        boolean save = this.save(announcement);
        ThrowUtils.throwIf(!save, HttpsCodeEnum.OPERATION_ERROR, "公告保存失败");
        return announcement.getId();
    }

    /**
     * 编辑公告；已发布公告的已读记录不会因编辑而重置
     *
     * @param request 编辑参数
     * @return 是否成功
     */
    @Override
    public boolean updateAnnouncement(AnnouncementUpdateRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null, HttpsCodeEnum.PARAMS_ERROR);
        Announcement oldAnnouncement = this.getById(request.getId());
        ThrowUtils.throwIf(oldAnnouncement == null, HttpsCodeEnum.NOT_FOUND_ERROR, "公告不存在");
        String title = normalizeTitle(request.getTitle());
        String content = normalizeContent(request.getContent());
        Announcement announcement = new Announcement();
        announcement.setId(request.getId());
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setEditTime(new Date());
        boolean update = this.updateById(announcement);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR, "公告更新失败");
        return true;
    }

    /**
     * 逻辑删除公告
     *
     * @param id 公告id
     * @return 是否成功
     */
    @Override
    public boolean deleteAnnouncement(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        Announcement announcement = this.getById(id);
        ThrowUtils.throwIf(announcement == null, HttpsCodeEnum.NOT_FOUND_ERROR, "公告不存在");
        boolean delete = this.removeById(id);
        ThrowUtils.throwIf(!delete, HttpsCodeEnum.OPERATION_ERROR, "公告删除失败");
        return true;
    }

    /**
     * 发布或下线公告；从草稿/下线状态重新发布时刷新发布时间
     *
     * @param request 发布参数
     * @return 是否成功
     */
    @Override
    public boolean publishAnnouncement(AnnouncementPublishRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getPublish() == null,
                HttpsCodeEnum.PARAMS_ERROR);
        Announcement oldAnnouncement = this.getById(request.getId());
        ThrowUtils.throwIf(oldAnnouncement == null, HttpsCodeEnum.NOT_FOUND_ERROR, "公告不存在");
        Announcement announcement = new Announcement();
        announcement.setId(oldAnnouncement.getId());
        announcement.setEditTime(new Date());
        if (Boolean.TRUE.equals(request.getPublish())) {
            announcement.setStatus(AnnouncementConstant.STATUS_PUBLISHED);
            // 首次发布或下线后重新发布，都刷新发布时间，保证按发布时间倒序展示
            if (!AnnouncementConstant.STATUS_PUBLISHED.equals(oldAnnouncement.getStatus())
                    || oldAnnouncement.getPublishTime() == null) {
                announcement.setPublishTime(new Date());
            }
        } else {
            announcement.setStatus(AnnouncementConstant.STATUS_OFFLINE);
        }
        boolean update = this.updateById(announcement);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR, "公告状态更新失败");
        return true;
    }

    /**
     * 后台分页查询公告
     *
     * @param request 查询参数
     * @return 公告分页结果
     */
    @Override
    public Page<Announcement> listAnnouncementByPage(AnnouncementQueryRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<Announcement>()
                .eq(request.getId() != null, Announcement::getId, request.getId())
                .like(StrUtil.isNotBlank(request.getTitle()), Announcement::getTitle, request.getTitle())
                .eq(request.getStatus() != null, Announcement::getStatus, request.getStatus())
                .orderByDesc(Announcement::getCreateTime);
        return this.page(new Page<>(request.getCurrent(), request.getPageSize()), queryWrapper);
    }

    /**
     * 查询公告详情（后台，草稿也可查询）
     *
     * @param id 公告id
     * @return 公告实体
     */
    @Override
    public Announcement getAnnouncement(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        Announcement announcement = this.getById(id);
        ThrowUtils.throwIf(announcement == null, HttpsCodeEnum.NOT_FOUND_ERROR, "公告不存在");
        return announcement;
    }

    /**
     * 查询当前用户待读公告
     * 已登录用户排除 announcement_read 中已读记录；游客返回已发布公告，由前端按 localStorage 过滤
     *
     * @return 待读公告列表
     */
    @Override
    public List<AnnouncementVO> listPendingAnnouncements() {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, AnnouncementConstant.STATUS_PUBLISHED)
                .isNotNull(Announcement::getPublishTime)
                .le(Announcement::getPublishTime, new Date())
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getId)
                .last("LIMIT " + AnnouncementConstant.PENDING_LIMIT);
        if (userId != null) {
            // NOT EXISTS 子查询按用户排除已读公告，避免把已读 id 全量查回内存
            queryWrapper.apply(
                    "NOT EXISTS (SELECT 1 FROM announcement_read ar WHERE ar.announcementId = announcement.id AND ar.userId = {0})",
                    userId);
        }
        return this.list(queryWrapper).stream().map(AnnouncementVO::objToVo).toList();
    }

    /**
     * 查询全部已发布公告（公告中心回看用）
     * 已登录用户填充 isRead，游客不填充（前端按 localStorage 判断）
     *
     * @return 已发布公告列表，按发布时间倒序
     */
    @Override
    public List<AnnouncementVO> listPublishedAnnouncements() {
        List<Announcement> announcements = this.list(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, AnnouncementConstant.STATUS_PUBLISHED)
                .isNotNull(Announcement::getPublishTime)
                .le(Announcement::getPublishTime, new Date())
                .orderByDesc(Announcement::getPublishTime)
                .orderByDesc(Announcement::getId)
                .last("LIMIT " + AnnouncementConstant.PUBLISHED_LIMIT));
        List<AnnouncementVO> voList = announcements.stream().map(AnnouncementVO::objToVo).toList();
        Long userId = getCurrentUserId();
        if (userId == null || voList.isEmpty()) {
            return voList;
        }
        // 已登录用户一次性查出已读集合，避免逐条查库
        List<Long> announcementIds = announcements.stream().map(Announcement::getId).toList();
        Set<Long> readIds = announcementReadMapper.selectList(new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getUserId, userId)
                        .in(AnnouncementRead::getAnnouncementId, announcementIds))
                .stream()
                .map(AnnouncementRead::getAnnouncementId)
                .collect(Collectors.toSet());
        voList.forEach(vo -> vo.setIsRead(readIds.contains(vo.getId())));
        return voList;
    }

    /**
     * 批量标记公告已读，重复标记幂等
     *
     * @param request 已读参数
     * @return 新增已读记录数
     */
    @Override
    public int markRead(AnnouncementReadRequest request) {
        ThrowUtils.throwIf(request == null || request.getAnnouncementIds() == null
                || request.getAnnouncementIds().isEmpty(), HttpsCodeEnum.PARAMS_ERROR, "公告ID不能为空");
        List<Long> announcementIds = request.getAnnouncementIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        ThrowUtils.throwIf(announcementIds.isEmpty(), HttpsCodeEnum.PARAMS_ERROR, "公告ID不能为空");
        ThrowUtils.throwIf(announcementIds.size() > AnnouncementConstant.READ_BATCH_LIMIT,
                HttpsCodeEnum.PARAMS_ERROR, "一次最多标记" + AnnouncementConstant.READ_BATCH_LIMIT + "条公告");
        Long userId = getCurrentUserId();
        ThrowUtils.throwIf(userId == null, HttpsCodeEnum.NEED_LOGIN);
        // 只允许标记已发布公告，避免把草稿/下线公告写成已读
        List<Announcement> publishedAnnouncements = this.list(new LambdaQueryWrapper<Announcement>()
                .in(Announcement::getId, announcementIds)
                .eq(Announcement::getStatus, AnnouncementConstant.STATUS_PUBLISHED));
        if (publishedAnnouncements.isEmpty()) {
            return 0;
        }
        List<Long> publishedIds = publishedAnnouncements.stream().map(Announcement::getId).toList();
        // 先查已有记录，减少无谓的插入和唯一键冲突
        Set<Long> readIds = announcementReadMapper.selectList(new LambdaQueryWrapper<AnnouncementRead>()
                        .eq(AnnouncementRead::getUserId, userId)
                        .in(AnnouncementRead::getAnnouncementId, publishedIds))
                .stream()
                .map(AnnouncementRead::getAnnouncementId)
                .collect(Collectors.toSet());
        int inserted = 0;
        for (Long announcementId : publishedIds) {
            if (readIds.contains(announcementId)) {
                continue;
            }
            AnnouncementRead announcementRead = new AnnouncementRead();
            announcementRead.setAnnouncementId(announcementId);
            announcementRead.setUserId(userId);
            try {
                inserted += announcementReadMapper.insert(announcementRead);
            } catch (DataIntegrityViolationException e) {
                // 并发下唯一键兜底：记录已存在时视为已读成功
            }
        }
        return inserted;
    }

    /**
     * 获取当前登录的普通用户id；未登录返回 null，管理员会话不参与公告已读
     *
     * @return 当前普通用户id
     */
    private Long getCurrentUserId() {
        if (!StpKit.USER.isLogin()) {
            return null;
        }
        Object loginId = StpKit.USER.getLoginId();
        return loginId == null ? null : Long.valueOf(loginId.toString());
    }

    /**
     * 校验并规范化公告标题
     *
     * @param title 原始标题
     * @return 去除首尾空白后的标题
     */
    private String normalizeTitle(String title) {
        String normalizedTitle = StrUtil.trim(title);
        ThrowUtils.throwIf(StrUtil.isBlank(normalizedTitle), HttpsCodeEnum.PARAMS_ERROR, "公告标题不能为空");
        ThrowUtils.throwIf(normalizedTitle.length() > TITLE_MAX_LENGTH, HttpsCodeEnum.PARAMS_ERROR,
                "公告标题不能超过" + TITLE_MAX_LENGTH + "个字符");
        return normalizedTitle;
    }

    /**
     * 校验公告正文；Markdown 原文保留首尾空白，避免破坏代码块等格式
     *
     * @param content 原始正文
     * @return 原始正文
     */
    private String normalizeContent(String content) {
        ThrowUtils.throwIf(StrUtil.isBlank(content), HttpsCodeEnum.PARAMS_ERROR, "公告内容不能为空");
        ThrowUtils.throwIf(content.length() > CONTENT_MAX_LENGTH, HttpsCodeEnum.PARAMS_ERROR,
                "公告内容不能超过" + CONTENT_MAX_LENGTH + "个字符");
        return content;
    }
}
