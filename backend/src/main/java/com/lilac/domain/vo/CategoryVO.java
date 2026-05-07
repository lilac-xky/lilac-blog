package com.lilac.domain.vo;

import com.lilac.domain.entity.Category;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 分类VO
 */
@Data
public class CategoryVO {
    /**
     * 分类id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * obj转换VO
     *
     * @param category 分类
     * @return 分类VO
     */
    public static CategoryVO objToVo(Category category) {
        if (category == null) {
            return null;
        }
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    /**
     * VO转换obj
     *
     * @param categoryVO 分类VO
     * @return 分类
     */
    public static Category voToObj(CategoryVO categoryVO) {
        if (categoryVO == null) {
            return null;
        }
        Category category = new Category();
        BeanUtils.copyProperties(categoryVO, category);
        return category;
    }
}
