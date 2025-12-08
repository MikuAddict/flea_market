package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 二手物品信息实体
 */
@TableName("product")
@Data
public class Product {

    /**
     * 二手物品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 二手物品名称
     */
    private String productName;

    /**
     * 二手物品描述
     */
    private String description;

    /**
     * 二手物品价格
     */
    private BigDecimal price;

    /**
     * 主图URL（第一张图片）
     */
    private String mainImageUrl;

    /**
     * 所有图片URL列表（JSON格式存储）
     */
    private String imageUrls;

    /**
     * 二手物品状态 (0-待审核, 1-已通过, 2-已拒绝, 3-已售出)
     */
    private Integer status;

    /**
     * 支付方式 (0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换)
     */
    private Integer paymentMethod;

    /**
     * 二手物品分类ID
     */
    private Long categoryId;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted = 0;
}
