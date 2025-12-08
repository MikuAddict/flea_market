package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
    
    /**
     * 根据用户ID查询购物车列表
     */
    @Select("SELECT * FROM shopping_cart WHERE user_id = #{userId}")
    List<ShoppingCart> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和二手物品ID查询购物车列表
     */
    @Select("SELECT * FROM shopping_cart WHERE user_id = #{userId} AND product_id = #{productId}")
    List<ShoppingCart> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
