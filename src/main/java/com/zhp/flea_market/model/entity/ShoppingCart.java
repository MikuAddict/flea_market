package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
     * 关联用户
     */
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User userId;

    /**
     * 关联商品
     */
    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product productId;

    /**
     * 添加时间
     */
    private Date createTime;
}
