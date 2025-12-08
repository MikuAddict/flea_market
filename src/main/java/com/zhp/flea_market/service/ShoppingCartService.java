package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.ShoppingCart;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 添加二手物品到购物车
     *
     * @param productId 二手物品ID
     * @param request HTTP请求
     * @return 是否添加成功
     */
    boolean addToCart(Long productId, HttpServletRequest request);

    /**
     * 从购物车删除二手物品
     *
     * @param cartId 购物车项ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    boolean removeFromCart(Long cartId, HttpServletRequest request);

    /**
     * 清空用户购物车
     *
     * @param request HTTP请求
     * @return 是否清空成功
     */
    boolean clearCart(HttpServletRequest request);

    /**
     * 获取用户购物车列表
     *
     * @param request HTTP请求
     * @return 购物车列表
     */
    List<ShoppingCart> getUserCart(HttpServletRequest request);

    /**
     * 获取购物车二手物品总数
     *
     * @param request HTTP请求
     * @return 二手物品总数
     */
    int getCartItemCount(HttpServletRequest request);

    /**
     * 获取购物车总金额
     *
     * @param request HTTP请求
     * @return 总金额
     */
    double getCartTotalAmount(HttpServletRequest request);

    /**
     * 批量删除购物车二手物品
     *
     * @param cartIds 购物车项ID列表
     * @param request HTTP请求
     * @return 是否删除成功
     */
    boolean batchRemoveFromCart(List<Long> cartIds, HttpServletRequest request);

    /**
     * 检查二手物品是否已在购物车中
     *
     * @param productId 二手物品ID
     * @param request HTTP请求
     * @return 是否在购物车中
     */
    boolean isProductInCart(Long productId, HttpServletRequest request);

    /**
     * 根据二手物品ID获取购物车项
     *
     * @param productId 二手物品ID
     * @param request HTTP请求
     * @return 购物车项
     */
    ShoppingCart getCartItemByProductId(Long productId, HttpServletRequest request);
}