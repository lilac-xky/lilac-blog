package com.lilac.domain.vo;

import cn.hutool.core.bean.BeanUtil;
import com.lilac.domain.entity.Note;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 知识碎片VO
 */
@Data
public class NoteVO {

    /**
     * 碎片id
     */
    private Long id;

    /**
     * 归属用户id
     */
    private Long userId;

    /**
     * 知识点正文
     */
    private String content;

    /**
     * 我的一句话理解
     */
    private String insight;

    /**
     * 来源：书名 / 链接 / 课程
     */
    private String source;

    /**
     * 0未整理，1已归档进文章
     */
    private Integer status;

    /**
     * 归档到的文章id
     */
    private Long articleId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 主题标签列表，顺序与写入时一致，第一个即整理时的分组依据
     */
    private List<TagVO> tags;

    /**
     * 对象转为VO
     *
     * @param note 碎片对象
     * @return NoteVO
     */
    public static NoteVO objToVo(Note note) {
        if (note == null) {
            return null;
        }
        NoteVO noteVO = new NoteVO();
        BeanUtil.copyProperties(note, noteVO);
        return noteVO;
    }

    /**
     * VO 转为对象
     *
     * @param noteVO 碎片VO对象
     * @return Note
     */
    public static Note voToObj(NoteVO noteVO) {
        if (noteVO == null) {
            return null;
        }
        Note note = new Note();
        BeanUtil.copyProperties(noteVO, note);
        return note;
    }
}
