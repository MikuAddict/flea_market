package com.zhp.flea_market.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 统计数据响应
 */
@Data
public class StatisticsResponse {

    /**
     * 统计类型
     */
    private String statisticsType;

    /**
     * 统计时间
     */
    private Date statisticsTime;

    /**
     * 总交易金额
     */
    private BigDecimal totalTradeAmount;

    /**
     * 总交易数量
     */
    private Long totalTradeCount;

    /**
     * 月度交易二手物品分类排行
     */
    private List<CategoryRankingItem> monthlyCategoryRanking;

    /**
     * 活跃用户排行
     */
    private List<UserRankingItem> activeUserRanking;

    /**
     * 需求量大二手物品分类排行
     */
    private List<CategoryRankingItem> highDemandRanking;

    /**
     * 闲置量大二手物品分类排行
     */
    private List<CategoryRankingItem> highInventoryRanking;

    /**
     * 二手物品分类排行项
     */
    @Data
    public static class CategoryRankingItem {
        /**
         * 二手物品分类ID
         */
        private Long categoryId;

        /**
         * 二手物品分类名称
         */
        private String categoryName;

        /**
         * 交易次数/数量
         */
        private Long categoryCount;

        /**
         * 交易金额
         */
        private BigDecimal tradeAmount;
    }

    /**
     * 用户排行项
     */
    @Data
    public static class UserRankingItem {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String userName;

        /**
         * 交易次数
         */
        private Long tradeCount;

        /**
         * 交易金额
         */
        private BigDecimal tradeAmount;

        /**
         * 用户头像
         */
        private String avatar;
    }
}