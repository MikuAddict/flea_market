package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.ProductMapper;
import com.zhp.flea_market.model.entity.Category;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.ProductVO;
import com.zhp.flea_market.service.CategoryService;
import com.zhp.flea_market.service.ImageStorageService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 添加二手物品
     *
     * @param product 二手物品信息
     * @param request HTTP请求
     * @return 是否添加成功
     */
    @Override
    public boolean addProduct(Product product, HttpServletRequest request) {
        // 参数校验
        if (product == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品信息不能为空");
        }
        
        if (StringUtils.isBlank(product.getProductName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品名称不能为空");
        }
        
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品价格必须大于0");
        }
        
        if (product.getCategoryId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品分类不能为空");
        }
        
        // 检查二手物品是否已设置用户ID
        if (product.getUserId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 处理图片信息：设置主图并保存所有图片URL
        processProductImages(product);
        
        // 设置二手物品信息
        product.setStatus(0); // 默认状态为待审核
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        
        // 保存二手物品
        return this.save(product);
    }

    /**
     * 更新二手物品信息
     *
     * @param product 二手物品信息
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @Override
    public boolean updateProduct(Product product, HttpServletRequest request) {
        // 参数校验
        if (product == null || product.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品信息不能为空");
        }
        
        // 检查二手物品是否存在
        Product existingProduct = this.getById(product.getId());
        if (existingProduct == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "二手物品不存在");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 权限校验：只有二手物品发布者或管理员可以修改
        if (!existingProduct.getUserId().equals(currentUser.getId()) && 
            !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改该二手物品");
        }
        
        // 处理图片信息：设置主图并保存所有图片URL
        processProductImages(product);
        
        // 设置更新时间
        product.setUpdateTime(new Date());
        
        // 创建更新对象，禁止通过此方法修改status字段
        Product updateProduct = new Product();
        updateProduct.setId(product.getId());
        updateProduct.setProductName(product.getProductName());
        updateProduct.setDescription(product.getDescription());
        updateProduct.setPrice(product.getPrice());
        updateProduct.setMainImageUrl(product.getMainImageUrl());
        updateProduct.setImageUrls(product.getImageUrls());
        updateProduct.setPaymentMethod(product.getPaymentMethod());
        updateProduct.setCategoryId(product.getCategoryId());
        updateProduct.setUpdateTime(product.getUpdateTime());
        // 注意：不设置status字段，防止通过普通更新方法修改状态
        
        return this.updateById(updateProduct);
    }

    /**
     * 删除二手物品
     *
     * @param id 二手物品ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Override
    public boolean deleteProduct(Long id, HttpServletRequest request) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID无效");
        }
        
        // 检查二手物品是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "二手物品不存在");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 权限校验：只有二手物品发布者或管理员可以删除
        if (!product.getUserId().equals(currentUser.getId()) && 
            !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该二手物品");
        }
        
        boolean result = this.removeById(id);
        
        // 如果删除成功且二手物品有图片，删除相关图片
        if (result && product.getImageUrls() != null) {
            try {
                // 删除所有相关图片
                List<String> imageUrls = parseImageUrls(product.getImageUrls());
                for (String imageUrl : imageUrls) {
                    imageStorageService.deleteImage(imageUrl);
                }
            } catch (Exception e) {
                // 删除图片失败不应该影响删除操作
                System.err.println("删除二手物品图片失败: " + e.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 获取二手物品详情
     *
     * @param id 二手物品ID
     * @return 二手物品详情
     */
    @Override
    public Product getProductDetail(Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID无效");
        }
        
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "二手物品不存在");
        }
        
        return product;
    }

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
    @Override
    public List<Product> advancedSearchProducts(String keyword, Long categoryId, BigDecimal minPrice, 
                                               BigDecimal maxPrice, Integer paymentMethod, String sortField, 
                                               String sortOrder, Page<Product> page) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        
        // 关键词搜索（支持二手物品名称和描述）
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
        
        // 只查询已通过的二手物品
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
     * 获取用户发布的二手物品列表
     *
     * @param userId 用户ID
     * @param page 分页参数
     * @return 二手物品列表
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
     * 更新二手物品状态
     *
     * @param id 二手物品ID
     * @param status 二手物品状态
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @Override
    public boolean updateProductStatus(Long id, Integer status, HttpServletRequest request) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID无效");
        }
        
        if (status == null || (status != 1 && status != 2)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品状态只能设置为1(已通过)或2(已拒绝)");
        }
        
        // 检查二手物品是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "二手物品不存在");
        }
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 权限校验：只有二手物品发布者或管理员可以修改状态
        if (!product.getUserId().equals(currentUser.getId()) && 
            !userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改二手物品状态");
        }
        
        // 更新二手物品状态
        Product updateProduct = new Product();
        updateProduct.setId(id);
        updateProduct.setStatus(status);
        updateProduct.setUpdateTime(new Date());
        
        return this.updateById(updateProduct);
    }

    /**
     * 标记二手物品为已售出（仅限订单完成时调用）
     *
     * @param id 二手物品ID
     * @return 是否更新成功
     */
    @Override
    public boolean markProductAsSold(Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID无效");
        }
        
        // 检查二手物品是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "二手物品不存在");
        }
        
        // 业务校验：只有已通过的二手物品才能标记为已售出
        if (product.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只有已通过的二手物品才能标记为已售出");
        }
        
        // 更新二手物品状态为已售出
        Product updateProduct = new Product();
        updateProduct.setId(id);
        updateProduct.setStatus(3); // 已售出
        updateProduct.setUpdateTime(new Date());
        
        return this.updateById(updateProduct);
    }

    /**
     * 获取最新二手物品列表
     *
     * @param limit 限制数量
     * @return 最新二手物品列表
     */
    @Override
    public List<Product> getLatestProducts(int limit) {
        // 参数校验
        if (limit <= 0 || limit > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "限制数量必须在1-50之间");
        }
        
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        // 只查询已通过审核的二手物品
        queryWrapper.eq("status", 1);
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
     * @param status 二手物品状态
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

    /**
     * 获取二手物品详情（用于前端展示）
     *
     * @param id 二手物品ID
     * @return 二手物品详情
     */
    @Override
    public ProductVO getProductDetailVO(Long id) {
        // 获取二手物品实体
        Product product = getProductDetail(id);
        if (product == null) {
            return null;
        }
        
        return convertToProductVO(product);
    }

    /**
     * 处理商品图片信息
     *
     * @param product 商品信息
     */
    @Override
    public void processProductImages(Product product) {
        if (product.getImageUrls() != null) {
            try {
                // 解析图片URL列表
                List<String> imageUrls = parseImageUrls(product.getImageUrls());

                // 设置第一张图片为主图
                if (!imageUrls.isEmpty()) {
                    product.setMainImageUrl(imageUrls.get(0));
                }

                // 将图片列表转换为JSON字符串保存
                String imageUrlsJson = objectMapper.writeValueAsString(imageUrls);
                product.setImageUrls(imageUrlsJson);

            } catch (Exception e) {
                log.error("处理商品图片信息失败: {}", e.getMessage());
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片信息格式错误");
            }
        }
    }

    /**
     * 解析JSON格式的图片URL列表
     *
     * @param imageUrlsJson JSON格式的图片URL列表
     * @return 图片URL列表
     */
    @Override
    public List<String> parseImageUrls(String imageUrlsJson) {
        try {
            if (imageUrlsJson == null || imageUrlsJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(imageUrlsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("解析图片URL列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 将Product实体转换为ProductVO
     */
    private ProductVO convertToProductVO(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(product, productVO);
        
        // 设置分类名称
        if (product.getCategoryId() != null) {
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                productVO.setCategoryName(category.getName());
            }
        }
        
        // 设置用户信息
        if (product.getUserId() != null) {
            User user = userService.getById(product.getUserId());
            if (user != null) {
                productVO.setUserName(user.getUserName());
                productVO.setUserAvatar(user.getUserAvatar());
            }
        }
        
        // 解析图片URL列表
        if (product.getImageUrls() != null) {
            try {
                List<String> imageUrls = parseImageUrls(product.getImageUrls());
                productVO.setImageUrls(imageUrls);
            } catch (Exception e) {
                log.error("解析图片URL列表失败: {}", e.getMessage());
                productVO.setImageUrls(new ArrayList<>());
            }
        }
        
        return productVO;
    }

}
