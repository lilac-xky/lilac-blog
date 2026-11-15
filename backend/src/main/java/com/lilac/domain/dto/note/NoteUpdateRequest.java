package com.lilac.domain.dto.note;

import lombok.Data;

import java.util.List;

/**
 * 修改碎片请求
 */
@Data
public class NoteUpdateRequest {

    /**
     * 碎片id
     */
    private Long id;

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
     * 主题标签id列表；传 null 表示不改动标签，传空列表表示清空标签
     */
    private List<Long> tagIds;
}
