package com.lilac.service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.domain.entity.Tag;
import com.lilac.service.impl.TagService;
import com.lilac.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* 标签Service实现
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService{

}




