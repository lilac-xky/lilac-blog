package com.lilac.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 碎片整理预览结果VO
 */
@Data
public class NoteAggregateVO {

    /**
     * 建议的文章标题（请求里传了标题则以请求为准）
     */
    private String title;

    /**
     * 生成好的 Markdown 骨架，尚未落库
     */
    private String markdown;

    /**
     * 参与整理的碎片条数
     */
    private Integer noteCount;

    /**
     * 其中未挂主题、被归入「杂项」的条数
     */
    private Integer untaggedCount;

    /**
     * 主题分布，按组内碎片数降序
     */
    private List<TopicItem> topics;

    /**
     * 本次整理涉及的主题标签id（生成草稿时会写入文章标签）
     */
    private List<Long> tagIds;

    /**
     * 参与整理的碎片id，便于前端高亮或二次确认
     */
    private List<Long> noteIds;

    /**
     * 单个主题的统计信息
     */
    @Data
    public static class TopicItem {

        /**
         * 标签id；未挂主题的「杂项」为 null
         */
        private Long tagId;

        /**
         * 标签名；未挂主题时为「杂项」
         */
        private String tagName;

        /**
         * 该主题下的碎片条数
         */
        private Integer count;
    }
}
