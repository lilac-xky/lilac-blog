package com.lilac.constant;

/**
 * 系统公告常量
 */
public interface AnnouncementConstant {

    /**
     * 草稿
     */
    Integer STATUS_DRAFT = 0;

    /**
     * 已发布
     */
    Integer STATUS_PUBLISHED = 1;

    /**
     * 已下线
     */
    Integer STATUS_OFFLINE = 2;

    /**
     * 待读公告单次最多返回条数
     */
    int PENDING_LIMIT = 20;

    /**
     * 单次批量标记已读的最大条数
     */
    int READ_BATCH_LIMIT = 100;
}
