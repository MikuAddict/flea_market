package com.zhp.flea_market.repository;

import com.zhp.flea_market.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * 根据名称查询商品类别
     */
    Category findByName(String name);
}
