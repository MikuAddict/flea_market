package com.zhp.flea_market.controller;

import com.zhp.flea_market.model.dto.response.MonthlyStatisticsResponse;
import com.zhp.flea_market.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计分析接口
 */
@RestController
@RequestMapping("/statistics")
@Slf4j
@Tag(name = "统计分析", description = "数据统计与分析接口")
@RequiredArgsConstructor
public class StatisticsController extends BaseController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly")
    @Operation(summary = "获取月度统计数据", description = "获取指定月份的完整统计数据")
    public MonthlyStatisticsResponse getMonthlyStatistics(
            @Parameter(description = "年份", example = "2024")
            @RequestParam Integer year,
            @Parameter(description = "月份", example = "12")
            @RequestParam Integer month) {
        log.info("获取月度统计数据: {}年{}月", year, month);
        return statisticsService.getMonthlyStatistics(year, month);
    }

    @GetMapping("/monthly/category-ranking")
    @Operation(summary = "获取每月交易成功物品分类排行", description = "获取指定月份交易成功的物品分类排行")
    public MonthlyStatisticsResponse getMonthlyCategoryRanking(
            @Parameter(description = "年份", example = "2024")
            @RequestParam Integer year,
            @Parameter(description = "月份", example = "12")
            @RequestParam Integer month) {
        log.info("获取每月交易成功物品分类排行: {}年{}月", year, month);
        return statisticsService.getMonthlyCategoryRanking(year, month);
    }

    @GetMapping("/monthly/active-user-ranking")
    @Operation(summary = "获取每月活跃用户排行", description = "获取指定月份活跃用户排行(按交易次数)")
    public MonthlyStatisticsResponse getMonthlyActiveUserRanking(
            @Parameter(description = "年份", example = "2024")
            @RequestParam Integer year,
            @Parameter(description = "月份", example = "12")
            @RequestParam Integer month) {
        log.info("获取每月活跃用户排行: {}年{}月", year, month);
        return statisticsService.getMonthlyActiveUserRanking(year, month);
    }

    @GetMapping("/monthly/category-on-sale")
    @Operation(summary = "获取每月物品分类在售量", description = "获取指定月份物品分类在售量(仅限status=1)")
    public MonthlyStatisticsResponse getMonthlyCategoryOnSaleInventory(
            @Parameter(description = "年份", example = "2024")
            @RequestParam Integer year,
            @Parameter(description = "月份", example = "12")
            @RequestParam Integer month) {
        log.info("获取每月物品分类在售量: {}年{}月", year, month);
        return statisticsService.getMonthlyCategoryOnSaleInventory(year, month);
    }

    @GetMapping("/monthly/category-sold")
    @Operation(summary = "获取每月物品分类已售量", description = "获取指定月份物品分类已售量(仅限status=3)")
    public MonthlyStatisticsResponse getMonthlyCategorySoldInventory(
            @Parameter(description = "年份", example = "2024")
            @RequestParam Integer year,
            @Parameter(description = "月份", example = "12")
            @RequestParam Integer month) {
        log.info("获取每月物品分类已售量: {}年{}月", year, month);
        return statisticsService.getMonthlyCategorySoldInventory(year, month);
    }
}