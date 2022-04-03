package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.Impl.ElasticsearchServiceImpl;
import com.nowcoder.community.service.Impl.LikeServiceImpl;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch的控制层
 * @author wang
 * @create 2022-03-31
 */
@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchServiceImpl elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeServiceImpl likeService;

    //search?keyword=xxx
    @GetMapping("/search")
    public String search(String keyword, Page page, Model model){
        if (StringUtils.isBlank(keyword)){
            return "index";
        }

        //搜索帖子
        List<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        //聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null){
            for (DiscussPost post : searchResult) {
                Map<String,Object> map = new HashMap<>();
                //帖子
                map.put("post",post);
                //作者
                map.put("user",userService.selectById(post.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : searchResult.size());

        return "/site/search";
    }
}
