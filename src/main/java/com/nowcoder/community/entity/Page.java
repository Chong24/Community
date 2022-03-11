package com.nowcoder.community.entity;

import org.springframework.context.annotation.Bean;

/**
 * 封装分页模块
 * @author wang
 * @create 2022-03-10
 */
public class Page {
    //需要当前页
    private int current = 1;
    //需要每页最多显示几条数据
    private int limit = 10;
    //需要数据的总量，这是查询出来的
    private int rows;
    //需要查询路径（用于重复使用分页链接） 因为不止一个地方需要分页模块，因此需要将链接写成动态的，不同的地方用只需要改这个链接
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        //设置当前页，需要判断是否超出了页数范围
        if (current >= 1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        //设置每页显示多少条数据，应该也有个范围
        if(limit >= 1 && limit <= 100){

            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        //数据总数量不能小于0
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * 获取起始页码  即要实现的假如当前页是3，就显示1 2 3 4 5；即左边两页 当前页 右边两页，如果左边没有两页，就显示剩下的
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
