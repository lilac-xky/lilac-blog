package com.lilac.domain.dto.spark;

import lombok.Data;

/**
 * 添加灵感请求
 */
@Data
public class SparkAddRequest {

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
