package com.lilac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.domain.entity.NoteTag;
import com.lilac.mapper.NoteTagMapper;
import com.lilac.service.NoteTagService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* 碎片标签关联服务实现
*/
@Service
public class NoteTagServiceImpl extends ServiceImpl<NoteTagMapper, NoteTag> implements NoteTagService {

    /**
     * 根据碎片id列表获取标签id列表
     *
     * @param noteIds 碎片id列表
     * @return 碎片id → 标签id列表，保序
     */
    @Override
    public Map<Long, List<Long>> listTagIdsByNoteIds(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) {
            return new HashMap<>();
        }
        List<NoteTag> noteTags = this.list(new LambdaQueryWrapper<NoteTag>()
                .in(NoteTag::getNoteId, noteIds)
                .orderByAsc(NoteTag::getId));
        Map<Long, List<Long>> result = new LinkedHashMap<>();
        for (NoteTag noteTag : noteTags) {
            result.computeIfAbsent(noteTag.getNoteId(), key -> new ArrayList<>()).add(noteTag.getTagId());
        }
        return result;
    }
}
