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
     * 月度交易二手物品排行
     */
    private List<ProductRankingItem> monthlyProductRanking;

    /**
     * 活跃用户排行
     */
    private List<UserRankingItem> activeUserRanking;

    /**
     * 需求量大二手物品排行
     */
    private List<ProductRankingItem> highDemandRanking;

    /**
     * 闲置量大二手物品排行
     */
    private List<ProductRankingItem> highInventoryRanking;

    /**
     * 二手物品排行项
     */
    @Data
    public static class ProductRankingItem {
        /**
         * 二手物品ID
         */
        private Long productId;

        /**
         * 二手物品名称
         */
        private String productName;

        /**
         * 交易次数
         */
        private Long tradeCount;

        /**
         * 交易金额
         */
        private BigDecimal tradeAmount;

        /**
         * 二手物品图片
         */
        private String imageUrl;
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