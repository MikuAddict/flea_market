package com.zhp.flea_market.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.annotation.LoginRequired;
import com.zhp.flea_market.common.BaseResponse;
import com.zhp.flea_market.common.ResultUtils;
import com.zhp.flea_market.model.dto.request.ProductCommentAddRequest;
import com.zhp.flea_market.model.dto.request.ProductCommentQueryRequest;
import com.zhp.flea_market.model.vo.ProductCommentVO;
import com.zhp.flea_market.service.ProductCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 二手物品留言接口
 */
@RestController
@RequestMapping("/product-comment")
@Slf4j
@Tag(name = "二手物品留言管理", description = "二手物品留言的增删改查等接口")
public class ProductCommentController extends BaseController {

    @Resource
    private ProductCommentService productCommentService;

    /**
     * 添加留言
     *
     * @param commentAddRequest 留言信息
     * @param request HTTP请求
     * @return 添加成功的留言ID
     */
    @Operation(summary = "添加留言", description = "用户对二手物品添加留言或回复")
    @PostMapping("/add")
    @LoginRequired
    public BaseResponse<Long> addComment(
            @Parameter(description = "留言添加信息") @RequestBody ProductCommentAddRequest commentAddRequest,
            HttpServletRequest request) {
        // 参数校验
        validateNotNull(commentAddRequest, "留言信息");
        validateId(commentAddRequest.getProductId(), "二手物品ID");
        validateNotBlank(commentAddRequest.getContent(), "留言内容");

        // 添加留言
        Long commentId = productCommentService.addComment(commentAddRequest, request);
        
        logOperation("添加留言", true, request, 
                "二手物品ID", commentAddRequest.getProductId(),
                "父留言ID", commentAddRequest.getParentId(),
                "回复用户ID", commentAddRequest.getReplyUserId()
        );
        return ResultUtils.success(commentId);
    }

    /**
     * 分页获取留言列表
     *
     * @param queryRequest 查询条件
     * @return 分页留言列表
     */
    @Operation(summary = "分页获取留言列表", description = "分页获取二手物品的留言列表")
    @GetMapping("/page")
    public BaseResponse<Page<ProductCommentVO>> getCommentsByPage(ProductCommentQueryRequest queryRequest) {
        // 参数校验
        if (queryRequest == null) {
            queryRequest = new ProductCommentQueryRequest();
        }
        
        // 设置默认值
        if (queryRequest.getCurrent() < 1) {
            queryRequest.setCurrent(1);
        }
        if (queryRequest.getPageSize() < 1 || queryRequest.getPageSize() > 100) {
            queryRequest.setPageSize(10);
        }

        // 获取分页留言列表
        Page<ProductCommentVO> commentPage = productCommentService.getCommentsByPage(queryRequest);
        
        logOperation("分页获取留言列表", null, 
                "二手物品ID", queryRequest.getProductId(),
                "当前页", queryRequest.getCurrent(),
                "每页大小", queryRequest.getPageSize()
        );
        return ResultUtils.success(commentPage);
    }

    /**
     * 获取留言树形结构
     *
     * @param productId 二手物品ID
     * @return 树形结构的留言列表
     */
    @Operation(summary = "获取留言树形结构", description = "获取二手物品的所有留言及回复，以树形结构展示")
    @GetMapping("/tree")
    public BaseResponse<List<ProductCommentVO>> getCommentTree(
            @Parameter(description = "二手物品ID") @RequestParam Long productId) {
        // 参数校验
        validateId(productId, "二手物品ID");

        // 获取留言树形结构
        List<ProductCommentVO> commentTree = productCommentService.getCommentTree(productId);
        
        logOperation("获取留言树形结构", null, "二手物品ID", productId);
        return ResultUtils.success(commentTree);
    }

    /**
     * 删除留言
     *
     * @param id 留言ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @Operation(summary = "删除留言", description = "用户删除自己的留言（仅限留言发布者和管理员）")
    @DeleteMapping("/{id}")
    @LoginRequired
    public BaseResponse<Boolean> deleteComment(
            @Parameter(description = "留言ID") @PathVariable Long id,
            HttpServletRequest request) {
        // 参数校验
        validateId(id, "留言ID");

        // 删除留言
        Boolean result = productCommentService.deleteComment(id, request);
        
        logOperation("删除留言", result, request, "留言ID", id);
        return handleOperationResult(result, "留言删除成功");
    }
}