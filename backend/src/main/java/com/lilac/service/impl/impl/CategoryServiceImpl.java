package com.lilac.service.impl.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lilac.domain.entity.Category;
import com.lilac.service.impl.CategoryService;
import com.lilac.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* 分类Service实现
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

}




