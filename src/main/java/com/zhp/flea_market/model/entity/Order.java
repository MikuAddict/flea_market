package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体
 */
@TableName("market_order")
@Data
public class Order {

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 二手物品ID
     */
    private Long productId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付方式 (0-现金, 1-微信, 2-积分兑换, 3-物品交换)
     */
    private Integer paymentMethod;

    /**
     * 订单状态 (0-待支付, 1-已支付, 2-已完成, 3-已取消)
     */
    private Integer status;

    /**
     * 支付凭证URL (现金支付时买家上传的支付凭证)
     */
    private String paymentProof = "";

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 完成时间
     */
    private Date finishTime;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted = 0;
}