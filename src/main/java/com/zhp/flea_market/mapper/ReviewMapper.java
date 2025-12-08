package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    
    /**
     * 根据二手物品ID查询二手物品评价列表
     */
    @Select("SELECT * FROM review WHERE product_id = #{productId}")
    List<Review> findByProductId(@Param("productId") Long productId);
    
    /**
     * 根据用户ID查询二手物品评价列表
     */
    @Select("SELECT * FROM review WHERE user_id = #{userId}")
    List<Review> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据订单ID查询二手物品评价列表
     */
    @Select("SELECT * FROM review WHERE order_id = #{orderId}")
    List<Review> findByOrderId(@Param("orderId") Long orderId);
}
