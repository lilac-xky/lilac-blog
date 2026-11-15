package com.lilac.domain.dto.announcement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 公告标记已读请求
 */
@Data
public class AnnouncementReadRequest implements Serializable {

    /**
     * 需要标记已读的公告id列表
     */
    private List<Long> announcementIds;

    private static final long serialVersionUID = 1L;
}
