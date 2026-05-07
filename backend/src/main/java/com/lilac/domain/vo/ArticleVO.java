package com.lilac.domain.vo;

import cn.hutool.core.bean.BeanUtil;
import com.lilac.domain.entity.Article;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 文章VO
 */
@Data
public class ArticleVO {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 关联分类
     */
    private Long categoryId;

    /**
     * 封面图片URL（WebP缩略图）
     */
    private String coverUrl;

    /**
     * 投稿人id
     */
    private Long userId;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 1置顶，0普通
     */
    private Integer isTop;

    /**
     * 0草稿，1待审核，2审核
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 标签列表
     */
     private List<TagVO> tags;

    /**
     * 对象转为VO
     *
     * @param article 文章对象
     * @return ArticleVO
     */
    public static ArticleVO objToVo(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO articleVO = new ArticleVO();
        BeanUtil.copyProperties(article, articleVO);
        return articleVO;
    }

    /**
     * VO 转为对象
     *
     * @param articleVO 文章VO对象
     * @return Article
     */
    public static Article voToObj(ArticleVO articleVO) {
        if (articleVO == null) {
            return null;
        }
        Article article = new Article();
        BeanUtil.copyProperties(articleVO, article);
        return article;
    }
}
