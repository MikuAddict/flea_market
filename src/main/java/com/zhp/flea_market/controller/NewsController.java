package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.model.dto.request.NewsAddRequest;
import com.zhp.flea_market.model.dto.request.NewsUpdateRequest;
import com.zhp.flea_market.model.entity.News;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.NewsVO;
import com.zhp.flea_market.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台公告接口
 */
@RestController
@RequestMapping("/news")
@Slf4j
@Tag(name = "平台公告管理", description = "平台公告的增删改查接口")
public class NewsController extends BaseController {

    @Resource
    private NewsService newsService;

    /**
     * 分页获取新闻列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页新闻列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页获取新闻列表", description = "分页获取系统中的新闻信息")
    public BaseResponse<List<NewsVO>> getNewsList(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") long size) {
        // 参数校验
        Page<News> page = validatePageParams(current, size, 20);
        
        // 获取新闻列表
        List<NewsVO> newsList = newsService.getNewsList(page);
        
        logOperation("分页获取新闻列表", null, "当前页", current, "每页大小", size);
        return ResultUtils.success(newsList);
    }

    /**
     * 获取最新新闻
     *
     * @return 最新一条新闻
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新新闻", description = "获取系统中最新的一条新闻信息")
    public BaseResponse<NewsVO> getLatestNews() {
        NewsVO latestNews = newsService.getLatestNews();
        logOperation("获取最新新闻", null, "新闻ID", latestNews != null ? latestNews.getId() : null);
        return ResultUtils.success(latestNews);
    }

    /**
     * 获取新闻详情
     *
     * @param id 新闻ID
     * @return 新闻详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取新闻详情", description = "根据ID获取新闻的详细信息")
    public BaseResponse<NewsVO> getNewsDetail(
            @Parameter(description = "新闻ID") @PathVariable Long id) {
        // 参数校验
        validateId(id, "新闻ID");
        
        // 获取新闻详情
        NewsVO news = newsService.getNewsDetail(id);
        validateResourceExists(news, "新闻");
        
        logOperation("获取新闻详情", null, "新闻ID", id);
        return ResultUtils.success(news);
    }

    /**
     * 添加新闻
     *
     * @param newsAddRequest 新闻添加请求
     * @param request HTTP请求
     * @return 新增新闻的ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "添加新闻", description = "管理员添加新的新闻")
    public BaseResponse<Long> addNews(
            @RequestBody NewsAddRequest newsAddRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(newsAddRequest, "新闻添加请求");
        validateNotBlank(newsAddRequest.getTitle(), "新闻标题");
        validateNotBlank(newsAddRequest.getContent(), "新闻内容");

        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);
        
        // 创建新闻对象
        News news = new News();
        BeanUtils.copyProperties(newsAddRequest, news);
        // 设置作者ID
        news.setAuthorId(currentUser.getId());

        // 添加新闻
        boolean result = newsService.addNews(news, request);
        
        logOperation("添加新闻", result, request, 
                "新闻标题", newsAddRequest.getTitle(), 
                "作者ID", currentUser.getId()
        );
        return handleOperationResult(result, "新闻添加成功", news.getId());
    }

    /**
     * 更新新闻
     *
     * @param newsUpdateRequest 新闻更新请求
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "更新新闻", description = "管理员更新新闻信息")
    public BaseResponse<Boolean> updateNews(
            @RequestBody NewsUpdateRequest newsUpdateRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(newsUpdateRequest, "新闻更新请求");
        validateId(newsUpdateRequest.getId(), "新闻ID");
        validateNotBlank(newsUpdateRequest.getTitle(), "新闻标题");
        validateNotBlank(newsUpdateRequest.getContent(), "新闻内容");
        
        // 检查新闻是否存在
        NewsVO existNews = newsService.getNewsDetail(newsUpdateRequest.getId());
        validateResourceExists(existNews, "新闻");

        // 创建新闻对象并更新
        News news = new News();
        BeanUtils.copyProperties(newsUpdateRequest, news);
        // 保留原有的作者ID和创建时间
        news.setAuthorId(existNews.getAuthorId());

        // 更新新闻
        boolean result = newsService.updateNews(news, request);
        
        logOperation("更新新闻", result, request, 
                "新闻ID", newsUpdateRequest.getId(), 
                "新闻标题", newsUpdateRequest.getTitle()
        );
        return handleOperationResult(result, "新闻更新成功");
    }

    /**
     * 删除新闻
     *
     * @param id 新闻ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除新闻", description = "管理员根据ID删除新闻")
    public BaseResponse<Boolean> deleteNews(
            @Parameter(description = "新闻ID") @PathVariable Long id, 
            HttpServletRequest request) {
        // 参数校验
        validateId(id, "新闻ID");
        
        // 检查新闻是否存在
        NewsVO existNews = newsService.getNewsDetail(id);
        validateResourceExists(existNews, "新闻");

        // 删除新闻
        boolean result = newsService.deleteNews(id, request);
        
        logOperation("删除新闻", result, request, 
                "新闻ID", id, 
                "新闻标题", existNews.getTitle()
        );
        return handleOperationResult(result, "新闻删除成功");
    }
}