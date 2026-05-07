package com.lilac.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lilac.domain.dto.tag.TagAddRequest;
import com.lilac.domain.dto.tag.TagQueryRequest;
import com.lilac.domain.dto.tag.TagUpdateRequest;
import com.lilac.domain.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* 标签Service
*/
public interface TagService extends IService<Tag> {
    /**
     * 添加标签
     *
     * @param tagAddRequest 添加参数
     * @return 分类id
     */
    long addTag(TagAddRequest tagAddRequest);

    /**
     * 获取查询参数
     *
     * @param tagQueryRequest 查询参数
     * @return 查询参数
     */
    Wrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest);

    /**
     * 删除标签
     *
     * @param id 标签id
     * @return 删除结果
     */
    boolean deleteTag(Long id);

}
