package com.lilac.domain.vo;

import cn.hutool.core.bean.BeanUtil;
import com.lilac.domain.entity.Announcement;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统公告 VO
 */
@Data
public class AnnouncementVO implements Serializable {

    /**
     * 公告id
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告正文（Markdown）
     */
    private String content;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 当前用户是否已读（仅公告中心回看接口填充；待读列表里恒为 null）
     */
    private Boolean isRead;

    /**
     * 公告实体转 VO
     *
     * @param announcement 公告实体
     * @return 公告 VO
     */
    public static AnnouncementVO objToVo(Announcement announcement) {
        if (announcement == null) {
            return null;
        }
        AnnouncementVO announcementVO = new AnnouncementVO();
        BeanUtil.copyProperties(announcement, announcementVO);
        return announcementVO;
    }

    private static final long serialVersionUID = 1L;
}
