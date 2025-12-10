package com.zhp.flea_market.model.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 月度统计数据响应
 */
@Data
public class MonthlyStatisticsResponse {

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 每月交易成功物品分类排行
     */
    private List<CategoryRankingItem> monthlyCategoryRanking;

    /**
     * 每月活跃用户排行(交易次数)
     */
    private List<UserRankingItem> activeUserRanking;

    /**
     * 每月物品分类在售量(仅限status=1)
     */
    private List<CategoryInventoryItem> categoryOnSaleInventory;

    /**
     * 每月物品分类已售量(仅限status=3)
     */
    private List<CategoryInventoryItem> categorySoldInventory;

    /**
     * 分类排行项
     */
    @Data
    public static class CategoryRankingItem {
        /**
         * 分类ID
         */
        private Long categoryId;

        /**
         * 分类名称
         */
        private String categoryName;

        /**
         * 交易成功次数
         */
        private Long tradeCount;

        /**
         * 交易总金额
         */
        private Double totalAmount;
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
         * 用户头像
         */
        private String avatar;

        /**
         * 交易次数
         */
        private Long tradeCount;

        /**
         * 交易总金额
         */
        private Double totalAmount;
    }

    /**
     * 分类库存项
     */
    @Data
    public static class CategoryInventoryItem {
        /**
         * 分类ID
         */
        private Long categoryId;

        /**
         * 分类名称
         */
        private String categoryName;

        /**
         * 物品数量
         */
        private Long itemCount;
    }
}