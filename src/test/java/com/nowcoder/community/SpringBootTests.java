package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.Impl.DiscussPostServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    private DiscussPost data;

    @BeforeAll
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    @AfterAll
    public static void afterClass() {
        System.out.println("afterClass");
    }

    @BeforeEach
    public void before() {
        System.out.println("before");

        // 初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.insertDiscussPost(data);
    }

    @AfterEach
    public void after() {
        System.out.println("after");

        // 删除测试数据
        discussPostService.updateStatus(data.getId(), 2);
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost post = discussPostService.selectDiscussPostById(data.getId());
        //断言：判断是否和我们预想的逻辑输出是否一致
        Assertions.assertNotNull(post);
        Assertions.assertEquals(data.getTitle(), post.getTitle());
        Assertions.assertEquals(data.getContent(), post.getContent());
    }

    @Test
    public void testUpdateScore() {
        int rows = discussPostService.updateScore(data.getId(), 2000.00);
        Assertions.assertEquals(1, rows);

        DiscussPost post = discussPostService.selectDiscussPostById(data.getId());
        Assertions.assertEquals(2000.00, post.getScore(), 2);
    }
}
