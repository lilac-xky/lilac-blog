package com.lilac.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.constant.SparkConstant;
import com.lilac.domain.dto.spark.SparkAddRequest;
import com.lilac.domain.dto.spark.SparkQueryRequest;
import com.lilac.domain.entity.Spark;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.SparkVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.enums.SparkStatusEnum;
import com.lilac.exception.BusinessException;
import com.lilac.service.SparkService;
import com.lilac.service.UserService;
import com.lilac.mapper.SparkMapper;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* 灵感表服务实现类
*/
@Service
public class SparkServiceImpl extends ServiceImpl<SparkMapper, Spark> implements SparkService {

    @Resource
    private UserService userService;

    /**
     * 添加灵感
     *
     * @param sparkAddRequest 添加灵感参数
     * @return 添加结果
     */
    @Override
    public long addSpark(SparkAddRequest sparkAddRequest) {
        ThrowUtils.throwIf(sparkAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        if (sparkAddRequest.getContent() == null) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "内容不能为空");
        }
        // 可见性只允许 public / private，未填写默认私有
        String visibility = sparkAddRequest.getVisibility();
        if (StrUtil.isNotBlank(visibility)
                && !SparkConstant.VISIBILITY_PUBLIC.equals(visibility)
                && !SparkConstant.VISIBILITY_PRIVATE.equals(visibility)) {
            throw new BusinessException(HttpsCodeEnum.PARAMS_ERROR, "不支持的可见性: " + visibility);
        }
        // 状态必须命中灵感状态枚举（SparkStatusEnum.fromStatus 内部不合法会抛异常）
        if (StrUtil.isNotBlank(sparkAddRequest.getStatus())) {
            SparkStatusEnum.fromStatus(sparkAddRequest.getStatus());
        }
        Spark spark = new Spark();
        BeanUtils.copyProperties(sparkAddRequest, spark);
        if (spark.getStatus() == null) {
            spark.setStatus(SparkStatusEnum.SPARK.getStatus());
        }
        if (spark.getVisibility() == null) {
            spark.setVisibility(SparkConstant.VISIBILITY_PRIVATE);
        }
        // 记录归属人，private 灵感只有本人可查看
        User loginUser = userService.getLoginUser();
        if (loginUser != null) {
            spark.setUserId(loginUser.getId());
        }
        boolean result = save(spark);
        if (!result) {
            throw new BusinessException(HttpsCodeEnum.OPERATION_ERROR, "添加失败");
        }
        return spark.getId();
    }

    /**
     * 分页查询灵感列表（管理员，公开与私有全部可见）
     *
     * @param sparkQueryRequest 查询参数
     * @return 灵感VO分页结果
     */
    @Override
    public Page<Spark> listSparkAdminPage(SparkQueryRequest sparkQueryRequest) {
        ThrowUtils.throwIf(sparkQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        return this.page(new Page<>(sparkQueryRequest.getCurrent(), sparkQueryRequest.getPageSize()),
                getQueryWrapper(sparkQueryRequest));
    }

    /**
     * 分页查询灵感列表（前台，只返回公开灵感 + 当前登录用户自己的私有灵感）
     *
     * @param sparkQueryRequest 查询参数
     * @return 灵感VO分页结果
     */
    @Override
    public Page<SparkVO> listSparkVOPage(SparkQueryRequest sparkQueryRequest) {
        ThrowUtils.throwIf(sparkQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        // 前台不信任请求里的可见性条件，可见范围完全由服务端决定
        sparkQueryRequest.setVisibility(null);
        LambdaQueryWrapper<Spark> queryWrapper = getQueryWrapper(sparkQueryRequest);
        Long loginUserId = getCurrentUserId();
        if (loginUserId == null) {
            // 未登录：只能看到公开灵感
            queryWrapper.eq(Spark::getVisibility, SparkConstant.VISIBILITY_PUBLIC);
        } else {
            // 已登录：公开灵感 或 自己创建的私有灵感
            queryWrapper.and(wrapper -> wrapper
                    .eq(Spark::getVisibility, SparkConstant.VISIBILITY_PUBLIC)
                    .or(privateWrapper -> privateWrapper
                            .eq(Spark::getVisibility, SparkConstant.VISIBILITY_PRIVATE)
                            .eq(Spark::getUserId, loginUserId)));
        }
        Page<Spark> sparkPage = this.page(
                new Page<>(sparkQueryRequest.getCurrent(), sparkQueryRequest.getPageSize()),
                queryWrapper);
        return toVOPage(sparkPage);
    }

    /**
     * 获取灵感详情（公开所有人可见，私有仅创建者本人可见）
     *
     * @param id 灵感id
     * @return 灵感VO
     */
    @Override
    public SparkVO getSparkVO(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        Spark spark = this.getById(id);
        ThrowUtils.throwIf(spark == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        // 私有灵感非本人访问时统一按“查不到”处理，避免内容泄露
        if (SparkConstant.VISIBILITY_PRIVATE.equals(spark.getVisibility())
                && !Objects.equals(spark.getUserId(), getCurrentUserId())) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "灵感不存在");
        }
        return SparkVO.objToVo(spark);
    }

    /**
     * 构建灵感查询条件
     *
     * @param sparkQueryRequest 查询参数
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<Spark> getQueryWrapper(SparkQueryRequest sparkQueryRequest) {
        LambdaQueryWrapper<Spark> queryWrapper = new LambdaQueryWrapper<>();
        ThrowUtils.throwIf(sparkQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Long id = sparkQueryRequest.getId();
        String content = sparkQueryRequest.getContent();
        String status = sparkQueryRequest.getStatus();
        String visibility = sparkQueryRequest.getVisibility();
        Long userId = sparkQueryRequest.getUserId();
        String sortOrder = sparkQueryRequest.getSortOrder();
        // 拼装条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Spark::getId, id);
        queryWrapper.like(StrUtil.isNotBlank(content), Spark::getContent, content);
        queryWrapper.eq(StrUtil.isNotBlank(status), Spark::getStatus, status);
        queryWrapper.eq(StrUtil.isNotBlank(visibility), Spark::getVisibility, visibility);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), Spark::getUserId, userId);
        boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
        queryWrapper.orderBy(true, isAsc, Spark::getCreateTime);
        return queryWrapper;
    }

    /**
     * 灵感分页对象转 VO 分页对象
     *
     * @param sparkPage 灵感分页结果
     * @return 灵感VO分页结果
     */
    private Page<SparkVO> toVOPage(Page<Spark> sparkPage) {
        Page<SparkVO> sparkVOPage = new Page<>(sparkPage.getCurrent(), sparkPage.getSize(), sparkPage.getTotal());
        sparkVOPage.setRecords(sparkPage.getRecords().stream().map(SparkVO::objToVo).toList());
        return sparkVOPage;
    }

    /**
     * 获取当前登录用户id（管理员或普通用户会话均可），未登录返回 null
     * 副作用：读取 Sa-Token session，不查询数据库
     *
     * @return 当前登录用户id，未登录为 null
     */
    private Long getCurrentUserId() {
        User loginUser = userService.getLoginUser();
        return loginUser == null ? null : loginUser.getId();
    }
}
