package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.tag.TagAddRequest;
import com.lilac.domain.dto.tag.TagQueryRequest;
import com.lilac.domain.dto.tag.TagUpdateRequest;
import com.lilac.domain.entity.Tag;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.TagVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.auth.anotation.SaAdminPermission;
import com.lilac.service.impl.TagService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标签接口
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    /**
     * 添加标签
     *
     * @param tagAddRequest 添加标签参数
     * @return 添加结果
     */
    @PostMapping("/add")
    @SaAdminPermission("tag:add")
    public Result<Long> addTag(@RequestBody TagAddRequest tagAddRequest) {
        ThrowUtils.throwIf(tagAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long tag = tagService.addTag(tagAddRequest);
        return Result.success(tag);
    }

    /**
     * 获取标签列表(管理员)
     *
     * @param tagQueryRequest 获取标签列表参数
     * @return 标签列表
     */
    @PostMapping("/list/page")
    @SaAdminPermission("tag:list")
    public Result<Page<Tag>> listTagByPage(@RequestBody TagQueryRequest tagQueryRequest) {
        ThrowUtils.throwIf(tagQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long current = tagQueryRequest.getCurrent();
        long size = tagQueryRequest.getPageSize();
        // 执行分页查询
        Page<Tag> tagPage = tagService.page(new Page<>(current, size), tagService.getQueryWrapper(tagQueryRequest));
        return Result.success(tagPage);
    }

    /**
     * 获取标签列表(用户)
     *
     * @param tagQueryRequest 获取标签列表参数
     * @return 标签列表
     */
    @PostMapping("/list/page/vo")
    public Result<Page<TagVO>> listTagByPageVO(@RequestBody TagQueryRequest tagQueryRequest) {
        ThrowUtils.throwIf(tagQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long current = tagQueryRequest.getCurrent();
        long size = tagQueryRequest.getPageSize();
        // 执行分页查询
        Page<Tag> tagPage = tagService.page(new Page<>(current, size), tagService.getQueryWrapper(tagQueryRequest));
        Page<TagVO> tagVOPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        tagVOPage.setRecords(tagPage.getRecords().stream().map(TagVO::objToVo).toList());
        return Result.success(tagVOPage);
    }

    /**
     * 删除标签
     *
     * @param deleteRequest 删除标签参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaAdminPermission("tag:delete")
    public Result<Boolean> deleteTag(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        boolean delete = tagService.deleteTag(deleteRequest.getId());
        ThrowUtils.throwIf(!delete, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }

    /**
     * 修改标签
     *
     * @param tagUpdateRequest 修改标签参数
     * @return 修改结果
     */
    @PostMapping("/update")
    @SaAdminPermission("tag:update")
    public Result<Boolean> updateTag(@RequestBody TagUpdateRequest tagUpdateRequest) {
        ThrowUtils.throwIf(tagUpdateRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagUpdateRequest, tag);
        boolean update = tagService.updateById(tag);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        return Result.success(true);
    }
}
