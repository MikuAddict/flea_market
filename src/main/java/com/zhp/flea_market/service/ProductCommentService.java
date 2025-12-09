package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.dto.request.ProductCommentAddRequest;
import com.zhp.flea_market.model.dto.request.ProductCommentQueryRequest;
import com.zhp.flea_market.model.entity.ProductComment;
import com.zhp.flea_market.model.vo.ProductCommentVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 二手物品留言服务接口
 */
public interface ProductCommentService extends IService<ProductComment> {

    /**
     * 添加留言
     *
     * @param commentAddRequest 留言信息
     * @param request HTTP请求
     * @return 添加成功的留言ID
     */
    Long addComment(ProductCommentAddRequest commentAddRequest, HttpServletRequest request);

    /**
     * 分页获取留言列表
     *
     * @param queryRequest 查询条件
     * @return 分页留言列表
     */
    Page<ProductCommentVO> getCommentsByPage(ProductCommentQueryRequest queryRequest);

    /**
     * 获取留言的回复列表
     *
     * @param parentId 父留言ID
     * @return 回复列表
     */
    List<ProductCommentVO> getReplies(Long parentId);

    /**
     * 删除留言
     *
     * @param id 留言ID
     * @param request HTTP请求
     * @return 是否删除成功
     */
    Boolean deleteComment(Long id, HttpServletRequest request);

    /**
     * 构建留言树形结构
     *
     * @param productId 二手物品ID
     * @return 树形结构的留言列表
     */
    List<ProductCommentVO> getCommentTree(Long productId);

    /**
     * 获取查询条件构造器
     *
     * @param queryRequest 查询请求
     * @return 查询条件构造器
     */
    default Object getQueryWrapper(ProductCommentQueryRequest queryRequest) {
        // 默认实现，可以在ServiceImpl中覆盖
        return null;
    }
}