package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.Review;
import com.zhp.flea_market.model.vo.ReviewVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 二手物品评价服务接口
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
     * 获取评价详情
     *
     * @param id 评价ID
     * @return 评价详情
     */
    ReviewVO getReviewDetail(Long id);

    /**
     * 分页获取评价列表
     *
     * @param page 分页参数
     * @return 评价列表视图
     */
    List<ReviewVO> getReviewList(Page<Review> page);

    /**
     * 根据用户ID获取评价列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 评价列表视图
     */
    List<ReviewVO> getReviewsByUserId(Long userId, Page<Review> page);

    /**
     * 根据订单ID获取评价
     *
     * @param orderId 订单ID
     * @return 评价信息视图
     */
    ReviewVO getReviewByOrderId(Long orderId);

    /**
     * 获取查询条件
     *
     * @param productId 二手物品ID
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @return 查询条件
     */
    QueryWrapper<Review> getQueryWrapper(Long productId, Long userId, Long orderId, Integer minRating, Integer maxRating);
}