package com.lilac.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.domain.dto.tag.TagAddRequest;
import com.lilac.domain.dto.tag.TagQueryRequest;
import com.lilac.domain.entity.ArticleTag;
import com.lilac.domain.entity.NoteTag;
import com.lilac.domain.entity.Tag;
import com.lilac.enums.HttpsCodeEnum;
import com.lilac.service.ArticleTagService;
import com.lilac.mapper.TagMapper;
import com.lilac.service.NoteTagService;
import com.lilac.service.TagService;
import com.lilac.utils.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 标签Service实现
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    private ArticleTagService articleTagService;

    @Resource
    private NoteTagService noteTagService;

    /**
     * 添加标签
     *
     * @param tagAddRequest 添加参数
     * @return 标签id
     */
    @Override
    public long addTag(TagAddRequest tagAddRequest) {
        Tag tag = new Tag();
        tag.setTagName(tagAddRequest.getTagName());
        boolean result = this.save(tag);
        ThrowUtils.throwIf(!result, HttpsCodeEnum.OPERATION_ERROR);
        return tag.getId();
    }

    /**
     * 获取标签列表
     *
     * @param tagQueryRequest 获取标签列表参数
     * @return 标签列表
     */
    @Override
    public Wrapper<Tag> getQueryWrapper(TagQueryRequest tagQueryRequest) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        Long id = tagQueryRequest.getId();
        String tagName = tagQueryRequest.getTagName();
        String sortOrder = tagQueryRequest.getSortOrder();
        // 拼装查询参数
        queryWrapper.eq(ObjUtil.isNotEmpty(id), Tag::getId, id);
        queryWrapper.eq(StrUtil.isNotBlank(tagName), Tag::getTagName, tagName);
        boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
        queryWrapper.orderBy(true, isAsc, Tag::getCreateTime);
        return queryWrapper;
    }

    /**
     * 删除标签
     *
     * @param id 标签id
     * @return 删除结果
     */
    @Override
    @Transactional
    public boolean deleteTag(Long id) {
        Tag tag = this.getById(id);
        ThrowUtils.throwIf(tag == null, HttpsCodeEnum.NOT_FOUND_ERROR);
        // 删除文章关联标签
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id));
        // 删除碎片关联标签：碎片的主题同样依赖 tag 表
        noteTagService.remove(new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getTagId, id));
        // 删除标签
        boolean remove = this.removeById(id);
        ThrowUtils.throwIf(!remove, HttpsCodeEnum.OPERATION_ERROR);
        return true;
    }
}




