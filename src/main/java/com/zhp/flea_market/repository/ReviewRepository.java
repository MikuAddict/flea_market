package com.zhp.flea_market.repository;

import com.zhp.flea_market.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * 根据商品ID查询商品评价列表
     */
    List<Review> findByProductId(Long productId);
    /**
     * 根据用户ID查询商品评价列表
     */
    List<Review> findByUserId(Long userId);
    /**
     * 根据订单ID查询商品评价列表
     */
    List<Review> findByOrderId(Long orderId);
}
