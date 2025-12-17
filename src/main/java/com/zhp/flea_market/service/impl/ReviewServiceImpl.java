package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.ReviewMapper;
import com.zhp.flea_market.model.entity.*;
import com.zhp.flea_market.model.vo.ReviewVO;
import com.zhp.flea_market.service.*;
import com.zhp.flea_market.utils.PageUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 二手物品评价服务实现类
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
        
        // 必须提供订单ID，且订单必须存在
        if (review.getOrderId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "必须提供有效的订单ID进行评价");
        }
        Order order = orderService.getById(review.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        
        // 检查订单是否已完成
        if (order.getStatus() != 2) { // 订单状态2表示已完成
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能对已完成的订单进行评价");
        }
        
        // 检查当前用户是否为订单的买家
        if (order.getBuyerId() == null || !order.getBuyerId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有订单买家才能进行评价");
        }
        
        // 约束条件2：如果订单有评论,则拒绝再次评论
        List<Review> reviews = this.list(
            this.getQueryWrapper(null, null, null, null, null)
                .eq("order_id", review.getOrderId())
        );
        if (!CollectionUtils.isEmpty(reviews)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单已有评价，无法再次评价");
        }
        // 检查二手物品是否存在
        Product product = productService.getById(review.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "二手物品不存在");
        }
        
        // 验证二手物品ID与订单中的二手物品ID一致
        if (!order.getProductId().equals(review.getProductId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID与订单中的二手物品不一致");
        }
        
        // 设置评价信息
        review.setUserId(currentUser.getId());
        review.setCreateTime(new Date());
        
        boolean saved = this.save(review);
        
        // 评价完成后，关联交易记录
        if (saved) {
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
        
        return saved;
    }

    /**
     * 获取评价详情
     *
     * @param id 评价ID
     * @return 评价详情
     */
    @Override
    public ReviewVO getReviewDetail(Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价ID无效");
        }
        
        Review review = this.getById(id);
        if (review == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }
        
        // 转换为VO对象

        return convertToReviewVO(review);
    }

    /**
     * 分页获取评价列表
     *
     * @param page 分页参数
     * @return 评价列表视图
     */
    @Override
    public List<ReviewVO> getReviewList(Page<Review> page) {
        List<Review> reviews = PageUtils.getPageResult(this, page, queryWrapper -> queryWrapper.orderByDesc("create_time"));
        return convertToReviewVOList(reviews);
    }

    /**
     * 根据用户ID获取评价列表（作为卖家收到的评价）
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 评价列表视图
     */
    @Override
    public List<ReviewVO> getReviewsByUserId(Long userId, Page<Review> page) {
        // 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }
        
        // 获取该用户发布的所有二手物品ID
        List<Product> userProducts = productService.getUserProducts(userId, new Page<>(1, Integer.MAX_VALUE));
        List<Long> productIds = userProducts.stream()
                .map(Product::getId)
                .toList();
        
        // 如果该用户没有发布过二手物品，则返回空列表
        if (productIds.isEmpty()) {
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            return new ArrayList<>();
        }
        
        // 查询这些二手物品收到的评价
        List<Review> reviews = PageUtils.getPageResult(this, page, queryWrapper -> {
            queryWrapper.in("product_id", productIds);
            queryWrapper.orderByDesc("create_time");
        });
        
        return convertToReviewVOList(reviews);
    }

    /**
     * 根据订单ID获取评价
     *
     * @param orderId 订单ID
     * @return 评价信息视图
     */
    public ReviewVO getReviewByOrderId(Long orderId) {
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        Review review = this.getOne(queryWrapper);
        
        if (review == null) {
            return null;
        }
        
        return convertToReviewVO(review);
    }

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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID无效");
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
     * 将评价实体转换为VO对象
     *
     * @param review 评价实体
     * @return 评价VO对象
     */
    private ReviewVO convertToReviewVO(Review review) {
        ReviewVO reviewVO = new ReviewVO();
        
        // 复制基本属性
        reviewVO.setId(review.getId());
        reviewVO.setUserId(review.getUserId());
        reviewVO.setProductId(review.getProductId());
        reviewVO.setOrderId(review.getOrderId());
        reviewVO.setRating(review.getRating());
        reviewVO.setContent(review.getContent());
        reviewVO.setCreateTime(review.getCreateTime());

        
        // 获取用户信息
        User user = userService.getById(review.getUserId());
        if (user != null) {
            reviewVO.setUserName(user.getUserName());
            reviewVO.setUserAvatar(user.getUserAvatar());
        }
        
        // 获取二手物品信息
        Product product = productService.getById(review.getProductId());
        if (product != null) {
            reviewVO.setProductName(product.getProductName());
        }
        
        return reviewVO;
    }
    
    /**
     * 将评价实体列表转换为VO对象列表
     *
     * @param reviews 评价实体列表
     * @return 评价VO对象列表
     */
    private List<ReviewVO> convertToReviewVOList(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ReviewVO> reviewVOs = new ArrayList<>();
        for (Review review : reviews) {
            reviewVOs.add(convertToReviewVO(review));
        }
        
        return reviewVOs;
    }
}