package com.zhp.flea_market.repository;

import com.zhp.flea_market.model.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    /**
     * 根据用户ID查询购物车列表
     */
    List<ShoppingCart> findByUserId(Long userId);
    /**
     * 根据用户ID和商品ID查询购物车列表
     */
    List<ShoppingCart> findByUserIdAndProductId(Long userId, Long productId);
}
