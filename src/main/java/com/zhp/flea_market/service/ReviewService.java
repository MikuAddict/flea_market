package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.Review;
import com.zhp.flea_market.model.dto.request.ReviewRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 商品评价服务接口
 */
public interface ReviewService extends IService<Review> {

    /**
     * 添加评价
     *
     * @param review 评价信息
     * @param request HTTP请求
     * @return 是否添加成功
     */
    boolean addReview(Review review, HttpServletRequest request);

    /**
     * 更新评价
     *
     * @param review 评价信息
     * @param request HTTP请求
     * @return 是否更新成功
     */
    boolean updateReview(Review review, HttpServletRequest request);

    /**
     * 删除评价
     *
     * @param id 评价ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    boolean deleteReview(Long id, HttpServletRequest request);

    /**
     * 获取评价详情
     *
     * @param id 评价ID
     * @return 评价详情
     */
    Review getReviewDetail(Long id);

    /**
     * 分页获取评价列表
     *
     * @param page 分页参数
     * @return 评价列表
     */
    List<Review> getReviewList(Page<Review> page);

    /**
     * 根据商品ID获取评价列表
     *
     * @param productId 商品ID
     * @param page 分页参数
     * @return 评价列表
     */
    List<Review> getReviewsByProductId(Long productId, Page<Review> page);

    /**
     * 根据用户ID获取评价列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 评价列表
     */
    List<Review> getReviewsByUserId(Long userId, Page<Review> page);

    /**
     * 根据订单ID获取评价列表
     *
     * @param orderId 订单ID
     * @param page 分页参数
     * @return 评价列表
     */
    List<Review> getReviewsByOrderId(Long orderId, Page<Review> page);

    /**
     * 获取用户对商品的评价
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 评价信息
     */
    Review getUserReviewForProduct(Long userId, Long productId);

    /**
     * 获取商品平均评分
     *
     * @param productId 商品ID
     * @return 平均评分
     */
    Double getAverageRatingByProductId(Long productId);

    /**
     * 获取商品评价统计信息
     *
     * @param productId 商品ID
     * @return 评价统计信息
     */
    ReviewRequest getReviewStatisticsByProductId(Long productId);

    /**
     * 获取查询条件
     *
     * @param productId 商品ID
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @return 查询条件
     */
    QueryWrapper<Review> getQueryWrapper(Long productId, Long userId, Long orderId, Integer minRating, Integer maxRating);
}