package com.zhp.flea_market.repository;

import com.zhp.flea_market.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * 根据买家ID查询订单列表
     */
    List<Order> findByBuyerId(Long buyerId);
    /**
     * 根据卖家ID查询订单列表
     */
    List<Order> findBySellerId(Long sellerId);
    /**
     * 根据订单状态查询订单列表
     */
    List<Order> findByStatus(Integer status);
    /**
     * 根据买家ID和订单状态查询订单列表
     */
    List<Order> findByBuyerIdAndStatus(Long buyerId, Integer status);
}
