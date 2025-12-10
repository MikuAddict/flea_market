package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.mapper.NewsMapper;
import com.zhp.flea_market.model.entity.News;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.NewsVO;
import com.zhp.flea_market.service.ImageStorageService;
import com.zhp.flea_market.service.NewsService;
import com.zhp.flea_market.service.UserService;
import com.zhp.flea_market.utils.PageUtils;
import org.springframework.beans.BeanUtils;
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
    
    @Autowired
    ImageStorageService imageStorageService;

    /**
     * 分页获取新闻列表
     */
    @Override
    public List<NewsVO> getNewsList(Page<News> page) {
        List<News> newsList = PageUtils.getPageResult(this, page, queryWrapper -> {
            queryWrapper.select("id", "title", "content", "image_url", "author_id", "create_time");
            queryWrapper.orderByDesc("create_time");
        });
        
        return convertToNewsVOList(newsList);
    }

    /**
     * 添加新闻
     * @param news 新闻信息
     */
    @Override
    public boolean addNews(News news, HttpServletRequest request) {
        //判断用户是否为管理员
        User currentUser = userService.getLoginUser(request);
        if (userService.isAdmin(currentUser)) {
            // 设置作者ID
            news.setAuthorId(currentUser.getId());
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
            // 获取旧的新闻信息，用于删除旧图片
            News oldNews = this.getById(news.getId());
            
            boolean result = this.updateById(news);
            
            // 如果新闻图片URL发生变化，删除旧图片
            if (result && oldNews != null && oldNews.getImageUrl() != null && 
                !oldNews.getImageUrl().equals(news.getImageUrl())) {
                try {
                    imageStorageService.deleteImage(oldNews.getImageUrl());
                } catch (Exception e) {
                    // 删除旧图片失败不应该影响更新操作
                    System.err.println("删除旧新闻图片失败: " + e.getMessage());
                }
            }
            
            return result;
        }
        return false;
    }

    /**
     * 删除新闻
     */
    @Override
    public boolean deleteNews(Long id, HttpServletRequest request) {
        if (userService.isAdmin(userService.getLoginUserPermitNull(request))) {
            // 获取新闻信息，用于删除相关图片
            News news = this.getById(id);
            
            boolean result = this.removeById(id);
            
            // 如果删除成功且新闻有图片，删除相关图片
            if (result && news != null && news.getImageUrl() != null) {
                try {
                    imageStorageService.deleteImage(news.getImageUrl());
                } catch (Exception e) {
                    // 删除图片失败不应该影响删除操作
                    System.err.println("删除新闻图片失败: " + e.getMessage());
                }
            }
            
            return result;
        }
        return false;
    }

    /**
     * 获取新闻详情
     */
    @Override
    public NewsVO getNewsDetail(Long id) {
        // 使用QueryWrapper明确指定查询字段，避免查询不存在的author字段
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "title", "content", "image_url", "author_id", "create_time");
        queryWrapper.eq("id", id);
        
        News news = this.getOne(queryWrapper);
        if (news != null) {
            return convertToNewsVO(news);
        }
        return null;
    }

    /**
     * 获取最新的新闻
     */
    @Override
    public NewsVO getLatestNews() {
        // 按创建时间倒序排列，只获取最新的一条新闻
        QueryWrapper<News> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "title", "content", "image_url", "author_id", "create_time");
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        
        News news = this.getOne(queryWrapper);
        if (news != null) {
            return convertToNewsVO(news);
        }
        return null;
    }

    /**
     * 将News实体转换为NewsVO
     */
    private NewsVO convertToNewsVO(News news) {
        if (news == null) {
            return null;
        }
        
        NewsVO newsVO = new NewsVO();
        BeanUtils.copyProperties(news, newsVO);
        
        // 查询作者信息并设置作者姓名
        if (news.getAuthorId() != null) {
            User author = userService.getById(news.getAuthorId());
            if (author != null) {
                newsVO.setAuthorName(author.getUserName());
                newsVO.setAuthorAvatar(author.getUserAvatar());
            }
        }
        
        return newsVO;
    }

    /**
     * 将News实体列表转换为NewsVO列表
     */
    private List<NewsVO> convertToNewsVOList(List<News> newsList) {
        if (newsList == null || newsList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // 获取所有作者ID
        List<Long> authorIds = newsList.stream()
                .map(News::getAuthorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        // 构建作者ID到作者姓名的映射
        java.util.Map<Long, String> authorNameMap = new java.util.HashMap<>();
        if (!authorIds.isEmpty()) {
            List<User> authors = userService.listByIds(authorIds);
            authors.forEach(author -> {
                if (author != null && author.getId() != null && author.getUserName() != null) {
                    authorNameMap.put(author.getId(), author.getUserName());
                }
            });
        }
        
        // 转换为VO列表
        return newsList.stream()
                .map(news -> {
                    NewsVO newsVO = new NewsVO();
                    BeanUtils.copyProperties(news, newsVO);
                    // 设置作者姓名
                    if (news.getAuthorId() != null) {
                        newsVO.setAuthorName(authorNameMap.get(news.getAuthorId()));
                        newsVO.setAuthorAvatar(authorNameMap.get(news.getAuthorId()));
                    }
                    return newsVO;
                })
                .collect(Collectors.toList());
    }
}