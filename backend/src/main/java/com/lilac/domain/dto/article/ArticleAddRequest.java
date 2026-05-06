package com.lilac.domain.dto.article;

import lombok.Data;

import java.util.List;

/**
 * 添加文章请求
 */
@Data
public class ArticleAddRequest {

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 关联分类
     */
    private Long categoryId;

    /**
     * 封面图片URL（WebP缩略图）
     */
    private String coverUrl;

    /**
     * 0草稿，1待审核，2审核
     */
    private Integer status;

    /**
     * 1置顶，0普通
     */
    private Integer isTop;

    /**
     * 标签列表
     */
    private List<Integer> tagIds;
}
