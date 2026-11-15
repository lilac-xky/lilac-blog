package com.lilac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.dto.note.NoteAddRequest;
import com.lilac.domain.dto.note.NoteAggregateRequest;
import com.lilac.domain.dto.note.NoteQueryRequest;
import com.lilac.domain.dto.note.NoteUpdateRequest;
import com.lilac.domain.entity.Note;
import com.lilac.domain.vo.NoteAggregateVO;
import com.lilac.domain.vo.NoteComposeVO;
import com.lilac.domain.vo.NoteTopicStatVO;
import com.lilac.domain.vo.NoteVO;

/**
* 知识碎片Service
*/
public interface NoteService extends IService<Note> {

    /**
     * 添加碎片，归属人取当前登录用户
     *
     * @param noteAddRequest 添加参数
     * @return 碎片id
     */
    long addNote(NoteAddRequest noteAddRequest);

    /**
     * 修改自己的碎片；标签传 null 表示不改动，传空列表表示清空
     *
     * @param noteUpdateRequest 修改参数
     * @return 修改结果
     */
    boolean updateNote(NoteUpdateRequest noteUpdateRequest);

    /**
     * 删除自己的碎片（连同 note_tag 关联一起清理）
     *
     * @param id 碎片id
     * @return 删除结果
     */
    boolean deleteNote(Long id);

    /**
     * 获取碎片详情，仅本人可见，他人访问按「不存在」处理
     *
     * @param id 碎片id
     * @return 碎片VO
     */
    NoteVO getNoteVO(Long id);

    /**
     * 分页查询我的碎片
     *
     * @param noteQueryRequest 查询参数
     * @return 碎片VO分页结果
     */
    Page<NoteVO> listMyNoteVOPage(NoteQueryRequest noteQueryRequest);

    /**
     * 后台分页查询全部碎片（跨用户）
     *
     * @param noteQueryRequest 查询参数
     * @return 碎片VO分页结果
     */
    Page<NoteVO> listNoteAdminVOPage(NoteQueryRequest noteQueryRequest);

    /**
     * 构建碎片查询条件
     *
     * @param noteQueryRequest 查询参数
     * @return 查询条件
     */
    LambdaQueryWrapper<Note> getQueryWrapper(NoteQueryRequest noteQueryRequest);

    /**
     * 整理预览：把一批碎片渲染成 Markdown 骨架，不落库
     *
     * @param request 整理参数
     * @return 预览结果
     */
    NoteAggregateVO aggregate(NoteAggregateRequest request);

    /**
     * 确认整理：生成草稿文章并把参与整理的碎片标记为已归档
     *
     * @param request 整理参数
     * @return 生成结果
     */
    NoteComposeVO compose(NoteAggregateRequest request);

    /**
     * 主题分布统计，供整理页做入口展示
     *
     * @return 统计结果
     */
    NoteTopicStatVO topicStat();
}
