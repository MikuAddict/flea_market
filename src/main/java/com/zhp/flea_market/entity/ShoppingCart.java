package com.zhp.flea_market.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

/**
 * 购物车实体
 */
@Entity
@Table(name = "shopping_cart")
@Data
public class ShoppingCart {

    /**
     * 购物车项ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 添加时间
     */
    private Date createTime;
}
