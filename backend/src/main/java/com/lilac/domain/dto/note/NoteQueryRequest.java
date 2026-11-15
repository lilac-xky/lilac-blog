package com.lilac.domain.dto.note;

import com.lilac.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 查询碎片参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoteQueryRequest extends PageRequest {

    /**
     * 碎片id
     */
    private Long id;

    /**
     * 知识点正文（模糊查询）
     */
    private String content;

    /**
     * 0未整理，1已归档；为空表示不限
     */
    private Integer status;

    /**
     * 主题标签id列表；命中任意一个即返回
     */
    private List<Long> tagIds;

    /**
     * 归属用户id；用户端会被服务端强制改写为当前登录用户，传入无效
     */
    private Long userId;

    /**
     * 归档到的文章id
     */
    private Long articleId;
}
