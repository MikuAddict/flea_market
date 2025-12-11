package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.vo.ProductVO;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 二手物品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 添加二手物品
     *
     * @param product 二手物品信息
     * @param request HTTP请求
     * @return 是否添加成功
     */
    boolean addProduct(Product product, HttpServletRequest request);

    /**
     * 更新二手物品信息
     *
     * @param product 二手物品信息
     * @param request HTTP请求
     * @return 是否更新成功
     */
    boolean updateProduct(Product product, HttpServletRequest request);

    /**
     * 删除二手物品
     *
     * @param id 二手物品ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    boolean deleteProduct(Long id, HttpServletRequest request);

    /**
     * 获取二手物品详情
     *
     * @param id 二手物品ID
     * @return 二手物品详情
     */
    Product getProductDetail(Long id);

    /**
     * 获取二手物品详情（用于前端展示）
     *
     * @param id 二手物品ID
     * @return 二手物品详情
     */
    ProductVO getProductDetailVO(Long id);

    /**
     * 高级搜索二手物品
     *
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param paymentMethod 支付方式
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     * @param page 分页参数
     * @return 二手物品列表
     */
    List<Product> advancedSearchProducts(String keyword, Long categoryId, BigDecimal minPrice, 
                                        BigDecimal maxPrice, Integer paymentMethod, String sortField, 
                                        String sortOrder, Page<Product> page);

    /**
     * 获取用户发布的二手物品列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 二手物品列表
     */
    List<Product> getUserProducts(Long userId, Page<Product> page);

    /**
     * 更新二手物品状态
     *
     * @param id 二手物品ID
     * @param status 二手物品状态
     * @param request HTTP请求
     * @return 是否更新成功
     */
    boolean updateProductStatus(Long id, Integer status, HttpServletRequest request);

    /**
     * 标记二手物品为已售出（仅限订单完成时调用）
     *
     * @param id 二手物品ID
     * @return 是否更新成功
     */
    boolean markProductAsSold(Long id);

    /**
     * 获取最新二手物品列表
     *
     * @param limit 限制数量
     * @return 最新二手物品列表
     */
    List<Product> getLatestProducts(int limit);

    /**
     * 获取查询条件
     *
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param status 二手物品状态
     * @return 查询条件
     */
    QueryWrapper<Product> getQueryWrapper(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer status);

    /**
     * 处理商品图片信息
     *
     * @param product 商品信息
     */
    void processProductImages(Product product);

    /**
     * 解析JSON格式的图片URL列表
     *
     * @param imageUrlsJson JSON格式的图片URL列表
     * @return 图片URL列表
     */
    List<String> parseImageUrls(String imageUrlsJson);

    /**
     * 将Product实体列表转换为ProductVO列表
     *
     * @param productList 商品实体列表
     * @return 商品视图对象列表
     */
    List<ProductVO> convertToProductVOList(List<Product> productList);
}
