package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.Product;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 添加商品
     *
     * @param product 商品信息
     * @param request HTTP请求
     * @return 是否添加成功
     */
    boolean addProduct(Product product, HttpServletRequest request);

    /**
     * 更新商品信息
     *
     * @param product 商品信息
     * @param request HTTP请求
     * @return 是否更新成功
     */
    boolean updateProduct(Product product, HttpServletRequest request);

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    boolean deleteProduct(Long id, HttpServletRequest request);

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    Product getProductDetail(Long id);

    /**
     * 分页获取商品列表
     *
     * @param page 分页参数
     * @return 商品列表
     */
    List<Product> getProductList(Page<Product> page);

    /**
     * 根据分类获取商品列表
     *
     * @param categoryId 分类ID
     * @param page 分页参数
     * @return 商品列表
     */
    List<Product> getProductsByCategory(Long categoryId, Page<Product> page);

    /**
     * 根据关键词搜索商品
     *
     * @param keyword 关键词
     * @param page 分页参数
     * @return 商品列表
     */
    List<Product> searchProducts(String keyword, Page<Product> page);

    /**
     * 高级搜索商品
     *
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param paymentMethod 支付方式
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @param page 分页参数
     * @return 商品列表
     */
    List<Product> advancedSearchProducts(String keyword, Long categoryId, BigDecimal minPrice, 
                                        BigDecimal maxPrice, Integer paymentMethod, String sortField, 
                                        String sortOrder, Page<Product> page);

    /**
     * 获取用户发布的商品列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 商品列表
     */
    List<Product> getUserProducts(Long userId, Page<Product> page);

    /**
     * 更新商品状态
     *
     * @param id 商品ID
     * @param status 商品状态
     * @param request HTTP请求
     * @return 是否更新成功
     */
    boolean updateProductStatus(Long id, Integer status, HttpServletRequest request);

    /**
     * 获取最新商品列表
     *
     * @param limit 限制数量
     * @return 最新商品列表
     */
    List<Product> getLatestProducts(int limit);

    /**
     * 获取查询条件
     *
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param status 商品状态
     * @return 查询条件
     */
    QueryWrapper<Product> getQueryWrapper(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer status);
}
