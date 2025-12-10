package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

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
     * 订单状态 (0-待支付, 1-已支付, 2-已完成, 3-已取消)
     */
    private Integer status;

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