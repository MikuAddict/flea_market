package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.ReviewMapper;
import com.zhp.flea_market.model.dto.request.ReviewRequest;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.Review;
import com.zhp.flea_market.model.entity.TradeRecord;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.OrderService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.ReviewService;
import com.zhp.flea_market.service.TradeRecordService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品评价服务实现类
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private TradeRecordService tradeRecordService;

    /**
     * 添加评价
     *
     * @param review 评价信息
     * @param request HTTP请求
     * @return 是否添加成功
     */
    @Override
    public boolean addReview(Review review, HttpServletRequest request) {
        // 参数校验
        validateReviewParams(review);
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 约束条件1：评论功能仅限买家对订单进行评价
        // 必须提供订单ID，且订单必须存在
        if (review.getOrderId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须提供订单ID进行评价");
        }
        
        // 检查订单是否存在且已完成
        Order order = orderService.getById(review.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        
        // 检查订单是否已完成
        if (order.getStatus() != 2) { // 订单状态2表示已完成
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能对已完成的订单进行评价");
        }
        
        // 检查当前用户是否为订单的买家
        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有订单买家才能进行评价");
        }
        
        // 约束条件2：每个订单仅允许买家提交一条评论
        Review existingReview = getReviewByOrderId(review.getOrderId());
        if (existingReview != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该订单已经评价过，每个订单只能评价一次");
        }
        
        // 检查商品是否存在
        Product product = productService.getById(review.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        
        // 验证商品ID与订单中的商品ID一致
        if (!order.getProductId().equals(review.getProductId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID与订单中的商品不一致");
        }
        
        // 设置评价信息
        review.setUserId(currentUser.getId());
        review.setCreateTime(new Date());
        
        boolean saved = this.save(review);
        
        // 评价完成后，根据评分调整卖家积分，并关联交易记录
        if (saved) {
            try {
                // 根据评分计算积分变化
                int sellerPointsChange = calculateSellerPointsChange(review.getRating());
                
                // 获取卖家ID并更新积分
                Product sellerProduct = productService.getById(review.getProductId());
                if (sellerProduct != null && sellerProduct.getUser() != null) {
                    userService.updateUserPoints(sellerProduct.getUser().getId(), sellerPointsChange);
                }
                
                // 关联交易记录
                if (review.getOrderId() != null) {
                    try {
                        // 获取订单对应的交易记录
                        List<TradeRecord> tradeRecords = tradeRecordService.list(
                            tradeRecordService.getQueryWrapper(null, null, null, null, null)
                                .eq("order_id", review.getOrderId())
                        );
                        
                        if (!tradeRecords.isEmpty()) {
                            TradeRecord tradeRecord = tradeRecords.get(0);
                            tradeRecordService.linkReview(tradeRecord.getId(), review.getId());
                        }
                    } catch (Exception e) {
                        // 交易记录关联失败不影响评价保存状态，但记录日志
                        System.err.println("评价保存时关联交易记录失败: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                // 积分更新失败不影响评价保存状态，但记录日志
                System.err.println("评价保存时积分更新失败: " + e.getMessage());
            }
        }
        
        return saved;
    }



    /**
     * 删除评价
     *
     * @param id 评价ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Override
    public boolean deleteReview(Long id, HttpServletRequest request) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查评价是否存在
        Review review = this.getById(id);
        if (review == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }
        
        // 权限校验：只能删除自己的评价
        if (!review.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该评价");
        }
        
        return this.removeById(id);
    }

    /**
     * 获取评价详情
     *
     * @param id 评价ID
     * @return 评价详情
     */
    @Override
    public Review getReviewDetail(Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价ID无效");
        }
        
        Review review = this.getById(id);
        if (review == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }
        
        return review;
    }

    /**
     * 分页获取评价列表
     *
     * @param page 分页参数
     * @return 评价列表
     */
    @Override
    public List<Review> getReviewList(Page<Review> page) {
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        
        Page<Review> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 根据商品ID获取评价列表
     *
     * @param productId 商品ID
     * @param page 分页参数
     * @return 评价列表
     */
    @Override
    public List<Review> getReviewsByProductId(Long productId, Page<Review> page) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.orderByDesc("create_time");
        
        Page<Review> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 根据用户ID获取评价列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 评价列表
     */
    @Override
    public List<Review> getReviewsByUserId(Long userId, Page<Review> page) {
        // 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }
        
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        
        Page<Review> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 根据订单ID获取评价列表
     *
     * @param orderId 订单ID
     * @param page 分页参数
     * @return 评价列表
     */
    @Override
    public List<Review> getReviewsByOrderId(Long orderId, Page<Review> page) {
        // 参数校验
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.orderByDesc("create_time");
        
        Page<Review> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 获取用户对商品的评价
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 评价信息
     */
    @Override
    public Review getUserReviewForProduct(Long userId, Long productId) {
        // 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("product_id", productId);
        
        return this.getOne(queryWrapper);
    }

    /**
     * 获取商品平均评分
     *
     * @param productId 商品ID
     * @return 平均评分
     */
    @Override
    public Double getAverageRatingByProductId(Long productId) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.select("AVG(rating) as avg_rating");
        
        List<Review> reviews = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(reviews)) {
            return 0.0;
        }
        
        // 计算平均分
        double totalRating = 0.0;
        int count = 0;
        for (Review review : reviews) {
            if (review.getRating() != null) {
                totalRating += review.getRating();
                count++;
            }
        }
        
        return count > 0 ? totalRating / count : 0.0;
    }

    /**
     * 根据订单ID获取评价
     *
     * @param orderId 订单ID
     * @return 评价信息
     */
    private Review getReviewByOrderId(Long orderId) {
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return this.getOne(queryWrapper);
    }

    /**
     * 获取商品评价统计信息
     *
     * @param productId 商品ID
     * @return 评价统计信息
     */
    @Override
    public ReviewRequest getReviewStatisticsByProductId(Long productId) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        ReviewRequest statistics = new ReviewRequest();
        
        // 获取所有评价
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        List<Review> reviews = this.list(queryWrapper);
        
        if (CollectionUtils.isEmpty(reviews)) {
            return statistics;
        }
        
        // 统计信息
        statistics.setTotalReviews(reviews.size());
        
        double totalRating = 0.0;
        int fiveStarCount = 0;
        int fourStarCount = 0;
        int threeStarCount = 0;
        int twoStarCount = 0;
        int oneStarCount = 0;
        
        for (Review review : reviews) {
            if (review.getRating() != null) {
                totalRating += review.getRating();
                
                switch (review.getRating()) {
                    case 5: fiveStarCount++; break;
                    case 4: fourStarCount++; break;
                    case 3: threeStarCount++; break;
                    case 2: twoStarCount++; break;
                    case 1: oneStarCount++; break;
                }
            }
        }
        
        statistics.setAverageRating(reviews.size() > 0 ? totalRating / reviews.size() : 0.0);
        statistics.setFiveStarCount(fiveStarCount);
        statistics.setFourStarCount(fourStarCount);
        statistics.setThreeStarCount(threeStarCount);
        statistics.setTwoStarCount(twoStarCount);
        statistics.setOneStarCount(oneStarCount);
        
        return statistics;
    }

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
    @Override
    public QueryWrapper<Review> getQueryWrapper(Long productId, Long userId, Long orderId, Integer minRating, Integer maxRating) {
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        
        if (productId != null && productId > 0) {
            queryWrapper.eq("product_id", productId);
        }
        
        if (userId != null && userId > 0) {
            queryWrapper.eq("user_id", userId);
        }
        
        if (orderId != null && orderId > 0) {
            queryWrapper.eq("order_id", orderId);
        }
        
        if (minRating != null && minRating >= 1 && minRating <= 5) {
            queryWrapper.ge("rating", minRating);
        }
        
        if (maxRating != null && maxRating >= 1 && maxRating <= 5) {
            queryWrapper.le("rating", maxRating);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return queryWrapper;
    }

    /**
     * 验证评价参数
     *
     * @param review 评价信息
     */
    private void validateReviewParams(Review review) {
        if (review == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价信息不能为空");
        }
        
        if (review.getProductId() == null || review.getProductId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在1-5分之间");
        }
        
        if (StringUtils.isBlank(review.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价内容不能为空");
        }
        
        if (review.getContent().length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价内容不能超过500字");
        }
    }

    /**
     * 根据评分计算卖家积分变化
     *
     * @param rating 评分 (1-5分)
     * @return 积分变化值
     */
    private int calculateSellerPointsChange(Integer rating) {
        if (rating == null) {
            return 0;
        }
        
        switch (rating) {
            case 5: return 10;  // 五星评价：卖家增加10积分
            case 4: return 5;   // 四星评价：卖家增加5积分
            case 3: return 0;   // 三星评价：不增减积分
            case 2: return -5;  // 二星评价：卖家扣除5积分
            case 1: return -10; // 一星评价：卖家扣除10积分
            default: return 0;
        }
    }
}