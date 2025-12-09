package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhp.flea_market.model.entity.ProductComment;
import com.zhp.flea_market.model.vo.ProductCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 二手物品留言 Mapper 接口
 */
@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {

    /**
     * 分页查询留言及其回复
     * @param page 分页对象
     * @param productId 二手物品ID
     * @param parentId 父留言ID（0表示一级留言）
     * @return 留言列表
     */
    Page<ProductCommentVO> selectCommentsWithReplies(Page<ProductCommentVO> page, 
                                                   @Param("productId") Long productId,
                                                   @Param("parentId") Long parentId);
}