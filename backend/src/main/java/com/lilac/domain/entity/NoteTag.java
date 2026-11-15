package com.lilac.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * 碎片标签关联表
 */
@TableName(value ="note_tag")
@Data
public class NoteTag implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 碎片id
     */
    private Long noteId;

    /**
     * 标签id（碎片的主题）
     */
    private Long tagId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
