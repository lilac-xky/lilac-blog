package com.lilac.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 主题分布统计VO，供整理页做入口展示
 */
@Data
public class NoteTopicStatVO {

    /**
     * 未整理的碎片总数
     */
    private Integer rawTotal;

    /**
     * 已归档的碎片总数
     */
    private Integer archivedTotal;

    /**
     * 各主题下未整理的碎片数，按数量降序
     */
    private List<Item> topics;

    /**
     * 单个主题的统计项
     */
    @Data
    public static class Item {

        /**
         * 标签id；未挂主题的碎片为 null
         */
        private Long tagId;

        /**
         * 标签名；未挂主题时为「杂项」
         */
        private String tagName;

        /**
         * 该主题下未整理的碎片数
         */
        private Integer count;
    }
}
