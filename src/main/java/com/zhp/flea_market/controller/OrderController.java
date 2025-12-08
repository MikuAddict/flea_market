package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.model.dto.request.OrderRequest;
import com.zhp.flea_market.model.dto.request.PaymentProofRequest;
import com.zhp.flea_market.model.dto.request.OrderConfirmRequest;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.vo.OrderVO;
import com.zhp.flea_market.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/order")
@Slf4j
@Tag(name = "订单管理", description = "订单的创建、支付、取消、完成等接口")
public class OrderController extends BaseController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单
     *
     * @param productId 二手物品ID
     * @param request HTTP请求
     * @return 订单ID
     */
    @Operation(summary = "创建订单", description = "用户创建新的订单，自动使用二手物品设置的支付方式")
    @PostMapping("/create")
    @LoginRequired
    public BaseResponse<Long> createOrder(
            @Parameter(description = "二手物品ID") @RequestParam Long productId,
            HttpServletRequest request) {
        // 参数校验
        validateId(productId, "二手物品ID");

        // 创建订单（自动使用二手物品设置的支付方式）
        Long orderId = orderService.createOrder(productId, request);
        
        logOperation("创建订单", true, request, 
                "二手物品ID", productId,
                "订单ID", orderId
        );
        return handleOperationResult(true, "订单创建成功", orderId);
    }

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否支付成功
     */
    @Operation(summary = "支付订单", description = "用户支付订单")
    @PostMapping("/pay/{orderId}")
    @LoginRequired
    public BaseResponse<Boolean> payOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 支付订单
        boolean result = orderService.payOrder(orderId, request);
        
        logOperation("支付订单", result, request, "订单ID", orderId);
        return handleOperationResult(result, "订单支付成功");
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否取消成功
     */
    @Operation(summary = "取消订单", description = "用户取消订单")
    @PostMapping("/cancel/{orderId}")
    @LoginRequired
    public BaseResponse<Boolean> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 取消订单
        boolean result = orderService.cancelOrder(orderId, request);
        
        logOperation("取消订单", result, request, "订单ID", orderId);
        return handleOperationResult(result, "订单取消成功");
    }

    /**
     * 完成订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否完成成功
     */
    @Operation(summary = "完成订单", description = "卖家或买家确认订单完成")
    @PostMapping("/complete/{orderId}")
    @LoginRequired
    public BaseResponse<Boolean> completeOrder(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 完成订单
        boolean result = orderService.completeOrder(orderId, request);
        
        logOperation("完成订单", result, request, "订单ID", orderId);
        return handleOperationResult(result, "订单完成成功");
    }

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 订单详情
     */
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    @GetMapping("/get/{orderId}")
    @LoginRequired
    public BaseResponse<OrderVO> getOrderById(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 获取订单信息
        OrderVO order = orderService.getOrderDetail(orderId, request);
        validateResourceExists(order, "订单");

        logOperation("获取订单详情", request, "订单ID", orderId);
        return ResultUtils.success(order);
    }

    /**
     * 获取买家订单列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 分页订单列表
     */
    @Operation(summary = "获取买家订单列表", description = "获取当前登录用户的买家订单列表")
    @GetMapping("/list/buyer")
    @LoginRequired
    public BaseResponse<Page<Order>> listBuyerOrders(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        Page<Order> page = validatePageParams(current, size);

        // 执行分页查询
        List<Order> orderList = orderService.getBuyerOrders(request, page);
        
        logOperation("获取买家订单列表", request, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 获取卖家订单列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 分页订单列表
     */
    @Operation(summary = "获取卖家订单列表", description = "获取当前登录用户的卖家订单列表")
    @GetMapping("/list/seller")
    @LoginRequired
    public BaseResponse<Page<Order>> listSellerOrders(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        Page<Order> page = validatePageParams(current, size);

        // 执行分页查询
        List<Order> orderList = orderService.getSellerOrders(request, page);
        
        logOperation("获取卖家订单列表", request, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 获取订单统计信息
     *
     * @param request HTTP请求
     * @return 订单统计信息
     */
    @Operation(summary = "获取订单统计信息", description = "获取当前用户的订单统计信息")
    @GetMapping("/statistics")
    @LoginRequired
    public BaseResponse<OrderRequest> getOrderStatistics(HttpServletRequest request) {
        // 获取订单统计信息
        OrderRequest statistics = orderService.getOrderStatistics(request);
        
        logOperation("获取订单统计信息", request);
        return ResultUtils.success(statistics);
    }

    /**
     * 获取所有订单（管理员权限）
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param status 订单状态
     * @param request HTTP请求
     * @return 分页订单列表
     */
    @Operation(summary = "获取所有订单", description = "管理员获取所有订单列表")
    @GetMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Order>> adminListOrders(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        // 参数校验
        Page<Order> page = validatePageParams(current, size);

        // 执行分页查询
        List<Order> orderList = orderService.getAllOrders(page, status);
        
        logOperation("管理员获取所有订单列表", request, 
                "当前页", current,
                "每页大小", size,
                "状态", status
        );
        return ResultUtils.success(page);
    }

    /**
     * 提交支付凭证
     *
     * @param proofRequest 支付凭证请求
     * @param request HTTP请求
     * @return 是否提交成功
     */
    @Operation(summary = "提交支付凭证", description = "买家上传现金支付凭证")
    @PostMapping("/submit/proof")
    @LoginRequired
    public BaseResponse<Boolean> submitPaymentProof(
            @Parameter(description = "支付凭证请求") @RequestBody PaymentProofRequest proofRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(proofRequest, "支付凭证请求");
        
        // 提交支付凭证
        boolean result = orderService.submitPaymentProof(proofRequest, request);
        
        logOperation("提交支付凭证", result, request, "订单ID", proofRequest.getOrderId());
        return handleOperationResult(result, "支付凭证提交成功");
    }

    /**
     * 确认订单（买家确认收货）
     *
     * @param confirmRequest 订单确认请求
     * @param request HTTP请求
     * @return 是否确认成功
     */
    @Operation(summary = "确认订单", description = "买家确认收货")
    @PostMapping("/confirm")
    @LoginRequired
    public BaseResponse<Boolean> confirmOrder(
            @Parameter(description = "订单确认请求") @RequestBody OrderConfirmRequest confirmRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(confirmRequest, "订单确认请求");
        
        // 确认订单
        boolean result = orderService.confirmOrder(confirmRequest, request);
        
        logOperation("买家确认收货", result, request, "订单ID", confirmRequest.getOrderId());
        return handleOperationResult(result, "确认收货成功");
    }

    /**
     * 模拟微信支付
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否支付成功
     */
    @Operation(summary = "模拟微信支付", description = "模拟微信支付流程")
    @PostMapping("/pay/wechat/{orderId}")
    @LoginRequired
    public BaseResponse<Boolean> simulateWechatPay(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 模拟微信支付
        boolean result = orderService.simulateWechatPay(orderId, request);
        
        logOperation("微信支付", result, request, "订单ID", orderId);
        return handleOperationResult(result, "微信支付成功");
    }

    /**
     * 使用积分兑换二手物品
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否兑换成功
     */
    @Operation(summary = "积分兑换二手物品", description = "使用积分兑换二手物品")
    @PostMapping("/pay/points/{orderId}")
    @LoginRequired
    public BaseResponse<Boolean> exchangeWithPoints(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 积分兑换
        boolean result = orderService.exchangeWithPoints(orderId, request);
        
        logOperation("积分兑换", result, request, "订单ID", orderId);
        return handleOperationResult(result, "积分兑换成功");
    }

    /**
     * 申请物品交换
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否申请成功
     */
    @Operation(summary = "申请物品交换", description = "买家申请物品交换")
    @PostMapping("/exchange/apply/{orderId}")
    @LoginRequired
    public BaseResponse<Boolean> applyForExchange(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 申请物品交换
        boolean result = orderService.applyForExchange(orderId, request);
        
        logOperation("申请物品交换", result, request, "订单ID", orderId);
        return handleOperationResult(result, "物品交换申请成功，等待卖家确认");
    }

    /**
     * 确认物品交换
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否确认成功
     */
    @Operation(summary = "确认物品交换", description = "卖家确认物品交换")
    @PostMapping("/exchange/confirm/{orderId}")
    @LoginRequired
    public BaseResponse<Boolean> confirmExchange(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            HttpServletRequest request) {
        // 参数校验
        validateId(orderId, "订单ID");

        // 确认物品交换
        boolean result = orderService.confirmExchange(orderId, request);
        
        logOperation("确认物品交换", result, request, "订单ID", orderId);
        return handleOperationResult(result, "物品交换确认成功");
    }


}