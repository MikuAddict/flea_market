package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.exception.ThrowUtils;
import com.zhp.flea_market.model.dto.request.NewsAddRequest;
import com.zhp.flea_market.model.dto.request.NewsUpdateRequest;
import com.zhp.flea_market.model.entity.News;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.NewsService;
import com.zhp.flea_market.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 新闻接口
 */
@RestController
@RequestMapping("/news")
@Slf4j
@Tag(name = "新闻管理", description = "新闻的增删改查接口")
public class NewsController {

    @Resource
    private NewsService newsService;

    @Resource
    private UserService userService;

    /**
     * 分页获取新闻列表
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页新闻列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页获取新闻列表", description = "分页获取系统中的新闻信息")
    public BaseResponse<List<News>> getNewsList(
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") long size) {
        Page<News> page = new Page<>(current, size);
        List<News> newsList = newsService.getNewsList(page);
        return ResultUtils.success(newsList);
    }

    /**
     * 获取最新新闻
     *
     * @return 最新一条新闻
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新新闻", description = "获取系统中最新的一条新闻信息")
    public BaseResponse<News> getLatestNews() {
        News latestNews = newsService.getLatestNews();
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
    public BaseResponse<News> getNewsDetail(
            @Parameter(description = "新闻ID") @PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新闻ID无效");
        }
        
        News news = newsService.getNewsDetail(id);
        if (news == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "新闻不存在");
        }
        
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
    public BaseResponse<Long> addNews(@RequestBody NewsAddRequest newsAddRequest, HttpServletRequest request) {
        // 参数校验
        if (newsAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(newsAddRequest.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新闻标题不能为空");
        }
        if (StringUtils.isBlank(newsAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新闻内容不能为空");
        }

        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);
        
        // 创建新闻对象
        News news = new News();
        BeanUtils.copyProperties(newsAddRequest, news);
        news.setAuthorId(currentUser.getId());
        news.setCreateTime(new Date());

        // 添加新闻
        boolean result = newsService.addNews(news, request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        
        return ResultUtils.success(news.getId());
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
    public BaseResponse<Boolean> updateNews(@RequestBody NewsUpdateRequest newsUpdateRequest, HttpServletRequest request) {
        // 参数校验
        if (newsUpdateRequest == null || newsUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(newsUpdateRequest.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新闻标题不能为空");
        }
        if (StringUtils.isBlank(newsUpdateRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新闻内容不能为空");
        }
        
        // 检查新闻是否存在
        News existNews = newsService.getNewsDetail(newsUpdateRequest.getId());
        if (existNews == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "新闻不存在");
        }

        // 创建新闻对象并更新
        News news = new News();
        BeanUtils.copyProperties(newsUpdateRequest, news);
        // 保留原有的作者ID和创建时间
        news.setAuthorId(existNews.getAuthorId());
        news.setCreateTime(existNews.getCreateTime());

        // 更新新闻
        boolean result = newsService.updateNews(news, request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        
        return ResultUtils.success(true);
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
            @Parameter(description = "新闻ID") @PathVariable Long id, HttpServletRequest request) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新闻ID无效");
        }
        
        // 检查新闻是否存在
        News existNews = newsService.getNewsDetail(id);
        if (existNews == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "新闻不存在");
        }

        // 删除新闻
        boolean result = newsService.deleteNews(id, request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        
        return ResultUtils.success(true);
    }
}