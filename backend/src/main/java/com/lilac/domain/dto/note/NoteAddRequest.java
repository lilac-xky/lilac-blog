package com.lilac.domain.dto.note;

import lombok.Data;

import java.util.List;

/**
 * 添加碎片请求
 */
@Data
public class NoteAddRequest {

    /**
     * 知识点正文
     */
    private String content;

    /**
     * 我的一句话理解（可选）
     */
    private String insight;

    /**
     * 来源：书名 / 链接 / 课程（可选）
     */
    private String source;

    /**
     * 主题标签id列表，第一个作为整理时的分组依据
     */
    private List<Long> tagIds;
}
