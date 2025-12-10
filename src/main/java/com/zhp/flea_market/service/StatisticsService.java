package com.zhp.flea_market.service;

import com.zhp.flea_market.model.dto.response.MonthlyStatisticsResponse;

/**
 * 统计分析服务接口
 */
public interface StatisticsService {

    /**
     * 获取每月统计数据
     * @param year 年份
     * @param month 月份
     * @return 月度统计数据响应
     */
    MonthlyStatisticsResponse getMonthlyStatistics(Integer year, Integer month);

    /**
     * 获取每月交易成功物品分类排行
     * @param year 年份
     * @param month 月份
     * @return 分类排行列表
     */
    MonthlyStatisticsResponse getMonthlyCategoryRanking(Integer year, Integer month);

    /**
     * 获取每月活跃用户排行(交易次数)
     * @param year 年份
     * @param month 月份
     * @return 用户排行列表
     */
    MonthlyStatisticsResponse getMonthlyActiveUserRanking(Integer year, Integer month);

    /**
     * 获取每月物品分类在售量(仅限status=1)
     * @param year 年份
     * @param month 月份
     * @return 分类在售量列表
     */
    MonthlyStatisticsResponse getMonthlyCategoryOnSaleInventory(Integer year, Integer month);

    /**
     * 获取每月物品分类已售量(仅限status=3)
     * @param year 年份
     * @param month 月份
     * @return 分类已售量列表
     */
    MonthlyStatisticsResponse getMonthlyCategorySoldInventory(Integer year, Integer month);
}