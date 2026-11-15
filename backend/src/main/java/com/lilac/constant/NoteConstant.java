package com.lilac.constant;

/**
 * 碎知（知识碎片）常量
 */
public interface NoteConstant {

    /**
     * 未整理：还没被整理进任何文章
     */
    Integer STATUS_RAW = 0;

    /**
     * 已归档：已经被整理进某篇文章，articleId 记录去向
     */
    Integer STATUS_ARCHIVED = 1;

    /**
     * 未挂主题的碎片在整理结果里的分组标题
     */
    String UNTAGGED_GROUP_NAME = "杂项";

    /**
     * 正文最大长度，超出直接拒绝，避免把 TEXT 字段撑爆
     */
    int CONTENT_MAX_LENGTH = 10000;

    /**
     * 一句话理解最大长度，与 note.insight 的 VARCHAR(500) 保持一致
     */
    int INSIGHT_MAX_LENGTH = 500;

    /**
     * 来源最大长度，与 note.source 的 VARCHAR(255) 保持一致
     */
    int SOURCE_MAX_LENGTH = 255;

    /**
     * 单次整理允许的碎片条数上限，防止一次生成过长的 Markdown
     */
    int AGGREGATE_MAX_SIZE = 200;

    /**
     * 主题统计时最多扫描的未整理碎片条数，避免个人博客场景下全表扫描
     */
    int TOPIC_STAT_SCAN_LIMIT = 2000;
}
