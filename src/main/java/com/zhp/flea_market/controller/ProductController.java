package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.model.dto.request.DeleteRequest;
import com.zhp.flea_market.model.dto.request.ProductAddRequest;
import com.zhp.flea_market.model.dto.request.ProductUpdateRequest;
import com.zhp.flea_market.model.entity.Category;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.CategoryService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品接口
 */
@RestController
@RequestMapping("/product")
@Slf4j
@Tag(name = "商品管理", description = "商品的增删改查、搜索等接口")
public class ProductController extends BaseController {

    @Resource
    private ProductService productService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private UserService userService;

    /**
     * 添加商品
     *
     * @param productAddRequest 商品添加信息
     * @param request HTTP请求
     * @return 新增商品的ID
     */
    @Operation(summary = "添加商品", description = "用户添加新的商品")
    @PostMapping("/add")
    @LoginRequired
    public BaseResponse<Long> addProduct(
            @Parameter(description = "商品添加信息") @RequestBody ProductAddRequest productAddRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(productAddRequest, "商品信息");
        validateNotBlank(productAddRequest.getProductName(), "商品名称");
        validateNotNull(productAddRequest.getPrice(), "商品价格");
        validateId(productAddRequest.getCategoryId(), "分类ID");
        validateNotNull(productAddRequest.getPaymentMethod(), "支付方式");
        if (productAddRequest.getPaymentMethod() < 0 || productAddRequest.getPaymentMethod() > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付方式无效，请选择：0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换");
        }
        validateNotNull(productAddRequest.getPaymentMethod(), "支付方式");
        if (productAddRequest.getPaymentMethod() < 0 || productAddRequest.getPaymentMethod() > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付方式无效，请选择：0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换");
        }

        // 检查分类是否存在
        Category category = categoryService.getById(productAddRequest.getCategoryId());
        validateResourceExists(category, "分类");

        // 创建商品对象
        Product product = new Product();
        BeanUtils.copyProperties(productAddRequest, product);
        product.setCategoryId(category);
        
        // 设置支付方式，使用用户选择的支付方式
        product.setPaymentMethod(productAddRequest.getPaymentMethod());
        
        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);
        product.setUserId(currentUser);

        // 添加商品
        boolean result = productService.addProduct(product, request);
        
