package com.lilac.domain.dto.announcement;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 公告分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnnouncementQueryRequest extends PageRequest implements Serializable {

    /**
     * 公告id
     */
    private Long id;

    /**
     * 公告标题（模糊查询）
     */
    private String title;

    /**
     * 状态：0草稿 1已发布 2已下线
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
