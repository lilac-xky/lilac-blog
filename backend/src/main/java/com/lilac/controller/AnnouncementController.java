package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.announcement.AnnouncementAddRequest;
import com.lilac.domain.dto.announcement.AnnouncementPublishRequest;
import com.lilac.domain.dto.announcement.AnnouncementQueryRequest;
import com.lilac.domain.dto.announcement.AnnouncementReadRequest;
import com.lilac.domain.dto.announcement.AnnouncementUpdateRequest;
import com.lilac.domain.entity.Announcement;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.AnnouncementVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.auth.anotation.SaAdminPermission;
import com.lilac.manager.auth.anotation.SaUserCheckLogin;
import com.lilac.service.AnnouncementService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统公告接口
 */
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    /**
     * 新增公告（默认草稿）
     *
     * @param request 新增参数
     * @return 公告id
     */
    @PostMapping("/add")
    @SaAdminPermission("announcement:add")
    public Result<Long> addAnnouncement(@RequestBody AnnouncementAddRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(announcementService.addAnnouncement(request));
    }

    /**
     * 编辑公告
     *
     * @param request 编辑参数
     * @return 是否成功
     */
    @PostMapping("/update")
    @SaAdminPermission("announcement:update")
    public Result<Boolean> updateAnnouncement(@RequestBody AnnouncementUpdateRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(announcementService.updateAnnouncement(request));
    }

    /**
     * 删除公告
     *
     * @param deleteRequest 删除参数
     * @return 是否成功
     */
    @PostMapping("/delete")
    @SaAdminPermission("announcement:delete")
    public Result<Boolean> deleteAnnouncement(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(announcementService.deleteAnnouncement(deleteRequest.getId()));
    }

    /**
     * 发布或下线公告
     *
     * @param request 发布参数
     * @return 是否成功
     */
    @PostMapping("/publish")
    @SaAdminPermission("announcement:publish")
    public Result<Boolean> publishAnnouncement(@RequestBody AnnouncementPublishRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(announcementService.publishAnnouncement(request));
    }

    /**
     * 后台分页查询公告
     *
     * @param request 查询参数
     * @return 公告分页结果
     */
    @PostMapping("/list/page")
    @SaAdminPermission("announcement:list")
    public Result<Page<Announcement>> listAnnouncementByPage(@RequestBody AnnouncementQueryRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(announcementService.listAnnouncementByPage(request));
    }

    /**
     * 后台查询公告详情
     *
     * @param id 公告id
     * @return 公告详情
     */
    @GetMapping("/get")
    @SaAdminPermission("announcement:list")
    public Result<Announcement> getAnnouncement(@RequestParam("id") Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(announcementService.getAnnouncement(id));
    }

    /**
     * 查询当前待读公告
     * 已登录用户返回未读公告；游客返回已发布公告，由前端 localStorage 过滤已读
     *
     * @return 待读公告列表
     */
    @GetMapping("/pending")
    public Result<List<AnnouncementVO>> listPendingAnnouncements() {
        return Result.success(announcementService.listPendingAnnouncements());
    }

    /**
     * 查询全部已发布公告（公告中心回看）
     * 已登录用户填充 isRead；游客不填充，由前端 localStorage 判断
     *
     * @return 已发布公告列表
     */
    @GetMapping("/published")
    public Result<List<AnnouncementVO>> listPublishedAnnouncements() {
        return Result.success(announcementService.listPublishedAnnouncements());
    }

    /**
     * 批量标记公告已读（仅登录用户）
     *
     * @param request 已读参数
     * @return 新增已读记录数
     */
    @PostMapping("/read")
    @SaUserCheckLogin
    public Result<Integer> markRead(@RequestBody AnnouncementReadRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(announcementService.markRead(request));
    }
}
