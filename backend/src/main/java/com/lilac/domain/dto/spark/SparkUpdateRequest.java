package com.lilac.domain.dto.spark;

import lombok.Data;

/**
 * 添加标签请求
 */
@Data
public class SparkUpdateRequest {
    /**
     * 灵感id
     */
    private Long id;

    /**
     * 灵感内容
     */
    private String content;

    /**
     * 灵感状态(默认spark)
     */
    private String status;

    /**
     * 灵感是否公开(默认private)
     */
    private String visibility;
}
