package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.model.dto.request.DeleteRequest;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.Review;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.ReviewVO;
import com.zhp.flea_market.service.OrderService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.ReviewService;
import com.zhp.flea_market.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 二手物品评价接口
 */
@RestController
@RequestMapping("/review")
@Slf4j
@Tag(name = "二手物品评价管理", description = "二手物品评价的增删改查、统计等接口")
public class ReviewController extends BaseController {

    @Resource
    private ReviewService reviewService;

    @Resource
    private UserService userService;

    @Resource
    private ProductService productService;

    @Resource
    private OrderService orderService;

    /**
     * 添加评价
     *
     * @param review 评价信息
     * @param request HTTP请求
     * @return 新增评价的ID
     */
    @Operation(summary = "添加评价", description = "用户对已完成的订单进行评价")
    @PostMapping("/add")
    @LoginRequired
    public BaseResponse<Long> addReview(
            @Parameter(description = "评价信息") @RequestBody Review review,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(review, "评价信息");
        validateNotNull(review.getProductId(), "二手物品信息");
        validateId(review.getProductId(), "二手物品ID");
        validateNotNull(review.getRating(), "评分");
        validateNotBlank(review.getContent(), "评价内容");

        // 检查二手物品是否存在
        Product product = productService.getById(review.getProductId());
        validateResourceExists(product, "二手物品");

        // 检查订单是否存在（如果提供了订单ID）
        if (review.getOrderId() != null) {
            Order order = orderService.getById(review.getOrderId());
            validateResourceExists(order, "订单");
        }

        // 添加评价
        boolean result = reviewService.addReview(review, request);
        
        logOperation("添加评价", result, request, 
                "二手物品ID", review.getProductId(),
                "订单ID", review.getOrderId(),
                "评分", review.getRating()
        );
        return handleOperationResult(result, "评价添加成功", review.getId());
    }

    /**
     * 删除评价
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Operation(summary = "删除评价", description = "用户删除自己的评价")
    @PostMapping("/delete")
    @LoginRequired
    public BaseResponse<Boolean> deleteReview(
            @Parameter(description = "删除请求") @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(deleteRequest, "删除请求");
        validateId(deleteRequest.getId(), "评价ID");

        // 检查评价是否存在
        validateResourceExists(reviewService.getById(deleteRequest.getId()), "评价");

        // 删除评价
        boolean result = reviewService.deleteReview(deleteRequest.getId(), request);
        
        logOperation("删除评价", result, request, "评价ID", deleteRequest.getId());
        return handleOperationResult(result, "评价删除成功");
    }

    /**
     * 根据ID获取评价详情
     *
     * @param id 评价ID
     * @return 评价详情
     */
    @Operation(summary = "获取评价详情", description = "根据评价ID获取评价详细信息")
    @GetMapping("/get/{id}")
    public BaseResponse<ReviewVO> getReviewById(
            @Parameter(description = "评价ID") @PathVariable Long id) {
        // 参数校验
        validateId(id, "评价ID");

        // 获取评价信息
        ReviewVO review = reviewService.getReviewDetail(id);
        validateResourceExists(review, "评价");

        logOperation("获取评价详情", null, "评价ID", id);
        return ResultUtils.success(review);
    }

    /**
     * 分页获取评价列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页评价列表
     */
    @Operation(summary = "分页获取评价列表", description = "分页获取所有评价列表")
    @GetMapping("/list/page")
    public BaseResponse<Page<Review>> listReviewByPage(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        // 参数校验
        Page<Review> page = validatePageParams(current, size);

        
        logOperation("分页获取评价列表", null, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 根据用户ID获取评价列表
     *
     * @param userId 用户ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页评价列表
     */
    @Operation(summary = "根据用户ID获取评价列表", description = "根据用户ID分页获取评价列表")
    @GetMapping("/list/user/{userId}")
    public BaseResponse<Page<Review>> listReviewsByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        // 参数校验
        validateId(userId, "用户ID");
        Page<Review> page = validatePageParams(current, size);

        // 检查用户是否存在
        validateResourceExists(userService.getById(userId), "用户");

        logOperation("根据用户ID获取评价列表", null, 
                "用户ID", userId,
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 根据订单ID获取评价
     *
     * @param orderId 订单ID
     * @return 评价信息
     */
    @Operation(summary = "根据订单ID获取评价", description = "根据订单ID获取评价信息（一个订单只能有一条评价）")
    @GetMapping("/get/order/{orderId}")
    public BaseResponse<Review> getReviewByOrderId(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 检查订单是否存在
        validateResourceExists(orderService.getById(orderId), "订单");

        // 获取评价信息
        Review review = reviewService.getReviewByOrderId(orderId);
        
        logOperation("根据订单ID获取评价", null, "订单ID", orderId);
        return ResultUtils.success(review);
    }

    /**
     * 获取当前用户的评价列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 分页评价列表
     */
    @Operation(summary = "获取当前用户的评价列表", description = "获取当前登录用户的评价列表")
    @GetMapping("/list/my")
    @LoginRequired
    public BaseResponse<Page<Review>> listMyReviews(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        Page<Review> page = validatePageParams(current, size);

        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);

        logOperation("获取当前用户的评价列表", request, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 管理员获取所有评价列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param productId 二手物品ID
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @param request HTTP请求
     * @return 分页评价列表
     */
    @Operation(summary = "管理员获取所有评价列表", description = "管理员获取所有评价列表")
    @GetMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Review>> adminListReviews(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "二手物品ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "订单ID") @RequestParam(required = false) Long orderId,
            @Parameter(description = "最低评分") @RequestParam(required = false) Integer minRating,
            @Parameter(description = "最高评分") @RequestParam(required = false) Integer maxRating,
            HttpServletRequest request) {
        // 参数校验
        Page<Review> page = validatePageParams(current, size);

        // 构建查询条件
        QueryWrapper<Review> queryWrapper = reviewService.getQueryWrapper(
                productId, userId, orderId, minRating, maxRating
        );

        // 执行分页查询
        Page<Review> reviewPage = reviewService.page(page, queryWrapper);
        
        logOperation("管理员获取所有评价列表", request, 
                "当前页", current,
                "每页大小", size,
                "二手物品ID", productId,
                "用户ID", userId,
                "订单ID", orderId,
                "最低评分", minRating,
                "最高评分", maxRating
        );
        return ResultUtils.success(reviewPage);
    }
}