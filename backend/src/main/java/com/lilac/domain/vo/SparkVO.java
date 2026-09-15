package com.lilac.domain.vo;

import com.lilac.domain.entity.Spark;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 灵感VO
 */
@Data
public class SparkVO {
    /**
     * id
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

    /**
     * 创建人id(灵感归属人)
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 灵感VO对象转换
     * @param spark 灵感对象
     * @return 灵感VO对象
     */
    public static SparkVO objToVo(Spark spark) {
        if (spark == null) return null;
        SparkVO sparkVO = new SparkVO();
        BeanUtils.copyProperties(spark, sparkVO);
        return sparkVO;
    }

    /**
     * 灵感VO对象转换
     * @param sparkVO 灵感VO对象
     * @return 灵感对象
     */
    public static Spark voToObj(SparkVO sparkVO) {
        if (sparkVO == null) return null;
        Spark spark = new Spark();
        BeanUtils.copyProperties(sparkVO, spark);
        return spark;
    }
}
