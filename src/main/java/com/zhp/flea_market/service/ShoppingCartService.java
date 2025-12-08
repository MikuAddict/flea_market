package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.ShoppingCart;
import com.zhp.flea_market.model.vo.ShoppingCartVO;
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
     * 获取用户购物车列表
     *
     * @param request HTTP请求
     * @return 购物车列表
     */
    List<ShoppingCart> getUserCart(HttpServletRequest request);

    /**
     * 获取用户购物车列表（包含商品信息）
     *
     * @param request HTTP请求
     * @return 购物车视图对象列表
     */
    List<ShoppingCartVO> getUserCartWithProductInfo(HttpServletRequest request);

    /**
     * 检查二手物品是否已在购物车中
     *
     * @param productId 二手物品ID
     * @param request HTTP请求
     * @return 是否在购物车中
     */
    boolean isProductInCart(Long productId, HttpServletRequest request);

    /**
     * 清空购物车
     *
     * @param request HTTP请求
     * @return 是否清空成功
     */
    boolean clearCart(HttpServletRequest request);

    /**
     * 校验购物车商品状态
     *
     * @param request HTTP请求
     * @return 是否所有商品状态正常
     */
    boolean validateCartItems(HttpServletRequest request);

    /**
     * 校验单个商品状态
     *
     * @param productId 商品ID
     * @return 商品是否可购买
     */
    boolean validateProductStatus(Long productId);
}