package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 根据名称查询商品类别
     */
    @Select("SELECT * FROM category WHERE name = #{name}")
    Category findByName(@Param("name") String name);
}
