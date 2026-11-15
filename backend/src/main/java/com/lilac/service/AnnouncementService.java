package com.lilac.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.dto.announcement.AnnouncementAddRequest;
import com.lilac.domain.dto.announcement.AnnouncementPublishRequest;
import com.lilac.domain.dto.announcement.AnnouncementQueryRequest;
import com.lilac.domain.dto.announcement.AnnouncementReadRequest;
import com.lilac.domain.dto.announcement.AnnouncementUpdateRequest;
import com.lilac.domain.entity.Announcement;
import com.lilac.domain.vo.AnnouncementVO;

import java.util.List;

/**
 * 系统公告 Service
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 新增公告（默认草稿）
     *
     * @param request 新增参数
     * @return 公告id
     */
    long addAnnouncement(AnnouncementAddRequest request);

    /**
     * 编辑公告
     *
     * @param request 编辑参数
     * @return 是否成功
     */
    boolean updateAnnouncement(AnnouncementUpdateRequest request);

    /**
     * 逻辑删除公告
     *
     * @param id 公告id
     * @return 是否成功
     */
    boolean deleteAnnouncement(Long id);

    /**
     * 发布或下线公告
     *
     * @param request 发布参数
     * @return 是否成功
     */
    boolean publishAnnouncement(AnnouncementPublishRequest request);

    /**
     * 后台分页查询公告
     *
     * @param request 查询参数
     * @return 公告分页结果
     */
    Page<Announcement> listAnnouncementByPage(AnnouncementQueryRequest request);

    /**
     * 查询公告详情（后台）
     *
     * @param id 公告id
     * @return 公告实体
     */
    Announcement getAnnouncement(Long id);

    /**
     * 查询当前用户待读公告；游客返回已发布公告，由前端本地过滤已读
     *
     * @return 待读公告列表
     */
    List<AnnouncementVO> listPendingAnnouncements();

    /**
     * 查询全部已发布公告（公告中心回看用，已读已下线不返回）
     * 已登录用户会填充 isRead，游客恒为 null（前台按 localStorage 判断）
     *
     * @return 已发布公告列表，按发布时间倒序
     */
    List<AnnouncementVO> listPublishedAnnouncements();

    /**
     * 批量标记公告已读（登录用户）
     *
     * @param request 已读参数
     * @return 新增已读记录数
     */
    int markRead(AnnouncementReadRequest request);
}
