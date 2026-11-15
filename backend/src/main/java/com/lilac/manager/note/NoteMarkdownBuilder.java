package com.lilac.manager.note;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.lilac.constant.NoteConstant;
import com.lilac.domain.entity.Note;
import com.lilac.domain.vo.NoteAggregateVO;
import com.lilac.domain.vo.TagVO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 碎片整理骨架构建器
 *
 * <p>职责：把一批知识碎片按「主题」分组、组内按时间排序，渲染成一份 Markdown 骨架。
 * 整个过程是纯函数式的：同样的输入永远产出同样的输出，不做任何"猜测式"归类，
 * 因此结果对用户完全可预期。</p>
 *
 * <p>只做确定性的事情（分组 / 排序 / 小标题 / 元信息 / 过渡提示），
 * 过渡句、删减、结论这些需要判断的部分留给人在编辑器里补。</p>
 */
public final class NoteMarkdownBuilder {

    /**
     * 日期标签格式：只保留到天，避免时间线过长时噪音过多
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 月份标签格式，用于自动生成标题
     */
    private static final String MONTH_PATTERN = "yyyy-MM";

    /**
     * 列表项续行缩进：两空格保证多行正文仍然归属同一个列表项
     */
    private static final String LIST_INDENT = "  ";

    /**
     * 过渡提示注释模板，参数依次为「上一个主题名」「下一个主题名」
     */
    private static final String TRANSITION_HINT = "<!-- 写一句过渡：从「%s」怎么引到「%s」；写完删掉这条注释 -->";

    /**
     * 杂项小节尾部的提示注释：提醒用户补主题或整段删掉
     */
    private static final String UNTAGGED_HINT = "<!-- 本节碎片未挂主题，建议补上 tag 后重新整理，或整段删除 -->";

    /**
     * 工具类禁止实例化
     */
    private NoteMarkdownBuilder() {
    }

    /**
     * 构建整理骨架
     *
     * @param notes      参与整理的碎片，调用方需保证已经按归属人过滤
     * @param noteTagMap 碎片id → 该碎片的标签列表（按写入顺序，第一个作为分组依据）
     * @param customTitle 用户指定的标题，为空则由规则自动生成
     * @param insertHints 是否插入过渡提示注释
     * @return 骨架构建结果，含 Markdown 正文与主题统计
     */
    public static BuildResult build(List<Note> notes,
                                    Map<Long, List<TagVO>> noteTagMap,
                                    String customTitle,
                                    boolean insertHints) {
        BuildResult result = new BuildResult();
        result.setMarkdown("");
        result.setTitle(StrUtil.blankToDefault(customTitle, ""));
        result.setNoteCount(0);
        result.setUntaggedCount(0);
        result.setTopics(new ArrayList<>());
        result.setTagIds(new ArrayList<>());
        result.setNoteIds(new ArrayList<>());
        if (notes == null || notes.isEmpty()) {
            return result;
        }

        // 组内排序依赖创建时间，这里先整体按时间升序稳定排序，缺失时间视为最早
        List<Note> sortedNotes = notes.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(NoteMarkdownBuilder::timeOf))
                .toList();

        // 按「第一个标签」分组；没有标签的统一归入杂项（key 为 null）
        Map<Long, TopicGroup> groupMap = new LinkedHashMap<>();
        for (Note note : sortedNotes) {
            TagVO primaryTag = primaryTagOf(noteTagMap, note.getId());
            Long groupKey = primaryTag == null ? null : primaryTag.getId();
            TopicGroup group = groupMap.computeIfAbsent(groupKey,
                    key -> new TopicGroup(key, primaryTag == null
                            ? NoteConstant.UNTAGGED_GROUP_NAME
                            : StrUtil.blankToDefault(primaryTag.getTagName(), NoteConstant.UNTAGGED_GROUP_NAME)));
            group.getNotes().add(note);
        }

        // 组间排序：碎片多的主题在前，「杂项」恒排最后
        List<TopicGroup> groups = new ArrayList<>(groupMap.values());
        groups.sort((a, b) -> {
            boolean aUntagged = a.getTagId() == null;
            boolean bUntagged = b.getTagId() == null;
            if (aUntagged != bUntagged) {
                return aUntagged ? 1 : -1;
            }
            int byCount = Integer.compare(b.getNotes().size(), a.getNotes().size());
            if (byCount != 0) {
                return byCount;
            }
            // 条数相同时按组内最早时间排序，保证结果确定且与学习推进顺序一致
            return Long.compare(earliestTimeOf(a), earliestTimeOf(b));
        });

