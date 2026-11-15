package com.lilac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lilac.domain.entity.NoteTag;

import java.util.List;
import java.util.Map;

/**
* 碎片标签关联Service
*/
public interface NoteTagService extends IService<NoteTag> {

    /**
     * 批量查询碎片id → 标签id列表，保持写入顺序（第一个即主主题）
     *
     * @param noteIds 碎片id列表
     * @return 碎片id → 标签id列表
     */
    Map<Long, List<Long>> listTagIdsByNoteIds(List<Long> noteIds);
}
