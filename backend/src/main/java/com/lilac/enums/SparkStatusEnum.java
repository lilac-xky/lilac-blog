package com.lilac.enums;

import com.lilac.exception.BusinessException;
import lombok.Getter;

/**
 * 灵感状态枚举
 */
@Getter
public enum SparkStatusEnum {
    SPARK("spark", "一闪"),
    BREWING("brewing", "酝酿中"),
    DOING("doing", "进行中"),
    DONE("done", "已完成"),
    PAUSED("paused", "搁置"),
    DROPPED("dropped", "废弃");

    /**
     * 状态编码，与 spark.status 字段取值一致
     */
    private final String status;

    /**
     * 状态中文描述，仅用于展示
     */
    private final String label;

    SparkStatusEnum(String status, String label) {
        this.status = status;
        this.label = label;
    }

    /**
     * 根据状态编码获取灵感状态
     *
     * @param status 状态编码
     * @return 灵感状态
     */
    public static SparkStatusEnum fromStatus(String status) {
        for (SparkStatusEnum e : values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "不支持的灵感状态: " + status);
    }
}
