package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.spark.SparkAddRequest;
import com.lilac.domain.dto.spark.SparkQueryRequest;
import com.lilac.domain.dto.spark.SparkUpdateRequest;
import com.lilac.domain.entity.Spark;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.SparkVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.auth.anotation.SaAdminPermission;
import com.lilac.service.SparkService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 灵感接口
 */
@RestController
@RequestMapping("/spark")
public class SparkController {

    @Resource
    private SparkService sparkService;

    /**
     * 添加灵感
     *
     * @param sparkAddRequest 添加灵感参数
     * @return 添加结果
     */
    @PostMapping("/add")
    @SaAdminPermission("spark:add")
    public Result<Long> addSpark(@RequestBody SparkAddRequest sparkAddRequest) {
        ThrowUtils.throwIf(sparkAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long spark = sparkService.addSpark(sparkAddRequest);
        return Result.success(spark);
    }

    /**
     * 删除灵感
     *
     * @param deleteRequest 删除灵感参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaAdminPermission("spark:delete")
    public Result<Boolean> deleteSpark(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean delete = sparkService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!delete, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 修改灵感
     *
     * @param sparkUpdateRequest 修改灵感参数
     * @return 修改结果
     */
    @PostMapping("/update")
    @SaAdminPermission("spark:update")
    public Result<Boolean> updateSpark(@RequestBody SparkUpdateRequest sparkUpdateRequest) {
        ThrowUtils.throwIf(sparkUpdateRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Spark spark = new Spark();
        BeanUtils.copyProperties(sparkUpdateRequest, spark);
        boolean update = sparkService.updateById(spark);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 获取灵感列表(管理员，公开与私有全部可见)
     *
     * @param sparkQueryRequest 查询参数
     * @return 灵感列表
     */
    @PostMapping("/list/page")
    @SaAdminPermission("spark:list")
    public Result<Page<Spark>> listSparkByPage(@RequestBody SparkQueryRequest sparkQueryRequest) {
        ThrowUtils.throwIf(sparkQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Page<Spark> sparkPage = sparkService.listSparkAdminPage(sparkQueryRequest);
        return Result.success(sparkPage);
    }

    /**
     * 获取灵感列表(前台，只返回公开灵感 + 本人私有灵感)
     *
     * @param sparkQueryRequest 查询参数
     * @return 灵感列表
     */
    @PostMapping("/list/page/vo")
    public Result<Page<SparkVO>> listSparkVOByPage(@RequestBody SparkQueryRequest sparkQueryRequest) {
        ThrowUtils.throwIf(sparkQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long size = sparkQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, HttpsCodeEnum.PARAMS_ERROR);
        Page<SparkVO> sparkVOPage = sparkService.listSparkVOPage(sparkQueryRequest);
        return Result.success(sparkVOPage);
    }

    /**
     * 获取灵感详情(公开灵感人人可见，私有灵感仅创建者本人可见)
     *
     * @param id 灵感id
     * @return 灵感详情
     */
    @GetMapping("/get")
    public Result<SparkVO> getSpark(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        SparkVO sparkVO = sparkService.getSparkVO(id);
        return Result.success(sparkVO);
    }
}
