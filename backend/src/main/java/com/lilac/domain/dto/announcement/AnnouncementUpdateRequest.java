package com.lilac.domain.dto.announcement;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑公告请求
 */
@Data
public class AnnouncementUpdateRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}
