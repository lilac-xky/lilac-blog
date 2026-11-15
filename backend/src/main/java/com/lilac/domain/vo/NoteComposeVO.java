package com.lilac.domain.vo;

import lombok.Data;

/**
 * 碎片整理成文章的提交结果VO
 */
@Data
public class NoteComposeVO {

    /**
     * 生成的文章id，前端据此跳转到现有编辑器
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 本次归档的碎片条数
     */
    private Integer noteCount;
}
