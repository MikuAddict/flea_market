package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品类别 Mapper 接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询所有分类及其商品数量
     * 使用LEFT JOIN确保即使没有商品的分类也会显示
     * 只统计已上架的商品（status = 1）
     */
    @Select("SELECT c.id as categoryId, c.name as categoryName, COALESCE(p.productCount, 0) as productCount " +
           "FROM category c " +
           "LEFT JOIN (" +
           "  SELECT category_id, COUNT(*) as productCount " +
           "  FROM product " +
           "  WHERE deleted = 0 AND status = 1 " +  // 只统计未删除且已上架的商品
           "  GROUP BY category_id" +
           ") p ON c.id = p.category_id " +
           "WHERE c.deleted = 0")  // 只查询未删除的分类
    List<Map<String, Object>> selectAllCategoriesWithProductCount();
}