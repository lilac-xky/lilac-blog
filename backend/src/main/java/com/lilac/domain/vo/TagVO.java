package com.lilac.domain.vo;

import com.lilac.domain.entity.Tag;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 标签VO
 */
@Data
public class TagVO {
    /**
     * 标签id
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 封装标签对象为VO
     *
     * @param tag 标签对象
     * @return TagVO
     */
    public static TagVO objToVo(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }

    /**
     * 封装VO对象为标签对象
     *
     * @param tagVO TagVO
     * @return 标签对象
     */
    public static Tag voToObj(TagVO tagVO) {
        if (tagVO == null) {
            return null;
        }
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagVO, tag);
        return tag;
    }
}
