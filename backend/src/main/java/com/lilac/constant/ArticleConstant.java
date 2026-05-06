package com.lilac.constant;

/**
 * 文章常量
 */
public interface ArticleConstant {

    /**
     * 草稿
     */
    Integer STATUS_DRAFT = 0;

    /**
     * 待审核
     */
    Integer STATUS_AUDIT = 1;

    /**
     * 审核通过
     */
    Integer STATUS_PUBLISH = 2;
}
