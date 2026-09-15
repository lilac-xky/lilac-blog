package com.lilac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.domain.dto.spark.SparkAddRequest;
import com.lilac.domain.dto.spark.SparkQueryRequest;
import com.lilac.domain.entity.Spark;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.vo.SparkVO;

/**
* 灵感表服务类
*/
public interface SparkService extends IService<Spark> {

    /**
     * 添加灵感
     *
     * @param sparkAddRequest 添加灵感参数
     * @return 添加结果
     */
    long addSpark(SparkAddRequest sparkAddRequest);

    /**
     * 分页查询灵感列表（管理员，公开与私有全部可见）
     *
     * @param sparkQueryRequest 查询参数
     * @return 灵感VO分页结果
     */
    Page<Spark> listSparkAdminPage(SparkQueryRequest sparkQueryRequest);

    /**
     * 分页查询灵感列表（前台，只返回公开灵感 + 当前登录用户自己的私有灵感）
     *
     * @param sparkQueryRequest 查询参数
     * @return 灵感VO分页结果
     */
    Page<SparkVO> listSparkVOPage(SparkQueryRequest sparkQueryRequest);

    /**
     * 获取灵感详情（公开所有人可见，私有仅创建者本人可见）
     *
     * @param id 灵感id
     * @return 灵感VO
     */
    SparkVO getSparkVO(Long id);

    /**
     * 构建灵感查询条件
     *
     * @param sparkQueryRequest 查询参数
     * @return 查询条件
     */
    LambdaQueryWrapper<Spark> getQueryWrapper(SparkQueryRequest sparkQueryRequest);
}
