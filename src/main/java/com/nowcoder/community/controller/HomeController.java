package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.Impl.DiscussPostServiceImpl;
import com.nowcoder.community.service.Impl.LikeServiceImpl;
import com.nowcoder.community.service.Impl.UserServiceImpl;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author wang
 * @create 2022-03-10
 */
@Controller
public class HomeController implements CommunityConstant {

    //自动加载的一定得是容器中有的组件，一般加载的都是实现类
    @Autowired
    UserServiceImpl userService;

    @Autowired
    DiscussPostServiceImpl discussPostService;

    @Autowired
    LikeServiceImpl likeService;

    @GetMapping("/")
    public String root(){
        return "forward:/index";
    }

    /**
     *  需要注意的是，控制器中的形参实体类，都是从IOC容器中取的；并且都会将其他形参类自动存在model中
     *  方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
     *  所以,在thymeleaf中可以直接访问Page对象中的数据.
     * @param model 是为了将查询到的数据存在model中，给页面显示
     * @param page  是为了分页
     * @return  返回要交给Thymeleaf模板处理的视图名
     */
    @GetMapping("/index")
    //因为形参是JavaBean类，所以springboot会把请求参数自动封装到page中，不是类，只要属性名一致也会自动封装
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){
        //首先要查出总共有多少条数据，参数为0代表着不带userId的查询
        int dataTotal = discussPostService.findDiscussPostRows(0);
        //将其传入Page中，获取Page模型的数据
        page.setRows(dataTotal);
        //为了分页也能携带orderMode参数，才不会错乱模式。因为是get请求，所以需要用？的方式拼接参数
        page.setPath("/index?orderMode=" + orderMode);

        //查询出当前页要显示的数据
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);

        //因为查询出来的数据中有userId，我们期望的是用户的数据，所以我们还需要根据userId查询出用户数据，放在list中一起返回
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list != null){
            //即手动模拟一个联表查询，即查出帖子的数据，又查出发帖人的数据，两个表之间的连接条件就是userId
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.selectById(post.getUserId());
                map.put("user",user);
                //查点赞数量
                Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
