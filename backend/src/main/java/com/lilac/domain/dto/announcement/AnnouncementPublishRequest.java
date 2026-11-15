package com.lilac.domain.dto.announcement;

import lombok.Data;

import java.io.Serializable;

/**
 * 公告发布/下线请求
 */
@Data
public class AnnouncementPublishRequest implements Serializable {

    /**
     * 公告id
     */
    private Long id;

    /**
     * true 发布，false 下线
     */
    private Boolean publish;

    private static final long serialVersionUID = 1L;
}
