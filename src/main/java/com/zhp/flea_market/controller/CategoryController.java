package com.zhp.flea_market.controller;

import com.zhp.flea_market.annotation.AuthCheck;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.constant.UserConstant;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.model.entity.Category;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.service.CategoryService;
import com.zhp.flea_market.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类接口
 */
@RestController
@RequestMapping("/category")
@Slf4j
@Tag(name = "商品分类管理", description = "商品分类的增删改查接口")
public class CategoryController extends BaseController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private ProductService productService;

    /**
     * 获取所有商品分类
     *
     * @return 分类列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有商品分类", description = "获取系统中所有的商品分类信息")
    public BaseResponse<List<Category>> getCategoryList() {
        List<Category> categoryList = categoryService.getCategoryList();
        logOperation("获取所有商品分类", null, "分类数量", categoryList.size());
        return ResultUtils.success(categoryList);
    }

    /**
     * 添加商品分类
     *
     * @param category 分类信息
     * @param request HTTP请求
     * @return 新增分类的ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "添加商品分类", description = "管理员添加新的商品分类")
    public BaseResponse<Long> addCategory(
            @RequestBody Category category,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(category, "分类信息");
        validateNotBlank(category.getName(), "分类名称");
        
        // 检查分类名称是否已存在
        List<Category> categoryList = categoryService.getCategoryList();
        boolean isExist = categoryList.stream().anyMatch(c -> c.getName().equals(category.getName()));
        if (isExist) {
            throw new BusinessException(com.zhp.flea_market.common.ErrorCode.PARAMS_ERROR, "分类名称已存在");
        }

        // 添加分类
        boolean result = categoryService.addCategory(category);
        
        logOperation("添加商品分类", result, request, "分类名称", category.getName());
        return handleOperationResult(result, "商品分类添加成功", category.getId());
    }

    /**
     * 更新商品分类
     *
     * @param category 分类信息
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "更新商品分类", description = "管理员更新商品分类信息")
    public BaseResponse<Boolean> updateCategory(
            @Parameter(description = "分类信息") @RequestBody Category category,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(category, "分类信息");
        validateId(category.getId(), "分类ID");
        validateNotBlank(category.getName(), "分类名称");
        
        // 检查分类是否存在
        Category existCategory = categoryService.getById(category.getId());
        validateResourceExists(existCategory, "分类");

        // 检查分类名称是否与其他分类重复
        List<Category> categoryList = categoryService.getCategoryList();
        boolean isExist = categoryList.stream()
                .anyMatch(c -> c.getName().equals(category.getName()) && !c.getId().equals(category.getId()));
        if (isExist) {
            throw new BusinessException(com.zhp.flea_market.common.ErrorCode.PARAMS_ERROR, "分类名称已存在");
        }

        // 更新分类
        boolean result = categoryService.updateCategory(category);
        
        logOperation("更新商品分类", result, request, 
                "分类ID", category.getId(), 
                "分类名称", category.getName()
        );
        return handleOperationResult(result, "商品分类更新成功");
    }

    /**
     * 删除商品分类
     *
     * @param id 分类ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除商品分类", description = "管理员根据ID删除商品分类")
    public BaseResponse<Boolean> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            HttpServletRequest request) {
        // 参数校验
        validateId(id, "分类ID");
        
        // 检查分类是否存在
        Category existCategory = categoryService.getById(id);
        validateResourceExists(existCategory, "分类");

        // 检查分类下是否有商品，如果有则不能删除
        List<Product> productsInCategory = productService.lambdaQuery()
                .eq(Product::getCategory, existCategory)
                .list();
        if (!productsInCategory.isEmpty()) {
            throw new BusinessException(com.zhp.flea_market.common.ErrorCode.OPERATION_ERROR,
                "该分类下存在商品，无法删除。请先移除或转移分类下的商品");
        }
        
        // 删除分类
        boolean result = categoryService.deleteCategory(id);
        
        logOperation("删除商品分类", result, request, 
                "分类ID", id, 
                "分类名称", existCategory.getName()
        );
        return handleOperationResult(result, "商品分类删除成功");
    }
}