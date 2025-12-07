package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.ShoppingCartMapper;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.ShoppingCart;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.ShoppingCartService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * 添加商品到购物车
     *
     * @param productId 商品ID
     * @param request HTTP请求
     * @return 是否添加成功
     */
    @Override
    @Transactional
    public boolean addToCart(Long productId, HttpServletRequest request) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查商品是否存在且已上架
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        
        if (product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品未上架，无法加入购物车");
        }
        
        // 检查商品是否已在购物车中
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        queryWrapper.eq("product_id", productId);
        ShoppingCart existingCartItem = this.getOne(queryWrapper);
        
        if (existingCartItem != null) {
            // 如果商品已在购物车中，直接返回成功（单件模式）
            return true;
        } else {
            // 创建新的购物车项
            ShoppingCart cartItem = new ShoppingCart();
            cartItem.setUser(currentUser);
            cartItem.setProduct(product);
            cartItem.setCreateTime(new Date());
            
            return this.save(cartItem);
        }
    }

    /**
     * 从购物车删除商品
     *
     * @param cartId 购物车项ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean removeFromCart(Long cartId, HttpServletRequest request) {
        // 参数校验
        if (cartId == null || cartId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "购物车项ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查购物车项是否存在
        ShoppingCart cartItem = this.getById(cartId);
        if (cartItem == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "购物车项不存在");
        }
        
        // 权限校验：只能删除自己的购物车项
        if (cartItem.getUser() == null || !cartItem.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该购物车项");
        }
        
        return this.removeById(cartId);
    }

    /**
     * 清空用户购物车
     *
     * @param request HTTP请求
     * @return 是否清空成功
     */
    @Override
    @Transactional
    public boolean clearCart(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        
        return this.remove(queryWrapper);
    }

    /**
     * 获取用户购物车列表
     *
     * @param request HTTP请求
     * @return 购物车列表
     */
    @Override
    public List<ShoppingCart> getUserCart(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        queryWrapper.orderByDesc("create_time");
        
        return this.list(queryWrapper);
    }

    /**
     * 获取购物车商品总数
     *
     * @param request HTTP请求
     * @return 商品总数
     */
    @Override
    public int getCartItemCount(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        
        return (int) this.count(queryWrapper);
    }

    /**
     * 获取购物车总金额
     *
     * @param request HTTP请求
     * @return 总金额
     */
    @Override
    public double getCartTotalAmount(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 直接查询购物车项，避免自调用getUserCart方法
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        queryWrapper.orderByDesc("create_time");
        List<ShoppingCart> cartItems = this.list(queryWrapper);
        
        if (CollectionUtils.isEmpty(cartItems)) {
            return 0.0;
        }
        
        double totalAmount = 0.0;
        for (ShoppingCart cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product != null && product.getStatus() == 1) {
                totalAmount += product.getPrice().doubleValue();
            }
        }
        
        return totalAmount;
    }

    /**
     * 批量删除购物车商品
     *
     * @param cartIds 购物车项ID列表
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean batchRemoveFromCart(List<Long> cartIds, HttpServletRequest request) {
        // 参数校验
        if (CollectionUtils.isEmpty(cartIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "购物车项ID列表不能为空");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查所有购物车项是否都属于当前用户
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        queryWrapper.in("id", cartIds);
        
        List<ShoppingCart> userCartItems = this.list(queryWrapper);
        if (userCartItems.size() != cartIds.size()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "部分购物车项不属于当前用户");
        }
        
        return this.removeByIds(cartIds);
    }

    /**
     * 检查商品是否已在购物车中
     *
     * @param productId 商品ID
     * @param request HTTP请求
     * @return 是否在购物车中
     */
    @Override
    public boolean isProductInCart(Long productId, HttpServletRequest request) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        queryWrapper.eq("product_id", productId);
        
        return this.count(queryWrapper) > 0;
    }

    /**
     * 根据商品ID获取购物车项
     *
     * @param productId 商品ID
     * @param request HTTP请求
     * @return 购物车项
     */
    @Override
    public ShoppingCart getCartItemByProductId(Long productId, HttpServletRequest request) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        queryWrapper.eq("product_id", productId);
        
        return this.getOne(queryWrapper);
    }
}