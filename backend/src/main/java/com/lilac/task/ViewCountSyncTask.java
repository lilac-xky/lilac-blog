package com.lilac.task;

import com.lilac.constant.ArticleConstant;
import com.lilac.domain.entity.Article;
import com.lilac.mapper.ArticleMapper;
import com.lilac.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class ViewCountSyncTask {

    @Resource
    private ArticleService articleService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ArticleMapper articleMapper;

    /**
     * 每5分钟同步一次所有文章的浏览量
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncViews() {
        //  获取所有 key
        String pattern = ArticleConstant.VIEW_COUNT_KEY + "*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return;
        }

        // 批量获取所有 key 对应的值
        List<String> keyList = new ArrayList<>(keys);
        List<Long> values = Objects.requireNonNull(stringRedisTemplate.opsForValue().multiGet(keyList))
                .stream()
                .map(v -> v == null ? 0L : Long.parseLong(v))
                .toList();

        // 批量获取文章
        List<Article> articlesToUpdate = getArticles(keyList, values);

        // 批量更新数据库
        articleService.updateBatchById(articlesToUpdate);
    }

    /**
     * 获取批量更新文章的参数
     */
    private static List<Article> getArticles(List<String> keyList, List<Long> values) {
        List<Article> articlesToUpdate = new ArrayList<>();
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            // 从 key 中提取文章ID，格式 lilacBlog:post:view:123
            try {
                String idPart = key.substring(ArticleConstant.VIEW_COUNT_KEY.length());
                Long articleId = Long.parseLong(idPart);
                Long viewCount = values.get(i);
                Article article = new Article();
                article.setId(articleId);
                article.setViewCount(viewCount.intValue());
                articlesToUpdate.add(article);
            } catch (NumberFormatException e) {
                // 忽略解析失败
            }
        }
        return articlesToUpdate;
    }
}
