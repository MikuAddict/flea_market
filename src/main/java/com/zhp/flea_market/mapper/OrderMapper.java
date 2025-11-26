package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 根据买家ID查询订单列表
     */
    @Select("SELECT * FROM market_order WHERE buyer_id = #{buyerId}")
    List<Order> findByBuyerId(@Param("buyerId") Long buyerId);
    
    /**
     * 根据卖家ID查询订单列表
     */
    @Select("SELECT * FROM market_order WHERE seller_id = #{sellerId}")
    List<Order> findBySellerId(@Param("sellerId") Long sellerId);
    
    /**
     * 根据订单状态查询订单列表
     */
    @Select("SELECT * FROM market_order WHERE status = #{status}")
    List<Order> findByStatus(@Param("status") Integer status);
    
    /**
     * 根据买家ID和订单状态查询订单列表
     */
    @Select("SELECT * FROM market_order WHERE buyer_id = #{buyerId} AND status = #{status}")
    List<Order> findByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") Integer status);
}
