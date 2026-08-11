package com.lilac.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.domain.entity.ArticleTag;
import com.lilac.service.ArticleTagService;
import com.lilac.mapper.ArticleTagMapper;
import org.springframework.stereotype.Service;

/**
* 文章标签关联服务实现
*/
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService{

}




