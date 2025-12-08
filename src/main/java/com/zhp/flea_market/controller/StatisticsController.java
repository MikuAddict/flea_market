package com.zhp.flea_market.controller;

import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.model.dto.response.StatisticsResponse;
import com.zhp.flea_market.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.Date;
import java.util.List;

/**
 * 统计分析接口
 */
@RestController
@RequestMapping("/statistics")
@Slf4j
@Tag(name = "统计分析", description = "数据统计与分析接口")
public class StatisticsController extends BaseController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 获取月度交易二手物品分类排行
     *
     * @param month 月份
     * @param year 年份
     * @param limit 限制数量
     * @param request HTTP请求
     * @return 二手物品分类排行列表
     */
    @Operation(summary = "获取月度交易二手物品分类排行", description = "获取指定月份的交易二手物品分类排行榜")
    @GetMapping("/monthly-categories")
    @LoginRequired
    public BaseResponse<List<StatisticsResponse.ProductRankingItem>> getMonthlyTopSellingCategories(
            @Parameter(description = "月份") @RequestParam int month,
            @Parameter(description = "年份") @RequestParam int year,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        // 参数校验
        if (month < 1 || month > 12) {
            throw new com.zhp.flea_market.exception.BusinessException(
                    com.zhp.flea_market.common.ErrorCode.PARAMS_ERROR, "月份必须在1-12之间");
        }
        if (year < 2020 || year > 2030) {
            throw new com.zhp.flea_market.exception.BusinessException(
                    com.zhp.flea_market.common.ErrorCode.PARAMS_ERROR, "年份必须在2020-2030之间");
        }

        // 获取月度交易二手物品分类排行
        List<StatisticsResponse.ProductRankingItem> result = 
                statisticsService.getMonthlyTopSellingCategories(month, year, limit);
        
        logOperation("获取月度交易二手物品分类排行", request, 
                "月份", month,
                "年份", year,
                "限制数量", limit
        );
        return ResultUtils.success(result);
    }

    /**
     * 获取活跃用户排行
     *
     * @param limit 限制数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 用户排行列表
     */
    @Operation(summary = "获取活跃用户排行", description = "获取指定时间范围内的活跃用户排行榜")
    @GetMapping("/active-users")
    @LoginRequired
    public BaseResponse<List<StatisticsResponse.UserRankingItem>> getActiveUsersRanking(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "开始日期") @RequestParam 
                @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam 
                @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        // 获取活跃用户排行
        List<StatisticsResponse.UserRankingItem> result = 
                statisticsService.getActiveUsersRanking(limit, startDate, endDate);
        
        logOperation("获取活跃用户排行", request, 
                "限制数量", limit,
                "开始日期", startDate,
                "结束日期", endDate
        );
        return ResultUtils.success(result);
    }

    /**
     * 获取需求量大二手物品分类排行
     *
     * @param limit 限制数量
     * @param request HTTP请求
     * @return 二手物品分类排行列表
     */
    @Operation(summary = "获取需求量大二手物品分类排行", description = "获取需求量大的二手物品分类排行榜")
    @GetMapping("/high-demand-categories")
    @LoginRequired
    public BaseResponse<List<StatisticsResponse.ProductRankingItem>> getHighDemandCategories(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        // 获取需求量大二手物品分类排行
        List<StatisticsResponse.ProductRankingItem> result = 
                statisticsService.getHighDemandCategories(limit);
        
        logOperation("获取需求量大二手物品分类排行", request, "限制数量", limit);
        return ResultUtils.success(result);
    }

    /**
     * 获取闲置量大二手物品分类排行（闲置量 = 在售物品数量 / 已售物品数量）
     *
     * @param limit 限制数量
     * @param request HTTP请求
     * @return 二手物品分类排行列表
     */
    @Operation(summary = "获取闲置量大二手物品分类排行", description = "获取闲置量大的二手物品分类排行榜（闲置量 = 在售物品数量 / 已售物品数量）")
    @GetMapping("/high-inventory-categories")
    @LoginRequired
    public BaseResponse<List<StatisticsResponse.ProductRankingItem>> getHighInventoryCategories(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        // 获取闲置量大二手物品分类排行
        List<StatisticsResponse.ProductRankingItem> result = 
                statisticsService.getHighInventoryCategories(limit);
        
        logOperation("获取闲置量大二手物品分类排行", request, "限制数量", limit);
        return ResultUtils.success(result);
    }

    /**
     * 获取综合统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 综合统计信息
     */
    @Operation(summary = "获取综合统计信息", description = "获取指定时间范围内的综合统计信息")
    @GetMapping("/comprehensive")
    @LoginRequired
    public BaseResponse<StatisticsResponse> getComprehensiveStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) 
                @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) 
                @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletRequest request) {
        // 获取综合统计信息
        StatisticsResponse result = statisticsService.getComprehensiveStatistics(startDate, endDate);
        
        logOperation("获取综合统计信息", request, 
                "开始日期", startDate,
                "结束日期", endDate
        );
        return ResultUtils.success(result);
    }

    /**
     * 获取月度统计数据
     *
     * @param month 月份
     * @param year 年份
     * @param request HTTP请求
     * @return 月度统计数据
     */
    @Operation(summary = "获取月度统计数据", description = "获取指定月份的统计数据")
    @GetMapping("/monthly")
    @LoginRequired
    public BaseResponse<StatisticsResponse> getMonthlyStatistics(
            @Parameter(description = "月份") @RequestParam int month,
            @Parameter(description = "年份") @RequestParam int year,
            HttpServletRequest request) {
        // 参数校验
        if (month < 1 || month > 12) {
            throw new com.zhp.flea_market.exception.BusinessException(
                    com.zhp.flea_market.common.ErrorCode.PARAMS_ERROR, "月份必须在1-12之间");
        }
        int currentYear = Year.now().getValue();
        int minYear = currentYear - 10;
        if (year < minYear || year > currentYear) {
            throw new com.zhp.flea_market.exception.BusinessException(
                    com.zhp.flea_market.common.ErrorCode.PARAMS_ERROR,
                    String.format("年份必须在 %d 到 %d 之间", minYear, currentYear));
        }

        // 获取月度统计数据
        StatisticsResponse result = statisticsService.getMonthlyStatistics(month, year);
        
        logOperation("获取月度统计数据", request, 
                "月份", month,
                "年份", year
        );
        return ResultUtils.success(result);
    }

    /**
     * 获取用户交易统计
     *
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 用户交易统计
     */
    @Operation(summary = "获取用户交易统计", description = "获取指定用户的交易统计数据")
    @GetMapping("/user/{userId}")
    @LoginRequired
    public BaseResponse<StatisticsResponse> getUserTradeStatistics(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            HttpServletRequest request) {
        // 参数校验
        validateId(userId, "用户ID");

        // 获取用户交易统计
        StatisticsResponse result = statisticsService.getUserTradeStatistics(userId);
        
        logOperation("获取用户交易统计", request, "用户ID", userId);
        return ResultUtils.success(result);
    }

    /**
     * 获取二手物品分类交易统计
     *
     * @param categoryId 分类ID
     * @param request HTTP请求
     * @return 二手物品分类交易统计
     */
    @Operation(summary = "获取二手物品分类交易统计", description = "获取指定分类的二手物品交易统计数据")
    @GetMapping("/category/{categoryId}")
    @LoginRequired
    public BaseResponse<StatisticsResponse> getCategoryTradeStatistics(
            @Parameter(description = "分类ID") @PathVariable Long categoryId,
            HttpServletRequest request) {
        // 参数校验
        validateId(categoryId, "分类ID");

        // 获取二手物品分类交易统计
        StatisticsResponse result = statisticsService.getCategoryTradeStatistics(categoryId);
        
        logOperation("获取二手物品分类交易统计", request, "分类ID", categoryId);
        return ResultUtils.success(result);
    }
}