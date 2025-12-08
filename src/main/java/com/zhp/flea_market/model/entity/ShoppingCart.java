package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

/**
 * 购物车实体
 */
@TableName("shopping_cart")
@Data
public class ShoppingCart {

    /**
     * 购物车项ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 添加时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted = 0;
    
    /**
     * 关联用户，非数据库字段
     */
    @TableField(exist = false)
    private User user;
    
    /**
     * 关联商品，非数据库字段
     */
    @TableField(exist = false)
    private Product product;
}
