package com.lilac.domain.dto.note;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 整理碎片成文章的请求（预览与生成共用）
 */
@Data
public class NoteAggregateRequest {

    /**
     * 方式一：指定要整理的碎片id列表，优先于时间区间
     */
    private List<Long> noteIds;

    /**
     * 方式二：按创建时间区间筛选，起始时间（含）
     */
    private Date fromTime;

    /**
     * 方式二：按创建时间区间筛选，结束时间（含）
     */
    private Date toTime;

    /**
     * 可选：只整理这些主题下的碎片；为空表示不限主题
     */
    private List<Long> tagIds;

    /**
     * 可选：自定义标题；为空时由服务端按规则生成
     */
    private String title;

    /**
     * 可选：是否在小节之间插入过渡提示注释，默认 true
     */
    private Boolean insertHints;
}
