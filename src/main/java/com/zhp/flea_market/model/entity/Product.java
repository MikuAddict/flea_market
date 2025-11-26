package com.zhp.flea_market.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品信息实体
 */
@Entity
@Table(name = "product")
@Data
public class Product {

    /**
     * 商品ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品图片地址
     */
    private String imageUrl;

    /**
     * 商品状态 (0-待审核, 1-已上架, 2-已下架, 3-已售出)
     */
    private Integer status;

    /**
     * 商品分类ID
     */
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * 发布者ID
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
