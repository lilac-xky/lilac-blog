package com.lilac.constant;

import java.util.List;

/**
 * 文件常量
 */
public interface FileConstant {

    /**
     * 文件上传最大限制
     */
    long MAX_SIZE = 5L * 1024 * 1024;

    /**
     * 允许的文件格式
     */
    List<String> ALLOW_FORMAT_LIST = List.of("png", "jpg", "jpeg", "webp");

    /**
     * 读取文件超时时间
     */
    int TIMEOUT_MS = 10_000;

    /**
     * 项目名
     */
    String PROJECT_NAME = "lilac-blog";

    /**
     * 缩略图样式
     */
    String THUMBNAIL_STYLE = "x-oss-process=image/resize,w_200/format,webp";

}
