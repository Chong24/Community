package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch的服务层
 *
 * @author wang
 * @create 2022-03-31
 */
@Service
public class ElasticsearchServiceImpl {

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    //往Elasticsearch中存帖子
    public void saveDiscussPost(DiscussPost post) {
        discussRepository.save(post);
    }

    //往Elasticsearch中删帖子
    public void deleteDiscussPost(int id) {
        discussRepository.deleteById(id);
    }

    //根据关键词搜索帖子，并对关键词进行高亮显示，对帖子的查询结果分页显示。current代表当前页
    public List<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        //获取查询到的内容
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();

        if (searchHits.isEmpty()) {
            return null;
        }
        List<DiscussPost> discussPosts = new ArrayList<>();
        //遍历返回的内容进行处理
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            DiscussPost post = new DiscussPost();
            //设置帖子属性
            int id = searchHit.getContent().getId();
            post.setId(id);

            int userId = searchHit.getContent().getUserId();
            post.setUserId(userId);

            Date createTime = searchHit.getContent().getCreateTime();
            post.setCreateTime(createTime);

            int status = searchHit.getContent().getStatus();
            post.setStatus(status);

            int commentCount = searchHit.getContent().getCommentCount();
            post.setCommentCount(commentCount);

            // 处理高亮显示的结果
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            System.out.println(highlightFields);
            if (highlightFields != null) {
                //设置内容高亮显示
                searchHit.getContent().setContent(highlightFields.get("content")==null ? searchHit.getContent().getContent():highlightFields.get("content").get(0).toString());
                //设置标题高亮显示
                searchHit.getContent().setTitle(highlightFields.get("title")==null ? searchHit.getContent().getTitle():highlightFields.get("title").get(0).toString());
            }

            String title = searchHit.getContent().getTitle();
            post.setTitle(title);

            String content = searchHit.getContent().getContent();
            post.setContent(content);

            discussPosts.add(post);
        }
        return discussPosts;
    }
}
