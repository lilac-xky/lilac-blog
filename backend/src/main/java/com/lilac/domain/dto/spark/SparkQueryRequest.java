package com.lilac.domain.dto.spark;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SparkQueryRequest extends PageRequest {
    /**
     * 灵感id
     */
    private Long id;

    /**
     * 灵感内容(模糊查询)
     */
    private String content;

    /**
     * 灵感状态(默认spark)
     */
    private String status;

    /**
     * 灵感是否公开(默认private，后台可用，前台会被服务端忽略)
     */
    private String visibility;

    /**
     * 创建人id(后台按归属人筛选)
     */
    private Long userId;
}
