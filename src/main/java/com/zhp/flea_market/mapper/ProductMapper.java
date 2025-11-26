package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据用户ID查询商品列表
     */
    @Select("SELECT * FROM product WHERE user_id = #{userId}")
    List<Product> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据商品分类ID和商品状态查询商品列表
     */
    @Select("SELECT * FROM product WHERE category_id = #{categoryId} AND status = #{status}")
    List<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") Integer status);
    
    /**
     * 根据商品状态查询商品列表
     */
    @Select("SELECT * FROM product WHERE status = #{status}")
    List<Product> findByStatus(@Param("status") Integer status);
    
    /**
     * 根据用户ID和商品状态查询商品列表
     */
    @Select("SELECT * FROM product WHERE user_id = #{userId} AND status = #{status}")
    List<Product> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 根据商品名字和商品状态查询商品列表
     */
    @Select("SELECT * FROM product WHERE product_name LIKE CONCAT('%', #{name}, '%') AND status = #{status}")
    List<Product> findByNameAndStatus(@Param("name") String name, @Param("status") Integer status);
}
