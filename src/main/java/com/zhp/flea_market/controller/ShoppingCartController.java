package com.zhp.flea_market.controller;

import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.model.vo.ShoppingCartVO;
import com.zhp.flea_market.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@Slf4j
@Tag(name = "购物车管理", description = "购物车的增删改查等接口")
public class ShoppingCartController extends BaseController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("")
    @Operation(summary = "添加商品到购物车", description = "用户将二手物品添加到购物车")
    @LoginRequired
    public BaseResponse<Boolean> addToCart(
            @Parameter(description = "二手物品ID") @RequestParam Long productId,
            HttpServletRequest request) {
        // 参数校验
        validateId(productId, "二手物品ID");
        
        boolean result = shoppingCartService.addToCart(productId, request);
        
        logOperation("添加商品到购物车", result, request, "二手物品ID", productId);
        return handleOperationResult(result, "商品添加成功");
    }

    /**
     * 从购物车移除商品
     */
    @DeleteMapping("/{cartId}")
    @Operation(summary = "从购物车移除商品", description = "用户从购物车中移除指定商品")
    @LoginRequired
    public BaseResponse<Boolean> removeFromCart(
            @Parameter(description = "购物车项ID") @PathVariable Long cartId,
            HttpServletRequest request) {
        // 参数校验
        validateId(cartId, "购物车项ID");
        
        boolean result = shoppingCartService.removeFromCart(cartId, request);
        
        logOperation("从购物车移除商品", result, request, "购物车项ID", cartId);
        return handleOperationResult(result, "商品移除成功");
    }

    /**
     * 获取用户购物车列表
     */
    @GetMapping("")
    @Operation(summary = "获取用户购物车列表", description = "获取当前登录用户的购物车列表")
    @LoginRequired
    public BaseResponse<List<ShoppingCartVO>> getUserCart(HttpServletRequest request) {
        List<ShoppingCartVO> cartList = shoppingCartService.getUserCartWithProductInfo(request);
        
        logOperation("获取用户购物车列表", request, "商品数量", cartList.size());
        return ResultUtils.success(cartList);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("")
    @Operation(summary = "清空购物车", description = "清空当前用户的购物车")
    @LoginRequired
    public BaseResponse<Boolean> clearCart(HttpServletRequest request) {
        boolean result = shoppingCartService.clearCart(request);
        
        logOperation("清空购物车", result, request);
        return handleOperationResult(result, "购物车清空成功");
    }

    /**
     * 检查商品是否在购物车中
     */
    @GetMapping("/check/{productId}")
    @Operation(summary = "检查商品是否在购物车中", description = "检查指定二手物品是否在当前用户的购物车中")
    @LoginRequired
    public BaseResponse<Boolean> checkProductInCart(
            @Parameter(description = "二手物品ID") @PathVariable Long productId,
            HttpServletRequest request) {
        // 参数校验
        validateId(productId, "二手物品ID");
        
        boolean result = shoppingCartService.isProductInCart(productId, request);
        
        logOperation("检查商品是否在购物车中", request, "二手物品ID", productId, "结果", result);
        return ResultUtils.success(result);
    }

    /**
     * 校验购物车商品状态
     */
    @GetMapping("/validate")
    @Operation(summary = "校验购物车商品状态", description = "校验购物车中所有商品的状态是否有效")
    @LoginRequired
    public BaseResponse<Boolean> validateCartItems(HttpServletRequest request) {
        boolean result = shoppingCartService.validateCartItems(request);
        
        logOperation("校验购物车商品状态", result, request);
        return handleOperationResult(result, "购物车商品状态校验成功");
    }
}