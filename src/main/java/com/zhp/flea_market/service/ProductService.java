package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.Product;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ProductService extends IService<Product> {
    /**
     * 用户添加商品
     */
    boolean addProduct(Product product, HttpServletRequest request);
}
