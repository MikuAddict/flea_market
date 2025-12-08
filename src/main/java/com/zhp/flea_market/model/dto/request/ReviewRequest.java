package com.zhp.flea_market.model.dto.request;

import lombok.Data;

/**
 * 二手物品评价统计请求
 */
@Data
public class ReviewRequest {
    
    /**
     * 总评价数
     */
    private int totalReviews;
    
    /**
     * 平均评分
     */
    private double averageRating;
    
    /**
     * 五星评价数
     */
    private int fiveStarCount;
    
    /**
     * 四星评价数
     */
    private int fourStarCount;
    
    /**
     * 三星评价数
     */
    private int threeStarCount;
    
    /**
     * 二星评价数
     */
    private int twoStarCount;
    
    /**
     * 一星评价数
     */
    private int oneStarCount;
}