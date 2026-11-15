package com.lilac.manager.note;

import com.lilac.domain.entity.Note;
import com.lilac.domain.vo.TagVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NoteMarkdownBuilder 单元测试
 *
 * <p>该构建器是纯函数，同样的输入必须产出同样的骨架，
 * 这里锁住分组、排序、渲染与标题生成四条核心规则。</p>
 */
class NoteMarkdownBuilderTest {

    /**
     * 构造一条碎片
     *
     * @param id         碎片id
     * @param content    正文
     * @param insight    一句话理解
     * @param source     来源
     * @param daysAgoBase 创建时间（毫秒时间戳）
     * @return 碎片实体
     */
    private Note note(long id, String content, String insight, String source, long daysAgoBase) {
        Note note = new Note();
        note.setId(id);
        note.setContent(content);
        note.setInsight(insight);
        note.setSource(source);
        note.setCreateTime(new Date(daysAgoBase));
        return note;
    }

    /**
     * 构造一个标签VO
     *
     * @param id   标签id
     * @param name 标签名
     * @return 标签VO
     */
    private TagVO tag(long id, String name) {
        TagVO tagVO = new TagVO();
        tagVO.setId(id);
        tagVO.setTagName(name);
        return tagVO;
    }

    @Test
    @DisplayName("按主题分组：组间按条数降序，杂项恒排最后")
    void shouldGroupByTopicWithUntaggedLast() {
        List<Note> notes = new ArrayList<>();
        notes.add(note(1L, "Redis 惰性删除", null, null, 1000L));
        notes.add(note(2L, "Redis 主从异步", null, null, 2000L));
        notes.add(note(3L, "Redis 缓存穿透", null, null, 3000L));
        notes.add(note(4L, "MySQL 最左前缀", null, null, 4000L));
        notes.add(note(5L, "没挂主题的一条", null, null, 5000L));

        Map<Long, List<TagVO>> tagMap = new HashMap<>();
        tagMap.put(1L, List.of(tag(10L, "Redis")));
        tagMap.put(2L, List.of(tag(10L, "Redis")));
        tagMap.put(3L, List.of(tag(10L, "Redis")));
        tagMap.put(4L, List.of(tag(11L, "MySQL")));

        NoteMarkdownBuilder.BuildResult result = NoteMarkdownBuilder.build(notes, tagMap, null, false);

        assertEquals(5, result.getNoteCount());
        assertEquals(1, result.getUntaggedCount());
        // 组间顺序：Redis(3) → MySQL(1) → 杂项(1)
        assertEquals("Redis", result.getTopics().get(0).getTagName());
        assertEquals(3, result.getTopics().get(0).getCount());
        assertEquals("MySQL", result.getTopics().get(1).getTagName());
        assertEquals("杂项", result.getTopics().get(2).getTagName());

        String markdown = result.getMarkdown();
        assertTrue(markdown.indexOf("## Redis") < markdown.indexOf("## MySQL"));
        assertTrue(markdown.indexOf("## MySQL") < markdown.indexOf("## 杂项"));
    }

    @Test
    @DisplayName("组内按创建时间升序，元信息与引用块按约定渲染")
    void shouldRenderNotesInAscendingTimeOrder() {
        // 故意乱序传入，构建器内部应按时间重排
        List<Note> notes = new ArrayList<>();
        notes.add(note(1L, "第二条", null, null, 2000L));
        notes.add(note(2L, "第一条", "我的理解", "某本书", 1000L));

        Map<Long, List<TagVO>> tagMap = new HashMap<>();
        tagMap.put(1L, List.of(tag(10L, "Redis")));
        tagMap.put(2L, List.of(tag(10L, "Redis")));

        NoteMarkdownBuilder.BuildResult result = NoteMarkdownBuilder.build(notes, tagMap, null, false);
        String markdown = result.getMarkdown();

        // 第一条（时间更早）应排在第二条之前
        assertTrue(markdown.indexOf("第一条") < markdown.indexOf("第二条"));
        // 来源与理解都要出现在结果里
        assertTrue(markdown.contains("`某本书`"));
        assertTrue(markdown.contains("> 我的理解"));
        // 日期元信息按 yyyy-MM-dd 渲染
        assertTrue(markdown.contains("`1970-01-01`"));
    }

    @Test
    @DisplayName("多段正文保留在同一个列表项内，不与下一条碎片混淆")
    void shouldKeepMultilineContentInOneListItem() {
        List<Note> notes = new ArrayList<>();
        notes.add(note(1L, "第一行\n第二行", null, null, 1000L));

        Map<Long, List<TagVO>> tagMap = new HashMap<>();
        tagMap.put(1L, List.of(tag(10L, "Redis")));

        String markdown = NoteMarkdownBuilder.build(notes, tagMap, null, false).getMarkdown();

        assertTrue(markdown.contains("- 第一行\n  第二行"));
        // 续行缩进两空格，保证仍属于同一个列表项
        assertFalse(markdown.contains("- 第一行\n- 第二行"));
    }

    @Test
    @DisplayName("标题生成：单主题用「学习笔记」，多主题用「学习速记 · 月份」")
    void shouldGenerateTitleByTopicCount() {
        List<Note> single = new ArrayList<>();
        single.add(note(1L, "内容", null, null, 1000L));
        Map<Long, List<TagVO>> singleTagMap = new HashMap<>();
        singleTagMap.put(1L, List.of(tag(10L, "Redis")));
        assertEquals("Redis 学习笔记",
                NoteMarkdownBuilder.build(single, singleTagMap, null, false).getTitle());

        List<Note> multi = new ArrayList<>();
        multi.add(note(1L, "内容一", null, null, 1000L));
        multi.add(note(2L, "内容二", null, null, 2000L));
        Map<Long, List<TagVO>> multiTagMap = new HashMap<>();
        multiTagMap.put(1L, List.of(tag(10L, "Redis")));
        multiTagMap.put(2L, List.of(tag(11L, "MySQL")));
        assertEquals("学习速记 · 1970-01",
                NoteMarkdownBuilder.build(multi, multiTagMap, null, false).getTitle());

        // 用户传了标题时以用户为准
        assertEquals("我自己的标题",
                NoteMarkdownBuilder.build(multi, multiTagMap, "我自己的标题", false).getTitle());
    }

    @Test
    @DisplayName("过渡提示：开启时插入注释，关闭时不插入")
    void shouldRespectInsertHintsFlag() {
        List<Note> notes = new ArrayList<>();
        notes.add(note(1L, "内容一", null, null, 1000L));
        notes.add(note(2L, "内容二", null, null, 2000L));
        Map<Long, List<TagVO>> tagMap = new HashMap<>();
        tagMap.put(1L, List.of(tag(10L, "Redis")));
        tagMap.put(2L, List.of(tag(11L, "MySQL")));

        assertTrue(NoteMarkdownBuilder.build(notes, tagMap, null, true).getMarkdown().contains("<!-- 写一句过渡"));
        assertFalse(NoteMarkdownBuilder.build(notes, tagMap, null, false).getMarkdown().contains("<!--"));
    }

    @Test
    @DisplayName("空输入返回空骨架，不抛异常")
    void shouldHandleEmptyInput() {
        NoteMarkdownBuilder.BuildResult result = NoteMarkdownBuilder.build(List.of(), Map.of(), null, true);
        assertEquals(0, result.getNoteCount());
        assertEquals("", result.getMarkdown());
        assertTrue(result.getTopics().isEmpty());
    }
}
