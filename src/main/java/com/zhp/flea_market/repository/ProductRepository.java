package com.zhp.flea_market.repository;

import com.zhp.flea_market.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /**
     * 根据用户ID查询商品列表
     */
    List<Product> findByUserId(Long userId);
    /**
     * 根据商品分类ID和商品状态查询商品列表
     */
    List<Product> findByCategoryIdAndStatus(Long categoryId, Integer status);
    /**
     * 根据商品状态查询商品列表
     */
    List<Product> findByStatus(Integer status);
    /**
     * 根据用户ID和商品状态查询商品列表
     */
    List<Product> findByUserIdAndStatus(Long userId, Integer status);
    /**
     * 根据商品名字和商品状态查询商品列表
     */
    List<Product> findByNameAndStatus(String name, Integer status);
}
