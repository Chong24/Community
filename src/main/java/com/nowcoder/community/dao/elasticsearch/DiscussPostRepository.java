package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * elasticsearch的DAO层操作：将贴子再elasticsearch中CRUD
 * ElasticsearchRepository的泛型参数一是实体类型，参数二是主键类型；它实现类实现了基础方法
 * @author wang
 * @create 2022-03-29
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
}