        logOperation("添加商品", result, request, 
                "商品名称", productAddRequest.getProductName(),
                "分类", category.getName(),
                "支付方式", product.getPaymentMethod()
        );
        return handleOperationResult(result, "商品添加成功", product.getId());
    }

    /**
     * 更新商品信息
     *
     * @param productUpdateRequest 商品更新信息
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @Operation(summary = "更新商品信息", description = "用户更新自己的商品信息")
    @PutMapping("/update")
    @LoginRequired
    public BaseResponse<Boolean> updateProduct(
            @Parameter(description = "商品更新信息") @RequestBody ProductUpdateRequest productUpdateRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(productUpdateRequest, "商品更新信息");
        validateId(productUpdateRequest.getId(), "商品ID");

        // 检查商品是否存在
        Product existingProduct = productService.getById(productUpdateRequest.getId());
        validateResourceExists(existingProduct, "商品");

        // 创建更新对象
        Product product = new Product();
        BeanUtils.copyProperties(productUpdateRequest, product);
        
        // 如果提供了分类ID，检查分类是否存在
        if (productUpdateRequest.getCategoryId() != null) {
            Category category = categoryService.getById(productUpdateRequest.getCategoryId());
            validateResourceExists(category, "分类");
            product.setCategoryId(category);
        }

        // 更新商品
        boolean result = productService.updateProduct(product, request);
        
        logOperation("更新商品", result, request, 
                "商品ID", productUpdateRequest.getId(),
                "支付方式", product.getPaymentMethod()
        );
        return handleOperationResult(result, "商品更新成功");
    }

    /**
     * 删除商品
     *
     * @param deleteRequest 删除请求
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Operation(summary = "删除商品", description = "用户删除自己的商品")
    @PostMapping("/delete")
    @LoginRequired
    public BaseResponse<Boolean> deleteProduct(
            @Parameter(description = "删除请求") @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(deleteRequest, "删除请求");
        validateId(deleteRequest.getId(), "商品ID");

        // 检查商品是否存在
        validateResourceExists(productService.getById(deleteRequest.getId()), "商品");

        // 删除商品
        boolean result = productService.deleteProduct(deleteRequest.getId(), request);
        
        logOperation("删除商品", result, request, "商品ID", deleteRequest.getId());
        return handleOperationResult(result, "商品删除成功");
    }

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @Operation(summary = "获取商品详情", description = "根据商品ID获取商品详细信息")
    @GetMapping("/get/{id}")
    public BaseResponse<Product> getProductById(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        // 参数校验
        validateId(id, "商品ID");

        // 获取商品信息
        Product product = productService.getProductDetail(id);
        validateResourceExists(product, "商品");

        logOperation("获取商品详情", null, "商品ID", id);
        return ResultUtils.success(product);
    }

    /**
     * 分页获取商品列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页商品列表
     */
    @Operation(summary = "分页获取商品列表", description = "分页获取已上架的商品列表")
    @GetMapping("/list/page")
    public BaseResponse<Page<Product>> listProductByPage(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        // 参数校验
        Page<Product> page = validatePageParams(current, size);

        // 执行分页查询
        List<Product> productList = productService.getProductList(page);
        
        logOperation("分页获取商品列表", null, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 根据分类获取商品列表
     *
     * @param categoryId 分类ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页商品列表
     */
    @Operation(summary = "根据分类获取商品列表", description = "根据分类ID分页获取商品列表")
    @GetMapping("/list/category/{categoryId}")
    public BaseResponse<Page<Product>> listProductsByCategory(
            @Parameter(description = "分类ID") @PathVariable Long categoryId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        // 参数校验
        validateId(categoryId, "分类ID");
        Page<Product> page = validatePageParams(current, size);

        // 检查分类是否存在
        validateResourceExists(categoryService.getById(categoryId), "分类");

        // 执行分页查询
        List<Product> productList = productService.getProductsByCategory(categoryId, page);
        
        logOperation("根据分类获取商品列表", null, 
                "分类ID", categoryId,
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 根据关键词搜索商品
     *
     * @param keyword 关键词
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页商品列表
     */
    @Operation(summary = "搜索商品", description = "根据关键词搜索商品")
    @GetMapping("/search")
    public BaseResponse<Page<Product>> searchProducts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        // 参数校验
        validateNotBlank(keyword, "搜索关键词");
        Page<Product> page = validatePageParams(current, size);

        // 执行搜索
        List<Product> productList = productService.searchProducts(keyword, page);
        
        logOperation("搜索商品", null, 
                "关键词", keyword,
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
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
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 分页商品列表
     */
    @Operation(summary = "高级搜索商品", description = "多条件组合搜索商品，支持分类、价格、支付方式筛选和排序")
    @GetMapping("/advanced-search")
    public BaseResponse<Page<Product>> advancedSearchProducts(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "最高价格") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "支付方式 (0-现金, 1-微信, 2-积分, 3-交换)") @RequestParam(required = false) Integer paymentMethod,
            @Parameter(description = "排序字段 (price/createtime/name)") @RequestParam(required = false) String sortField,
            @Parameter(description = "排序顺序 (asc/desc)") @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        Page<Product> page = validatePageParams(current, size);

        // 验证价格范围
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最低价格不能高于最高价格");
        }

        // 验证支付方式
        if (paymentMethod != null && (paymentMethod < 0 || paymentMethod > 3)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付方式无效");
        }

        // 执行高级搜索
        List<Product> productList = productService.advancedSearchProducts(
                keyword, categoryId, minPrice, maxPrice, paymentMethod, sortField, sortOrder, page);

        
        logOperation("高级搜索商品", request, 
                "关键词", keyword,
                "分类ID", categoryId,
                "价格区间", minPrice + "-" + maxPrice,
                "支付方式", paymentMethod,
                "排序", sortField + " " + sortOrder,
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 获取用户发布的商品列表
     *
     * @param userId 用户ID
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 分页商品列表
     */
    @Operation(summary = "获取用户发布的商品列表", description = "根据用户ID获取该用户发布的商品列表")
    @GetMapping("/list/user/{userId}")
    public BaseResponse<Page<Product>> listUserProducts(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        validateId(userId, "用户ID");
        Page<Product> page = validatePageParams(current, size);

        // 检查用户是否存在
        validateResourceExists(userService.getById(userId), "用户");

        // 执行分页查询
        List<Product> productList = productService.getUserProducts(userId, page);
        
        logOperation("获取用户发布的商品列表", request, 
                "用户ID", userId,
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 获取当前用户发布的商品列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 分页商品列表
     */
    @Operation(summary = "获取当前用户发布的商品列表", description = "获取当前登录用户发布的商品列表")
    @GetMapping("/list/my")
    @LoginRequired
    public BaseResponse<Page<Product>> listMyProducts(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        // 参数校验
        Page<Product> page = validatePageParams(current, size);

        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);

        // 执行分页查询
        List<Product> productList = productService.getUserProducts(currentUser.getId(), page);
        
        logOperation("获取当前用户发布的商品列表", request, 
                "当前页", current,
                "每页大小", size
        );
        return ResultUtils.success(page);
    }

    /**
     * 更新商品状态
     *
     * @param id 商品ID
     * @param status 商品状态
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @Operation(summary = "更新商品状态", description = "更新商品状态（上架/下架/售出等）")
    @PutMapping("/status/{id}")
    @LoginRequired
    public BaseResponse<Boolean> updateProductStatus(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "商品状态") @RequestParam Integer status,
            HttpServletRequest request) {
        // 参数校验
        validateId(id, "商品ID");
        validateNotNull(status, "商品状态");

        // 检查商品是否存在
        validateResourceExists(productService.getById(id), "商品");

        // 更新商品状态
        boolean result = productService.updateProductStatus(id, status, request);
        
        logOperation("更新商品状态", result, request, 
                "商品ID", id,
                "状态", status
        );
        return handleOperationResult(result, "商品状态更新成功");
    }

    /**
     * 获取最新商品列表
     *
     * @param limit 限制数量
     * @return 最新商品列表
     */
    @Operation(summary = "获取最新商品列表", description = "获取最新发布的商品列表")
    @GetMapping("/latest")
    public BaseResponse<List<Product>> getLatestProducts(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") int limit) {
        // 参数校验
        if (limit <= 0 || limit > 50) {
            throw new BusinessException(com.zhp.flea_market.common.ErrorCode.PARAMS_ERROR, "限制数量必须在1-50之间");
        }

        // 获取最新商品
        List<Product> productList = productService.getLatestProducts(limit);
        
        logOperation("获取最新商品列表", null, "限制数量", limit);
        return ResultUtils.success(productList);
    }

    /**
     * 管理员审核商品
     *
     * @param id 商品ID
     * @param status 审核状态
     * @param request HTTP请求
     * @return 是否审核成功
     */
    @Operation(summary = "审核商品", description = "管理员审核商品（上架/拒绝）")
    @PutMapping("/review/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewProduct(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "审核状态") @RequestParam Integer status,
            HttpServletRequest request) {
        // 参数校验
        validateId(id, "商品ID");
        validateNotNull(status, "审核状态");

        // 检查商品是否存在
        validateResourceExists(productService.getById(id), "商品");

        // 审核商品
        boolean result = productService.updateProductStatus(id, status, request);
        
        logOperation("审核商品", result, request, 
                "商品ID", id,
                "审核状态", status
        );
        return handleOperationResult(result, "商品审核成功");
    }

    /**
     * 管理员获取所有商品列表（包括未审核的）
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param status 商品状态
     * @param request HTTP请求
     * @return 分页商品列表
     */
    @Operation(summary = "管理员获取所有商品列表", description = "管理员获取所有商品列表（包括未审核的）")
    @GetMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Product>> adminListProducts(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "商品状态") @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        // 参数校验
        Page<Product> page = validatePageParams(current, size);

        // 构建查询条件
        QueryWrapper<Product> queryWrapper = productService.getQueryWrapper(
                keyword, categoryId, null, null, status
        );

        // 执行分页查询
        Page<Product> productPage = productService.page(page, queryWrapper);
        
        logOperation("管理员获取所有商品列表", request, 
                "当前页", current,
                "每页大小", size,
                "关键词", keyword,
                "分类ID", categoryId,
                "状态", status
        );
        return ResultUtils.success(productPage);
    }
}