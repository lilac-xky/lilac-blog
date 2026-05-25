package com.lilac.domain.dto.article;

import lombok.Data;

/**
 * 文章审核参数
 */
@Data
public class ArticleReviewRequest {
    /**
     * 文章id
     */
    Long id;

    /**
     * 操作类型  (1通过 2拒绝)
     */
    Integer action;

    /**
     * 拒绝理由
     */
    String rejectReason;
}
