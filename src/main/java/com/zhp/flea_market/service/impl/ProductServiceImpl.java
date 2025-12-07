package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.ProductMapper;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.CategoryService;
import com.zhp.flea_market.service.ImageStorageService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ImageStorageService imageStorageService;

    /**
     * 添加商品
     *
     * @param product 商品信息
     * @param request HTTP请求
     * @return 是否添加成功
     */
    @Override
    public boolean addProduct(Product product, HttpServletRequest request) {
        // 参数校验
        if (product == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品信息不能为空");
        }
        
        if (StringUtils.isBlank(product.getProductName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品名称不能为空");
        }
        
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品价格必须大于0");
        }
        
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品分类不能为空");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 设置商品信息
        product.setUser(currentUser);
        product.setStatus(0); // 默认状态为待审核
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        
        // 保存商品
        return this.save(product);
    }

    /**
     * 更新商品信息
     *
     * @param product 商品信息
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @Override
    public boolean updateProduct(Product product, HttpServletRequest request) {
        // 参数校验
        if (product == null || product.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品信息不能为空");
        }
        
        // 检查商品是否存在
        Product existingProduct = this.getById(product.getId());
        if (existingProduct == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 权限校验：只有商品发布者或管理员可以修改
        if (!existingProduct.getUser().getId().equals(currentUser.getId()) && 
            !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改该商品");
        }
        
        // 设置更新时间
        product.setUpdateTime(new Date());
        
        return this.updateById(product);
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Override
    public boolean deleteProduct(Long id, HttpServletRequest request) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        // 检查商品是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 权限校验：只有商品发布者或管理员可以删除
        if (!product.getUser().getId().equals(currentUser.getId()) && 
            !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该商品");
        }
        
        boolean result = this.removeById(id);
        
        // 如果删除成功且商品有图片，删除相关图片
        if (result && product.getImageUrl() != null) {
            try {
                imageStorageService.deleteImage(product.getImageUrl());
            } catch (Exception e) {
                // 删除图片失败不应该影响删除操作
                System.err.println("删除商品图片失败: " + e.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @Override
    public Product getProductDetail(Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        
        return product;
    }

    /**
     * 分页获取商品列表
     *
     * @param page 分页参数
     * @return 商品列表
     */
    @Override
    public List<Product> getProductList(Page<Product> page) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        // 只查询已上架的商品
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("create_time");
        
        Page<Product> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 根据分类获取商品列表
     *
     * @param categoryId 分类ID
     * @param page 分页参数
     * @return 商品列表
     */
    @Override
    public List<Product> getProductsByCategory(Long categoryId, Page<Product> page) {
        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类ID无效");
        }
        
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId);
        queryWrapper.eq("status", 1); // 只查询已上架的商品
        queryWrapper.orderByDesc("create_time");
        
        Page<Product> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 根据关键词搜索商品
     *
     * @param keyword 关键词
     * @param page 分页参数
     * @return 商品列表
     */
    @Override
    public List<Product> searchProducts(String keyword, Page<Product> page) {
        if (StringUtils.isBlank(keyword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索关键词不能为空");
        }
        
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("product_name", keyword)
                   .or()
                   .like("description", keyword);
        queryWrapper.eq("status", 1); // 只查询已上架的商品
        queryWrapper.orderByDesc("create_time");
        
        Page<Product> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

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
    @Override
    public List<Product> advancedSearchProducts(String keyword, Long categoryId, BigDecimal minPrice, 
                                               BigDecimal maxPrice, Integer paymentMethod, String sortField, 
                                               String sortOrder, Page<Product> page) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索（支持商品名称和描述）
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like("product_name", keyword)
                .or()
                .like("description", keyword)
            );
        }
        
        // 分类筛选
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq("category_id", categoryId);
        }
        
        // 价格范围筛选
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
            queryWrapper.ge("price", minPrice);
        }
        
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) >= 0) {
            queryWrapper.le("price", maxPrice);
        }
        
        // 支付方式筛选
        if (paymentMethod != null && paymentMethod >= 0 && paymentMethod <= 3) {
            queryWrapper.eq("payment_method", paymentMethod);
        }
        
        // 只查询已上架的商品
        queryWrapper.eq("status", 1);
        
        // 排序处理
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField.toLowerCase()) {
                case "price":
                    queryWrapper.orderBy(true, isAsc, "price");
                    break;
                case "createtime":
                    queryWrapper.orderBy(true, isAsc, "create_time");
                    break;
                case "name":
                    queryWrapper.orderBy(true, isAsc, "product_name");
                    break;
                default:
                    queryWrapper.orderByDesc("create_time");
                    break;
            }
        } else {
            queryWrapper.orderByDesc("create_time");
        }
        
        Page<Product> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 获取用户发布的商品列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 商品列表
     */
    @Override
    public List<Product> getUserProducts(Long userId, Page<Product> page) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }
        
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        
        Page<Product> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 更新商品状态
     *
     * @param id 商品ID
     * @param status 商品状态
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @Override
    public boolean updateProductStatus(Long id, Integer status, HttpServletRequest request) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品ID无效");
        }
        
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品状态无效");
        }
        
        // 检查商品是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 权限校验：只有商品发布者或管理员可以修改状态
        if (!product.getUser().getId().equals(currentUser.getId()) && 
            !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改商品状态");
        }
        
        // 更新商品状态
        Product updateProduct = new Product();
        updateProduct.setId(id);
        updateProduct.setStatus(status);
        updateProduct.setUpdateTime(new Date());
        
        return this.updateById(updateProduct);
    }

    /**
     * 获取最新商品列表
     *
     * @param limit 限制数量
     * @return 最新商品列表
     */
    @Override
    public List<Product> getLatestProducts(int limit) {
        if (limit <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "限制数量必须大于0");
        }
        
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 只查询已上架的商品
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT " + limit);
        
        return this.list(queryWrapper);
    }

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
    @Override
    public QueryWrapper<Product> getQueryWrapper(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer status) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like("product_name", keyword)
                       .or()
                       .like("description", keyword);
        }
        
        // 分类筛选
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq("category_id", categoryId);
        }
        
        // 价格范围筛选
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
            queryWrapper.ge("price", minPrice);
        }
        
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) >= 0) {
            queryWrapper.le("price", maxPrice);
        }
        
        // 状态筛选
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return queryWrapper;
    }
}
