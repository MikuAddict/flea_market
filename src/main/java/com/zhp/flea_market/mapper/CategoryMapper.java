package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 商品类别 Mapper 接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 查询每个分类下的商品数量
     * @return 分类ID到商品数量的映射
     */
    @Select("SELECT category_id as categoryId, COUNT(*) as productCount FROM product WHERE deleted = 0 AND status = 1 GROUP BY category_id")
    Map<Long, Long> selectProductCountGroupByCategory();
}