package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.News;
import com.zhp.flea_market.model.vo.NewsVO;
import jakarta.servlet.http.HttpServletRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface NewsService extends IService<News> {
    /**
     * 分页获取新闻列表
     * @return
     */
    List<NewsVO> getNewsList(Page<News> page);
    /**
     * 添加新闻
     * @param news
     * @return
     */
    boolean addNews(News news, HttpServletRequest request);
    /**
     * 更新新闻
     * @param news
     * @return
     */
    boolean updateNews(News news, HttpServletRequest request);
    /**
     * 删除新闻
     * @param id
     * @return
     */
    boolean deleteNews(Long id, HttpServletRequest request);
    /**
     * 获取新闻详情
     * @param id
     * @return
     */
    NewsVO getNewsDetail(Long id);
    /**
     * 获取最新的新闻
     */
    NewsVO getLatestNews();
}