        // 渲染 Markdown：逐组输出小节，相邻小节之间插入过渡提示
        StringBuilder markdown = new StringBuilder();
        int untaggedCount = 0;
        List<NoteAggregateVO.TopicItem> topics = new ArrayList<>();
        List<Long> noteIds = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            TopicGroup group = groups.get(i);
            if (i > 0 && insertHints) {
                markdown.append('\n')
                        .append(String.format(TRANSITION_HINT, groups.get(i - 1).getTagName(), group.getTagName()))
                        .append("\n\n");
            }
            markdown.append("## ").append(group.getTagName()).append("\n\n");
            for (Note note : group.getNotes()) {
                appendNote(markdown, note);
                noteIds.add(note.getId());
            }
            if (group.getTagId() == null) {
                untaggedCount = group.getNotes().size();
                if (insertHints) {
                    markdown.append('\n').append(UNTAGGED_HINT).append('\n');
                }
            }
            markdown.append('\n');

            NoteAggregateVO.TopicItem topicItem = new NoteAggregateVO.TopicItem();
            topicItem.setTagId(group.getTagId());
            topicItem.setTagName(group.getTagName());
            topicItem.setCount(group.getNotes().size());
            topics.add(topicItem);
        }

        result.setMarkdown(markdown.toString().stripTrailing());
        result.setTitle(StrUtil.isBlank(customTitle)
                ? generateTitle(groups, sortedNotes)
                : customTitle.trim());
        result.setNoteCount(sortedNotes.size());
        result.setUntaggedCount(untaggedCount);
        result.setTopics(topics);
        result.setNoteIds(noteIds);
        result.setTagIds(collectTagIds(sortedNotes, noteTagMap));
        return result;
    }

    /**
     * 渲染单条碎片：正文 → 日期与来源 → 一句话理解
     * 副作用：向 markdown 追加内容，不修改 note
     *
     * @param markdown 目标 Markdown 缓冲区
     * @param note     待渲染的碎片
     */
    private static void appendNote(StringBuilder markdown, Note note) {
        String content = StrUtil.nullToEmpty(note.getContent()).strip();
        // 正文可能被记录成首尾带空行的多段文本，这里只保留非空行，避免列表项被空行截断
        String[] lines = content.split("\\R");
        boolean firstLine = true;
        for (String line : lines) {
            if (StrUtil.isBlank(line)) {
                continue;
            }
            if (firstLine) {
                markdown.append("- ").append(line.strip());
                firstLine = false;
            } else {
                markdown.append('\n').append(LIST_INDENT).append(line.strip());
            }
        }
        // 正文整段为空时仍然保留一个列表项，防止整条碎片在结果里凭空消失
        if (firstLine) {
            markdown.append("- ");
        }
        markdown.append('\n');

        // 元信息行：日期恒有，来源可选；用行内代码渲染，视觉上不抢正文注意力
        StringBuilder meta = new StringBuilder(LIST_INDENT)
                .append('`').append(formatDate(note.getCreateTime(), DATE_PATTERN)).append('`');
        if (StrUtil.isNotBlank(note.getSource())) {
            // 来源里的反引号会截断行内代码，直接剔除
            meta.append(' ').append('`').append(note.getSource().replace("`", "").strip()).append('`');
        }
        markdown.append(meta).append('\n');

        // 一句话理解用引用块承载，与原文形成明确的层级差
        if (StrUtil.isNotBlank(note.getInsight())) {
            String[] insightLines = note.getInsight().strip().split("\\R");
            for (String insightLine : insightLines) {
                if (StrUtil.isBlank(insightLine)) {
                    continue;
                }
                markdown.append(LIST_INDENT).append("> ").append(insightLine.strip()).append('\n');
            }
        }
    }

    /**
     * 自动生成标题
     * 规则：只有一个主题时用「{主题} 学习笔记」，否则用「学习速记 · {月份区间}」
     *
     * @param groups      已排序的主题分组
     * @param sortedNotes 参与整理的碎片（已按时间升序）
     * @return 标题
     */
    private static String generateTitle(List<TopicGroup> groups, List<Note> sortedNotes) {
        if (groups.size() == 1 && groups.get(0).getTagId() != null) {
            return groups.get(0).getTagName() + " 学习笔记";
        }
        String fromMonth = formatDate(timeOf(sortedNotes.get(0)), MONTH_PATTERN);
        String toMonth = formatDate(timeOf(sortedNotes.get(sortedNotes.size() - 1)), MONTH_PATTERN);
        if (StrUtil.equals(fromMonth, toMonth)) {
            return "学习速记 · " + toMonth;
        }
        return "学习速记 · " + fromMonth + " ~ " + toMonth;
    }

    /**
     * 收集本次整理涉及的全部标签id（含同一条碎片的副标签），供生成文章时写入文章标签
     *
     * @param notes      参与整理的碎片
     * @param noteTagMap 碎片id → 标签列表
     * @return 去重后的标签id列表
     */
    private static List<Long> collectTagIds(List<Note> notes, Map<Long, List<TagVO>> noteTagMap) {
        if (noteTagMap == null || noteTagMap.isEmpty()) {
            return new ArrayList<>();
        }
        return notes.stream()
                .map(note -> noteTagMap.get(note.getId()))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .map(TagVO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 取碎片的主主题：标签列表的第一个元素
     *
     * @param noteTagMap 碎片id → 标签列表
     * @param noteId     碎片id
     * @return 主主题标签，没有则返回 null
     */
    private static TagVO primaryTagOf(Map<Long, List<TagVO>> noteTagMap, Long noteId) {
        if (noteTagMap == null || noteId == null) {
            return null;
        }
        List<TagVO> tags = noteTagMap.get(noteId);
        return (tags == null || tags.isEmpty()) ? null : tags.get(0);
    }

    /**
     * 读取分组内最早一条碎片的时间，用于组间排序的稳定兜底
     *
     * @param group 主题分组
     * @return 最早时间戳，缺失为 0
     */
    private static long earliestTimeOf(TopicGroup group) {
        return group.getNotes().stream().mapToLong(NoteMarkdownBuilder::timeOf).min().orElse(0L);
    }

    /**
     * 读取碎片创建时间的时间戳
     *
     * @param note 碎片
     * @return 时间戳，缺失或非法返回 0
     */
    private static long timeOf(Note note) {
        Date createTime = note.getCreateTime();
        return createTime == null ? 0L : createTime.getTime();
    }

    /**
     * 按指定格式格式化时间，缺失时返回占位符
     *
     * @param date    时间
     * @param pattern 格式
     * @return 格式化结果
     */
    private static String formatDate(Date date, String pattern) {
        return date == null ? "-" : DateUtil.format(date, pattern);
    }

    /**
     * 按指定格式格式化时间戳，缺失（0 或负数）时返回占位符
     *
     * @param timestamp 时间戳（毫秒）
     * @param pattern   格式
     * @return 格式化结果
     */
    private static String formatDate(long timestamp, String pattern) {
        return timestamp <= 0L ? "-" : DateUtil.format(new Date(timestamp), pattern);
    }

    /**
     * 渲染过程中的主题分组载体
     */
    private static final class TopicGroup {

        /**
         * 主题标签id，null 表示未挂主题的杂项
         */
        private final Long tagId;

        /**
         * 主题名，用于生成小标题
         */
        private final String tagName;

        /**
         * 组内碎片，已按创建时间升序
         */
        private final List<Note> notes = new ArrayList<>();

        TopicGroup(Long tagId, String tagName) {
            this.tagId = tagId;
            this.tagName = tagName;
        }

        Long getTagId() {
            return tagId;
        }

        String getTagName() {
            return tagName;
        }

        List<Note> getNotes() {
            return notes;
        }
    }

    /**
     * 骨架构建结果
     */
    public static final class BuildResult {

        /**
         * 文章标题
         */
        private String title;

        /**
         * Markdown 正文
         */
        private String markdown;

        /**
         * 参与整理的碎片条数
         */
        private Integer noteCount;

        /**
         * 未挂主题的碎片条数
         */
        private Integer untaggedCount;

        /**
         * 主题分布
         */
        private List<NoteAggregateVO.TopicItem> topics;

        /**
         * 涉及的标签id
         */
        private List<Long> tagIds;

        /**
         * 参与整理的碎片id
         */
        private List<Long> noteIds;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMarkdown() {
            return markdown;
        }

        public void setMarkdown(String markdown) {
            this.markdown = markdown;
        }

        public Integer getNoteCount() {
            return noteCount;
        }

        public void setNoteCount(Integer noteCount) {
            this.noteCount = noteCount;
        }

        public Integer getUntaggedCount() {
            return untaggedCount;
        }

        public void setUntaggedCount(Integer untaggedCount) {
            this.untaggedCount = untaggedCount;
        }

        public List<NoteAggregateVO.TopicItem> getTopics() {
            return topics;
        }

        public void setTopics(List<NoteAggregateVO.TopicItem> topics) {
            this.topics = topics;
        }

        public List<Long> getTagIds() {
            return tagIds;
        }

        public void setTagIds(List<Long> tagIds) {
            this.tagIds = tagIds;
        }

        public List<Long> getNoteIds() {
            return noteIds;
        }

        public void setNoteIds(List<Long> noteIds) {
            this.noteIds = noteIds;
        }
    }
}
