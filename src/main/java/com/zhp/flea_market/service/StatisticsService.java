package com.zhp.flea_market.service;

import com.zhp.flea_market.model.dto.response.StatisticsResponse;

import java.util.Date;
import java.util.List;

/**
 * 统计分析服务接口
 */
public interface StatisticsService {

    /**
     * 获取月度交易二手物品排行
     *
     * @param month 月份
     * @param year 年份
     * @param limit 限制数量
     * @return 二手物品排行列表
     */
    List<StatisticsResponse.ProductRankingItem> getMonthlyTopSellingProducts(int month, int year, int limit);

    /**
     * 获取活跃用户排行
     *
     * @param limit 限制数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户排行列表
     */
    List<StatisticsResponse.UserRankingItem> getActiveUsersRanking(int limit, Date startDate, Date endDate);

    /**
     * 获取需求量大二手物品排行
     *
     * @param limit 限制数量
     * @return 二手物品排行列表
     */
    List<StatisticsResponse.ProductRankingItem> getHighDemandProducts(int limit);

    /**
     * 获取闲置量大二手物品排行
     *
     * @param limit 限制数量
     * @return 二手物品排行列表
     */
    List<StatisticsResponse.ProductRankingItem> getHighInventoryProducts(int limit);

    /**
     * 获取综合统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 综合统计信息
     */
    StatisticsResponse getComprehensiveStatistics(Date startDate, Date endDate);

    /**
     * 获取月度统计数据
     *
     * @param month 月份
     * @param year 年份
     * @return 月度统计数据
     */
    StatisticsResponse getMonthlyStatistics(int month, int year);

    /**
     * 获取用户交易统计
     *
     * @param userId 用户ID
     * @return 用户交易统计
     */
    StatisticsResponse getUserTradeStatistics(Long userId);

    /**
     * 获取二手物品交易统计
     *
     * @param productId 二手物品ID
     * @return 二手物品交易统计
     */
    StatisticsResponse getProductTradeStatistics(Long productId);
}