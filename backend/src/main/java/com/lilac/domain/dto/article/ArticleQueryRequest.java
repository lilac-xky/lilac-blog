package com.lilac.domain.dto.article;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 查询文章参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleQueryRequest extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 关联分类
     */
    private Long categoryId;

    /**
     * 投稿人id
     */
    private Long userId;

    /**
     * 1置顶，0普通
     */
    private Integer isTop;

    /**
     * 0草稿，1待审核，2审核
     */
    private Integer status;

    /**
     * 标签列表
     */
    private List<Integer> tagIds;
}
