package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
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
     * 二手物品ID
     */
    private Long productId;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted = 0;
}
