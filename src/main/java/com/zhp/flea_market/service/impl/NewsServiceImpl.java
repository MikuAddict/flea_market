package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.mapper.NewsMapper;
import com.zhp.flea_market.model.entity.News;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.NewsService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    @Autowired
    UserService userService;

    /**
     * 分页获取新闻列表
     * @param page 分页参数
     * @return 新闻列表
     */
    @Override
    public List<News> getNewsList(Page<News> page) {
        // 使用QueryWrapper明确指定查询字段，避免查询不存在的author字段
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "title", "content", "image_url", "author_id", "create_time");
        
        // 实现真正的分页查询
        Page<News> resultPage = this.page(page, queryWrapper);
        List<News> newsList = resultPage.getRecords();
        
        // 加载作者信息
        return loadAuthorInfo(newsList);
    }

    /**
     * 添加新闻
     * @param news 新闻信息
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
     * @param news 新闻信息
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
     */
    @Override
    public News getNewsDetail(Long id) {
        // 使用QueryWrapper明确指定查询字段，避免查询不存在的author字段
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "title", "content", "image_url", "author_id", "create_time");
        queryWrapper.eq("id", id);
        
        News news = this.getOne(queryWrapper);
        if (news != null) {
            loadAuthorInfo(news);
        }
        return news;
    }

    /**
     * 获取最新的新闻
     */
    @Override
    public News getLatestNews() {
        // 按创建时间倒序排列，只获取最新的一条新闻
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "title", "content", "image_url", "author_id", "create_time");
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        
        News news = this.getOne(queryWrapper);
        if (news != null) {
            loadAuthorInfo(news);
        }
        return news;
    }

    /**
     * 为单条新闻加载作者信息
     */
    private void loadAuthorInfo(News news) {
        if (news != null && news.getUser() != null) {
            User author = userService.getById(news.getUser().getId());
        }
    }

    /**
     * 为新闻列表加载作者信息
     * @param newsList 新闻列表
     */
    private List<News> loadAuthorInfo(List<News> newsList) {
        if (newsList == null || newsList.isEmpty()) {
            return newsList;
        }
        
        // 获取所有作者ID
        List<Long> authorIds = newsList.stream()
                .map(news -> news.getUser() != null ? news.getUser().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (authorIds.isEmpty()) {
            return newsList;
        }
        
        // 批量查询作者信息
        List<User> authors = userService.listByIds(authorIds);
        
        // 构建作者ID到作者名字的映射
        java.util.Map<Long, String> authorNameMap = authors.stream()
                .collect(Collectors.toMap(User::getId, User::getUserName));

        return newsList;
    }
}
