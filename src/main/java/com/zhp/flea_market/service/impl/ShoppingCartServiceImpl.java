package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.ShoppingCartMapper;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.ShoppingCart;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.ShoppingCartVO;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.ShoppingCartService;
import com.zhp.flea_market.service.UserService;
import com.zhp.flea_market.utils.PageUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * 添加二手物品到购物车
     *
     * @param productId 二手物品ID
     * @param request HTTP请求
     * @return 是否添加成功
     */
    @Override
    @Transactional
    public boolean addToCart(Long productId, HttpServletRequest request) {
        // 参数校验
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID无效");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 检查二手物品是否存在且已上架
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "二手物品不存在");
        }
        
        if (product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品未上架，无法加入购物车");
        }
        
        // 检查用户不能添加自己发布的商品到购物车
        if (product.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能添加自己发布的商品到购物车");
        }
        
        // 检查二手物品是否已在购物车中
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        queryWrapper.eq("product_id", productId);
        ShoppingCart existingCartItem = this.getOne(queryWrapper);
        
        if (existingCartItem != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该物品已添加到购物车");
        } else {
            // 创建新的购物车项
            ShoppingCart cartItem = new ShoppingCart();
            cartItem.setUserId(currentUser.getId());
            cartItem.setProductId(productId);

            return this.save(cartItem);
        }
    }

    /**
     * 从购物车删除二手物品
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
        if (!cartItem.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该购物车项");
        }
        
        return this.removeById(cartId);
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
        
        return PageUtils.getPageResult(this, new Page<>(), queryWrapper -> {
            queryWrapper.eq("user_id", currentUser.getId());
            queryWrapper.orderByDesc("create_time");
        });
    }

    /**
     * 获取用户购物车列表（包含商品信息）
     *
     * @param request HTTP请求
     * @return 购物车视图对象列表
     */
    @Override
    public List<ShoppingCartVO> getUserCartWithProductInfo(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        // 获取购物车列表
        List<ShoppingCart> cartList = this.getUserCart(request);
        if (CollectionUtils.isEmpty(cartList)) {
            return new ArrayList<>();
        }

        // 转换为视图对象
        return cartList.stream().map(cartItem -> {
            ShoppingCartVO cartVO = new ShoppingCartVO();
            BeanUtils.copyProperties(cartItem, cartVO);
            
            // 获取商品信息
            Product product = productService.getById(cartItem.getProductId());
            if (product != null) {
                cartVO.setProductName(product.getProductName());
                cartVO.setMainImageUrl(product.getMainImageUrl());
                cartVO.setPrice(product.getPrice());
                cartVO.setDescription(product.getDescription());
                cartVO.setPaymentMethod(product.getPaymentMethod());
                cartVO.setProductStatus(product.getStatus());
                
                // 获取卖家信息
                User seller = userService.getById(product.getUserId());
                if (seller != null) {
                    cartVO.setSellerName(seller.getUserName());
                    cartVO.setSellerAvatar(seller.getUserAvatar());
                }
            }
            
            return cartVO;
        }).collect(Collectors.toList());
    }

    /**
     * 校验购物车商品状态
     *
     * @param request HTTP请求
     * @return 是否所有商品状态正常
     */
    @Override
    public boolean validateCartItems(HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        // 获取购物车列表
        List<ShoppingCart> cartList = this.getUserCart(request);
        if (CollectionUtils.isEmpty(cartList)) {
            return true; // 空购物车视为正常
        }

        // 校验每个商品状态
        for (ShoppingCart cartItem : cartList) {
            if (!validateProductStatus(cartItem.getProductId())) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 校验单个商品状态
     *
     * @param productId 商品ID
     * @return 商品是否可购买
     */
    @Override
    public boolean validateProductStatus(Long productId) {
        if (productId == null || productId <= 0) {
            return false;
        }

        // 获取商品信息
        Product product = productService.getById(productId);
        if (product == null) {
            return false; // 商品不存在
        }

        // 检查商品状态：已售出(3)、已拒绝(2)、待审核(0)的商品不可购买
        if (product.getStatus() == 3) {
            // 商品已售出
            return false;
        } else if (product.getStatus() == 2) {
            // 商品审核未通过
            return false;
        } else if (product.getStatus() == 0) {
            // 商品待审核
            return false;
        }

        // 检查商品是否被逻辑删除
        if (product.getDeleted() != null && product.getDeleted() == 1) {
            return false; // 商品已被删除
        }

        // 只有状态为1（已通过）的商品可购买
        return product.getStatus() == 1;
    }

    /**
     * 检查商品是否在购物车中
     *
     * @param productId 商品ID
     * @param request HTTP请求
     * @return 是否在购物车中
     */
    @Override
    public boolean isProductInCart(Long productId, HttpServletRequest request) {
        // 参数校验
        if (productId == null || productId <= 0) {
            return false;
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            return false; // 未登录用户视为商品不在购物车
        }
        
        // 查询购物车中是否存在该商品
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        queryWrapper.eq("product_id", productId);
        ShoppingCart cartItem = this.getOne(queryWrapper);
        
        return cartItem != null;
    }

}