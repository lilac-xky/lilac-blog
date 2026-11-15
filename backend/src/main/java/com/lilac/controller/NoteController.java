package com.lilac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lilac.common.DeleteRequest;
import com.lilac.domain.dto.note.NoteAddRequest;
import com.lilac.domain.dto.note.NoteAggregateRequest;
import com.lilac.domain.dto.note.NoteQueryRequest;
import com.lilac.domain.dto.note.NoteUpdateRequest;
import com.lilac.domain.result.Result;
import com.lilac.domain.vo.NoteAggregateVO;
import com.lilac.domain.vo.NoteComposeVO;
import com.lilac.domain.vo.NoteTopicStatVO;
import com.lilac.domain.vo.NoteVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.manager.auth.anotation.SaAdminPermission;
import com.lilac.manager.auth.anotation.SaUserPermission;
import com.lilac.service.NoteService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 碎知（知识碎片）接口
 */
@RestController
@RequestMapping("/note")
public class NoteController {

    @Resource
    private NoteService noteService;

    /**
     * 随手记一条碎片
     *
     * @param noteAddRequest 添加参数
     * @return 碎片id
     */
    @PostMapping("/add")
    @SaUserPermission("note:add")
    public Result<Long> addNote(@RequestBody NoteAddRequest noteAddRequest) {
        ThrowUtils.throwIf(noteAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(noteService.addNote(noteAddRequest));
    }

    /**
     * 修改自己的碎片
     *
     * @param noteUpdateRequest 修改参数
     * @return 修改结果
     */
    @PostMapping("/update")
    @SaUserPermission("note:update")
    public Result<Boolean> updateNote(@RequestBody NoteUpdateRequest noteUpdateRequest) {
        ThrowUtils.throwIf(noteUpdateRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(noteService.updateNote(noteUpdateRequest));
    }

    /**
     * 删除自己的碎片
     *
     * @param deleteRequest 删除参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaUserPermission("note:delete")
    public Result<Boolean> deleteNote(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(noteService.deleteNote(deleteRequest.getId()));
    }

    /**
     * 获取碎片详情（仅本人可见）
     *
     * @param id 碎片id
     * @return 碎片详情
     */
    @GetMapping("/get")
    @SaUserPermission("note:list")
    public Result<NoteVO> getNote(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(noteService.getNoteVO(id));
    }

    /**
     * 分页查询我的碎片
     *
     * @param noteQueryRequest 查询参数
     * @return 碎片列表
     */
    @PostMapping("/list/page")
    @SaUserPermission("note:list")
    public Result<Page<NoteVO>> listMyNoteByPage(@RequestBody NoteQueryRequest noteQueryRequest) {
        ThrowUtils.throwIf(noteQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        long size = noteQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(noteService.listMyNoteVOPage(noteQueryRequest));
    }

    /**
     * 后台分页查询全部碎片（跨用户）
     *
     * @param noteQueryRequest 查询参数
     * @return 碎片列表
     */
    @PostMapping("/list/page/admin")
    @SaAdminPermission("note:list:admin")
    public Result<Page<NoteVO>> listNoteAdminByPage(@RequestBody NoteQueryRequest noteQueryRequest) {
        ThrowUtils.throwIf(noteQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(noteService.listNoteAdminVOPage(noteQueryRequest));
    }

    /**
     * 整理预览：返回 Markdown 骨架，不落库
     *
     * @param request 整理参数
     * @return 预览结果
     */
    @PostMapping("/aggregate")
    @SaUserPermission("note:compose")
    public Result<NoteAggregateVO> aggregate(@RequestBody NoteAggregateRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(noteService.aggregate(request));
    }

    /**
     * 确认整理：生成草稿文章并归档碎片
     *
     * @param request 整理参数
     * @return 生成结果，含 articleId
     */
    @PostMapping("/compose")
    @SaUserPermission("note:compose")
    public Result<NoteComposeVO> compose(@RequestBody NoteAggregateRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        return Result.success(noteService.compose(request));
    }

    /**
     * 主题分布统计
     *
     * @return 统计结果
     */
    @GetMapping("/topic/stat")
    @SaUserPermission("note:list")
    public Result<NoteTopicStatVO> topicStat() {
        return Result.success(noteService.topicStat());
    }
}
