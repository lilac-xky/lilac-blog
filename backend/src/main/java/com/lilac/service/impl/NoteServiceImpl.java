package com.lilac.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.constant.ArticleConstant;
import com.lilac.constant.NoteConstant;
import com.lilac.domain.dto.article.ArticleAddRequest;
import com.lilac.domain.dto.note.NoteAddRequest;
import com.lilac.domain.dto.note.NoteAggregateRequest;
import com.lilac.domain.dto.note.NoteQueryRequest;
import com.lilac.domain.dto.note.NoteUpdateRequest;
import com.lilac.domain.entity.Note;
import com.lilac.domain.entity.NoteTag;
import com.lilac.domain.entity.Tag;
import com.lilac.domain.entity.User;
import com.lilac.domain.vo.NoteAggregateVO;
import com.lilac.domain.vo.NoteComposeVO;
import com.lilac.domain.vo.NoteTopicStatVO;
import com.lilac.domain.vo.NoteVO;
import com.lilac.domain.vo.TagVO;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.exception.BusinessException;
import com.lilac.manager.note.NoteMarkdownBuilder;
import com.lilac.mapper.NoteMapper;
import com.lilac.service.ArticleService;
import com.lilac.service.NoteService;
import com.lilac.service.NoteTagService;
import com.lilac.service.TagService;
import com.lilac.service.UserService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* 知识碎片服务实现
*/
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Resource
    private UserService userService;

    @Resource
    private NoteTagService noteTagService;

    @Resource
    private TagService tagService;

    @Resource
    private ArticleService articleService;

    /**
     * 添加碎片
     * 副作用：写入 note 与 note_tag；内容长度超限直接拒绝
     *
     * @param noteAddRequest 添加参数
     * @return 碎片id
     */
    @Override
    @Transactional
    public long addNote(NoteAddRequest noteAddRequest) {
        ThrowUtils.throwIf(noteAddRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        String content = StrUtil.trim(noteAddRequest.getContent());
        ThrowUtils.throwIf(StrUtil.isBlank(content), HttpsCodeEnum.PARAMS_ERROR, "碎片内容不能为空");
        ThrowUtils.throwIf(content.length() > NoteConstant.CONTENT_MAX_LENGTH,
                HttpsCodeEnum.PARAMS_ERROR, "碎片内容不能超过 " + NoteConstant.CONTENT_MAX_LENGTH + " 个字符");
        String insight = StrUtil.trim(noteAddRequest.getInsight());
        ThrowUtils.throwIf(insight != null && insight.length() > NoteConstant.INSIGHT_MAX_LENGTH,
                HttpsCodeEnum.PARAMS_ERROR, "理解不能超过 " + NoteConstant.INSIGHT_MAX_LENGTH + " 个字符");
        String source = StrUtil.trim(noteAddRequest.getSource());
        ThrowUtils.throwIf(source != null && source.length() > NoteConstant.SOURCE_MAX_LENGTH,
                HttpsCodeEnum.PARAMS_ERROR, "来源不能超过 " + NoteConstant.SOURCE_MAX_LENGTH + " 个字符");

        Note note = new Note();
        note.setContent(content);
        note.setInsight(StrUtil.emptyToNull(insight));
        note.setSource(StrUtil.emptyToNull(source));
        note.setStatus(NoteConstant.STATUS_RAW);
        // 归属人取当前登录用户，请求体里的 userId 全部忽略
        note.setUserId(currentUserId());
        boolean saved = this.save(note);
        ThrowUtils.throwIf(!saved, HttpsCodeEnum.OPERATION_ERROR, "添加失败");
        // 标签按请求顺序写入，第一个即主主题
        saveNoteTags(note.getId(), noteAddRequest.getTagIds());
        return note.getId();
    }

    /**
     * 修改自己的碎片
     * 副作用：更新 note 与 note_tag；tagIds 为 null 时保存原有标签
     *
     * @param noteUpdateRequest 修改参数
     * @return 修改结果
     */
    @Override
    @Transactional
    public boolean updateNote(NoteUpdateRequest noteUpdateRequest) {
        ThrowUtils.throwIf(noteUpdateRequest == null || noteUpdateRequest.getId() == null,
                HttpsCodeEnum.PARAMS_ERROR);
        Note old = getOwnNoteOrThrow(noteUpdateRequest.getId());

        Note note = new Note();
        note.setId(old.getId());
        if (noteUpdateRequest.getContent() != null) {
            String content = StrUtil.trim(noteUpdateRequest.getContent());
            ThrowUtils.throwIf(StrUtil.isBlank(content), HttpsCodeEnum.PARAMS_ERROR, "碎片内容不能为空");
            ThrowUtils.throwIf(content.length() > NoteConstant.CONTENT_MAX_LENGTH,
                    HttpsCodeEnum.PARAMS_ERROR, "碎片内容不能超过 " + NoteConstant.CONTENT_MAX_LENGTH + " 个字符");
            note.setContent(content);
        }
        if (noteUpdateRequest.getInsight() != null) {
            String insight = StrUtil.trim(noteUpdateRequest.getInsight());
            ThrowUtils.throwIf(insight.length() > NoteConstant.INSIGHT_MAX_LENGTH,
                    HttpsCodeEnum.PARAMS_ERROR, "理解不能超过 " + NoteConstant.INSIGHT_MAX_LENGTH + " 个字符");
            note.setInsight(StrUtil.emptyToNull(insight));
        }
        if (noteUpdateRequest.getSource() != null) {
            String source = StrUtil.trim(noteUpdateRequest.getSource());
            ThrowUtils.throwIf(source.length() > NoteConstant.SOURCE_MAX_LENGTH,
                    HttpsCodeEnum.PARAMS_ERROR, "来源不能超过 " + NoteConstant.SOURCE_MAX_LENGTH + " 个字符");
            note.setSource(StrUtil.emptyToNull(source));
        }
        note.setEditTime(new Date());
        boolean update = this.updateById(note);
        ThrowUtils.throwIf(!update, HttpsCodeEnum.OPERATION_ERROR);
        // tagIds 为 null 表示本次不调整标签，空列表表示清空
        if (noteUpdateRequest.getTagIds() != null) {
            noteTagService.remove(new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, old.getId()));
            saveNoteTags(old.getId(), noteUpdateRequest.getTagIds());
        }
        return true;
    }

    /**
     * 删除自己的碎片
     * 副作用：逻辑删除 note，物理清理 note_tag 关联
     *
     * @param id 碎片id
     * @return 删除结果
     */
    @Override
    @Transactional
    public boolean deleteNote(Long id) {
        Note old = getOwnNoteOrThrow(id);
        boolean remove = this.removeById(old.getId());
        ThrowUtils.throwIf(!remove, HttpsCodeEnum.OPERATION_ERROR);
        noteTagService.remove(new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, old.getId()));
        return true;
    }

    /**
     * 获取碎片详情
     * 他人碎片统一按「不存在」处理，避免泄露内容
     *
     * @param id 碎片id
     * @return 碎片VO
     */
    @Override
    public NoteVO getNoteVO(Long id) {
        Note note = getOwnNoteOrThrow(id);
        NoteVO noteVO = NoteVO.objToVo(note);
        noteVO.setTags(loadTags(Collections.singletonList(note)).getOrDefault(note.getId(), new ArrayList<>()));
        return noteVO;
    }

    /**
     * 分页查询我的碎片
     *
     * @param noteQueryRequest 查询参数
     * @return 碎片VO分页结果
     */
    @Override
    public Page<NoteVO> listMyNoteVOPage(NoteQueryRequest noteQueryRequest) {
        ThrowUtils.throwIf(noteQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        // 归属条件由服务端强制写入，请求里的 userId 无效
        noteQueryRequest.setUserId(currentUserId());
        return pageToVOPage(this.page(
                new Page<>(noteQueryRequest.getCurrent(), noteQueryRequest.getPageSize()),
                getQueryWrapper(noteQueryRequest)));
    }

    /**
     * 后台分页查询全部碎片
     *
     * @param noteQueryRequest 查询参数
     * @return 碎片VO分页结果
     */
    @Override
    public Page<NoteVO> listNoteAdminVOPage(NoteQueryRequest noteQueryRequest) {
        ThrowUtils.throwIf(noteQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        return pageToVOPage(this.page(
                new Page<>(noteQueryRequest.getCurrent(), noteQueryRequest.getPageSize()),
                getQueryWrapper(noteQueryRequest)));
    }

    /**
     * 构建碎片查询条件
     *
     * @param noteQueryRequest 查询参数
     * @return 查询条件
     */
    @Override
    public LambdaQueryWrapper<Note> getQueryWrapper(NoteQueryRequest noteQueryRequest) {
        LambdaQueryWrapper<Note> queryWrapper = new LambdaQueryWrapper<>();
        ThrowUtils.throwIf(noteQueryRequest == null, HttpsCodeEnum.PARAMS_ERROR);
        Long id = noteQueryRequest.getId();
        String content = noteQueryRequest.getContent();
        Integer status = noteQueryRequest.getStatus();
        Long userId = noteQueryRequest.getUserId();
        Long articleId = noteQueryRequest.getArticleId();
        String sortOrder = noteQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Note::getId, id);
        queryWrapper.like(StrUtil.isNotBlank(content), Note::getContent, content);
        queryWrapper.eq(ObjUtil.isNotEmpty(status), Note::getStatus, status);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), Note::getUserId, userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(articleId), Note::getArticleId, articleId);
        // 按主题过滤：先查关联表拿到碎片id列表，再用 in 收口
        List<Long> tagIds = noteQueryRequest.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> noteIdsByTags = noteTagService
                    .list(new LambdaQueryWrapper<NoteTag>().in(NoteTag::getTagId, tagIds))
                    .stream().map(NoteTag::getNoteId).distinct().toList();
            queryWrapper.in(Note::getId, noteIdsByTags.isEmpty() ? List.of(-1L) : noteIdsByTags);
        }
        boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
        queryWrapper.orderBy(true, isAsc, Note::getCreateTime);
        return queryWrapper;
    }

    /**
     * 整理预览：渲染 Markdown 骨架，不写库
     *
     * @param request 整理参数
     * @return 预览结果
     */
    @Override
    public NoteAggregateVO aggregate(NoteAggregateRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        List<Note> notes = loadNotesForAggregate(request);
        ThrowUtils.throwIf(notes.isEmpty(), HttpsCodeEnum.PARAMS_ERROR, "没有可整理的碎片");
        Map<Long, List<TagVO>> noteTagMap = loadTags(notes);
        boolean insertHints = !Boolean.FALSE.equals(request.getInsertHints());
        NoteMarkdownBuilder.BuildResult result =
                NoteMarkdownBuilder.build(notes, noteTagMap, request.getTitle(), insertHints);

        NoteAggregateVO vo = new NoteAggregateVO();
        vo.setTitle(result.getTitle());
        vo.setMarkdown(result.getMarkdown());
        vo.setNoteCount(result.getNoteCount());
        vo.setUntaggedCount(result.getUntaggedCount());
        vo.setTopics(result.getTopics());
        vo.setTagIds(result.getTagIds());
        vo.setNoteIds(result.getNoteIds());
        return vo;
    }

    /**
     * 确认整理：生成草稿文章并归档碎片
     * 副作用：新建 article（status=0 草稿）与 article_tag，回写 note.status / note.articleId
     *
     * @param request 整理参数
     * @return 生成结果
     */
    @Override
    @Transactional
    public NoteComposeVO compose(NoteAggregateRequest request) {
        ThrowUtils.throwIf(request == null, HttpsCodeEnum.PARAMS_ERROR);
        List<Note> notes = loadNotesForAggregate(request);
        ThrowUtils.throwIf(notes.isEmpty(), HttpsCodeEnum.PARAMS_ERROR, "没有可整理的碎片");
        Map<Long, List<TagVO>> noteTagMap = loadTags(notes);
        boolean insertHints = !Boolean.FALSE.equals(request.getInsertHints());
        NoteMarkdownBuilder.BuildResult result =
                NoteMarkdownBuilder.build(notes, noteTagMap, request.getTitle(), insertHints);

        // 生成草稿文章：复用现有文章服务，标签自动带上本次涉及的主题
        ArticleAddRequest articleAddRequest = new ArticleAddRequest();
        articleAddRequest.setTitle(result.getTitle());
        articleAddRequest.setContent(result.getMarkdown());
        articleAddRequest.setStatus(ArticleConstant.STATUS_DRAFT);
        articleAddRequest.setIsTop(0);
        articleAddRequest.setTagIds(result.getTagIds());
        long articleId = articleService.addArticle(articleAddRequest);

        // 回写归档状态，避免同一批碎片被重复整理
        for (Note note : notes) {
            Note update = new Note();
            update.setId(note.getId());
            update.setStatus(NoteConstant.STATUS_ARCHIVED);
            update.setArticleId(articleId);
            update.setEditTime(new Date());
            boolean ok = this.updateById(update);
            ThrowUtils.throwIf(!ok, HttpsCodeEnum.OPERATION_ERROR, "归档碎片失败");
        }

        NoteComposeVO vo = new NoteComposeVO();
        vo.setArticleId(articleId);
        vo.setTitle(result.getTitle());
        vo.setNoteCount(result.getNoteCount());
        return vo;
    }

    /**
     * 主题分布统计
     * 口径：只统计「未整理」的碎片，按主主题聚合，未挂主题的记为杂项
     *
     * @return 统计结果
     */
    @Override
    public NoteTopicStatVO topicStat() {
        Long userId = currentUserId();
        // 未整理总数与已归档总数分别统计，避免把全量数据拉进内存
        int rawTotal = Math.toIntExact(this.count(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .eq(Note::getStatus, NoteConstant.STATUS_RAW)));
        int archivedTotal = Math.toIntExact(this.count(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .eq(Note::getStatus, NoteConstant.STATUS_ARCHIVED)));

        NoteTopicStatVO vo = new NoteTopicStatVO();
        vo.setRawTotal(rawTotal);
        vo.setArchivedTotal(archivedTotal);
        vo.setTopics(new ArrayList<>());
        if (rawTotal == 0) {
            return vo;
        }
        // 只扫描最近的一部分未整理碎片做主题分布，个人博客场景足够
        List<Note> recentRawNotes = this.list(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .eq(Note::getStatus, NoteConstant.STATUS_RAW)
                .orderByDesc(Note::getCreateTime)
                .last("LIMIT " + NoteConstant.TOPIC_STAT_SCAN_LIMIT));
        Map<Long, List<TagVO>> noteTagMap = loadTags(recentRawNotes);

        // 按主主题计数，key 为 null 代表未挂主题
        Map<Long, NoteTopicStatVO.Item> itemMap = new LinkedHashMap<>();
        for (Note note : recentRawNotes) {
            List<TagVO> tags = noteTagMap.get(note.getId());
            TagVO primary = (tags == null || tags.isEmpty()) ? null : tags.get(0);
            Long key = primary == null ? null : primary.getId();
            NoteTopicStatVO.Item item = itemMap.computeIfAbsent(key, k -> {
                NoteTopicStatVO.Item created = new NoteTopicStatVO.Item();
                created.setTagId(primary == null ? null : primary.getId());
                created.setTagName(primary == null ? NoteConstant.UNTAGGED_GROUP_NAME : primary.getTagName());
                created.setCount(0);
                return created;
            });
            item.setCount(item.getCount() + 1);
        }
        List<NoteTopicStatVO.Item> topics = new ArrayList<>(itemMap.values());
        topics.sort((a, b) -> {
            boolean aUntagged = a.getTagId() == null;
            boolean bUntagged = b.getTagId() == null;
            if (aUntagged != bUntagged) {
                return aUntagged ? 1 : -1;
            }
            return Integer.compare(b.getCount(), a.getCount());
        });
        vo.setTopics(topics);
        return vo;
    }

    /**
     * 按整理参数加载碎片：强制限定当前用户 + 未整理状态
     * 选取规则：noteIds 优先；否则用时间区间；两者都没有则取全部未整理碎片
     *
     * @param request 整理参数
     * @return 碎片列表，按创建时间升序
     */
    private List<Note> loadNotesForAggregate(NoteAggregateRequest request) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, currentUserId())
                .eq(Note::getStatus, NoteConstant.STATUS_RAW);
        List<Long> noteIds = request.getNoteIds();
        if (noteIds != null && !noteIds.isEmpty()) {
            ThrowUtils.throwIf(noteIds.size() > NoteConstant.AGGREGATE_MAX_SIZE,
                    HttpsCodeEnum.PARAMS_ERROR, "单次最多整理 " + NoteConstant.AGGREGATE_MAX_SIZE + " 条碎片");
            wrapper.in(Note::getId, noteIds);
        } else {
            Date fromTime = request.getFromTime();
            Date toTime = request.getToTime();
            wrapper.ge(fromTime != null, Note::getCreateTime, fromTime);
            wrapper.le(toTime != null, Note::getCreateTime, toTime);
        }
        // 主题限定：只整理指定主题下的碎片
        List<Long> tagIds = request.getTagIds();
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> noteIdsByTags = noteTagService
                    .list(new LambdaQueryWrapper<NoteTag>().in(NoteTag::getTagId, tagIds))
                    .stream().map(NoteTag::getNoteId).distinct().toList();
            if (noteIdsByTags.isEmpty()) {
                return new ArrayList<>();
            }
            wrapper.in(Note::getId, noteIdsByTags);
        }
        wrapper.orderByAsc(Note::getCreateTime);
        List<Note> notes = this.list(wrapper);
        // 时间区间未指定时兜底限流，防止一次把历史全部拉出来
        if (notes.size() > NoteConstant.AGGREGATE_MAX_SIZE) {
            notes = notes.subList(0, NoteConstant.AGGREGATE_MAX_SIZE);
        }
        return new ArrayList<>(notes);
    }

    /**
     * 批量加载碎片标签，保持写入顺序，第一个即主主题
     *
     * @param notes 碎片列表
     * @return 碎片id → 标签VO列表
     */
    private Map<Long, List<TagVO>> loadTags(List<Note> notes) {
        Map<Long, List<TagVO>> result = new LinkedHashMap<>();
        if (notes == null || notes.isEmpty()) {
            return result;
        }
        List<Long> noteIds = notes.stream().map(Note::getId).toList();
        Map<Long, List<Long>> noteTagIdMap = noteTagService.listTagIdsByNoteIds(noteIds);
        List<Long> allTagIds = noteTagIdMap.values().stream()
                .flatMap(List::stream).distinct().toList();
        if (allTagIds.isEmpty()) {
            return result;
        }
        Map<Long, Tag> tagMap = tagService.listByIds(allTagIds).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));
        noteTagIdMap.forEach((noteId, tagIds) -> {
            List<TagVO> tagVOS = new ArrayList<>();
            for (Long tagId : tagIds) {
                Tag tag = tagMap.get(tagId);
                // 标签被删掉时跳过，避免历史碎片整条消失
                if (tag != null) {
                    tagVOS.add(TagVO.objToVo(tag));
                }
            }
            result.put(noteId, tagVOS);
        });
        return result;
    }

    /**
     * 写入碎片标签关联
     *
     * @param noteId 碎片id
     * @param tagIds 标签id列表，为空则不做任何事
     */
    private void saveNoteTags(Long noteId, List<Long> tagIds) {
        if (noteId == null || tagIds == null || tagIds.isEmpty()) {
            return;
        }
        // 去重但保持顺序，保证「第一个标签 = 主主题」的语义不被破坏
        List<Long> distinctTagIds = tagIds.stream().filter(Objects::nonNull).distinct().toList();
        for (Long tagId : distinctTagIds) {
            NoteTag noteTag = new NoteTag();
            noteTag.setNoteId(noteId);
            noteTag.setTagId(tagId);
            boolean saved = noteTagService.save(noteTag);
            ThrowUtils.throwIf(!saved, HttpsCodeEnum.OPERATION_ERROR);
        }
    }

    /**
     * 查询属于当前登录用户的碎片，不属于自己时统一按「不存在」处理
     *
     * @param id 碎片id
     * @return 碎片实体
     */
    private Note getOwnNoteOrThrow(Long id) {
        ThrowUtils.throwIf(id == null, HttpsCodeEnum.PARAMS_ERROR);
        Note note = this.getById(id);
        ThrowUtils.throwIf(note == null, HttpsCodeEnum.NOT_FOUND_ERROR, "碎片不存在");
        if (!Objects.equals(note.getUserId(), currentUserId())) {
            throw new BusinessException(HttpsCodeEnum.NOT_FOUND_ERROR, "碎片不存在");
        }
        return note;
    }

    /**
     * 分页对象转换为 VO 分页对象，并批量补齐标签
     *
     * @param notePage 碎片分页结果
     * @return 碎片VO分页结果
     */
    private Page<NoteVO> pageToVOPage(Page<Note> notePage) {
        Page<NoteVO> voPage = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        Map<Long, List<TagVO>> tagMap = loadTags(notePage.getRecords());
        List<NoteVO> records = notePage.getRecords().stream().map(note -> {
            NoteVO vo = NoteVO.objToVo(note);
            vo.setTags(tagMap.getOrDefault(note.getId(), new ArrayList<>()));
            return vo;
        }).toList();
        voPage.setRecords(records);
        return voPage;
    }

    /**
     * 获取当前登录用户id
     * 副作用：读取 Sa-Token 会话；未登录直接抛「需要登录」
     *
     * @return 用户id
     */
    private Long currentUserId() {
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, HttpsCodeEnum.NEED_LOGIN);
        return loginUser.getId();
    }
}
