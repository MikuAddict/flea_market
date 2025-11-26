package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.mapper.NewsMapper;
import com.zhp.flea_market.model.entity.News;
import com.zhp.flea_market.service.NewsService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    @Autowired
    UserService userService;

    /**
     * 分页获取新闻列表
     * @param page
     * @return
     */
    @Override
    public List<News> getNewsList(Page<News> page) {
        // 实现真正的分页查询
        return this.page(page).getRecords();
    }

    /**
     * 添加新闻
     * @param news
     * @return
     */
    @Override
    public boolean addNews(News news, HttpServletRequest request) {
        //判断用户是否为管理员
        if (userService.isAdmin(userService.getLoginUser(request))) {
            return this.save(news);
        }
        return false;
    }

    /**
     * 更新新闻
     * @param news
     * @return
     */
    @Override
    public boolean updateNews(News news, HttpServletRequest request) {
        if (userService.isAdmin(userService.getLoginUserPermitNull(request))) {
            return this.updateById(news);
        }
        return false;
    }

    /**
     * 删除新闻
     * @param id
     * @return
     */
    @Override
    public boolean deleteNews(Long id, HttpServletRequest request) {
        if (userService.isAdmin(userService.getLoginUserPermitNull(request))) {
            return this.removeById(id);
        }
        return false;
    }

    /**
     * 获取新闻详情
     * @param id
     * @return
     */
    @Override
    public News getNewsDetail(Long id) {
        return this.getById(id);
    }

    /**
     * 获取最新的新闻
     */
    @Override
    public List<News> getLatestNews() {
        // 按创建时间倒序排列，获取最新的新闻
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return this.list(queryWrapper);
    }
}
