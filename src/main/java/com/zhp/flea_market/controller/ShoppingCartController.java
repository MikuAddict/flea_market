package com.zhp.flea_market.controller;

import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.model.vo.ShoppingCartVO;
import com.zhp.flea_market.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "购物车管理")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车")
    public BaseResponse<Boolean> addToCart(@RequestParam Long productId, HttpServletRequest request) {
        if (productId == null || productId <= 0) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR.getCode(), false, "商品ID无效");
        }
        
        boolean result = shoppingCartService.addToCart(productId, request);
        return ResultUtils.success(result);
    }

    /**
     * 从购物车移除商品
     */
    @DeleteMapping("/remove/{cartId}")
    @Operation(summary = "从购物车移除商品")
    public BaseResponse<Boolean> removeFromCart(@PathVariable Long cartId, HttpServletRequest request) {
        if (cartId == null || cartId <= 0) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR.getCode(), false, "购物车项ID无效");
        }
        
        boolean result = shoppingCartService.removeFromCart(cartId, request);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户购物车列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户购物车列表")
    public BaseResponse<List<ShoppingCartVO>> getUserCart(HttpServletRequest request) {
        List<ShoppingCartVO> cartList = shoppingCartService.getUserCartWithProductInfo(request);
        return ResultUtils.success(cartList);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车")
    public BaseResponse<Boolean> clearCart(HttpServletRequest request) {
        boolean result = shoppingCartService.clearCart(request);
        return ResultUtils.success(result);
    }

    /**
     * 检查商品是否在购物车中
     */
    @GetMapping("/check")
    @Operation(summary = "检查商品是否在购物车中")
    public BaseResponse<Boolean> checkProductInCart(@RequestParam Long productId, HttpServletRequest request) {
        if (productId == null || productId <= 0) {
            return new BaseResponse<>(ErrorCode.PARAMS_ERROR.getCode(), false, "商品ID无效");
        }
        
        boolean result = shoppingCartService.isProductInCart(productId, request);
        return ResultUtils.success(result);
    }

    /**
     * 校验购物车商品状态
     */
    @GetMapping("/validate")
    @Operation(summary = "校验购物车商品状态")
    public BaseResponse<Boolean> validateCartItems(HttpServletRequest request) {
        boolean result = shoppingCartService.validateCartItems(request);
        return ResultUtils.success(result);
    }
}