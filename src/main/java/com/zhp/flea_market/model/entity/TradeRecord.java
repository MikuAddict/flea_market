package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 交易记录实体
 */
@TableName("trade_record")
@Data
public class TradeRecord {

    /**
     * 交易记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
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
     * 支付方式描述
     */
    @TableField("payment_method_desc")
    private String paymentMethodDesc;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "trade_time", fill = FieldFill.INSERT)
    private Date tradeTime;

    /**
     * 交易状态 (1-成功, 2-已完成评价, 3-已退款)
     */
    @TableField("trade_status")
    private Integer tradeStatus;

    /**
     * 交易备注
     */
    private String remark;

    /**
     * 评价ID
     */
    private Long reviewId;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted = 0;
    
    /**
     * 订单信息，非数据库字段
     */
    @TableField(exist = false)
    private Order order;

    /**
     * 商品信息，非数据库字段
     */
    @TableField(exist = false)
    private Product product;

    /**
     * 买家信息，非数据库字段
     */
    @TableField(exist = false)
    private User buyer;

    /**
     * 卖家信息，非数据库字段
     */
    @TableField(exist = false)
    private User seller;

    /**
     * 评价信息，非数据库字段
     */
    @TableField(exist = false)
    private Review review;
}