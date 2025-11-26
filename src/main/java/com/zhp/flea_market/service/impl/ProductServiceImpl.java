package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.mapper.ProductMapper;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    @Override
    public boolean addProduct(Product product, HttpServletRequest request) {
        return false;
    }
}
