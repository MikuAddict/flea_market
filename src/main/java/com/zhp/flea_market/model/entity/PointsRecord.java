package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 积分记录实体
 */
@TableName("points_record")
@Data
public class PointsRecord {

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 积分变化值（正数为增加，负数为减少）
     */
    private BigDecimal pointsChange;

    /**
     * 变化后的积分
     */
    private BigDecimal pointsAfter;

    /**
     * 积分变动类型
     * 1: 订单完成奖励
     * 2: 评价奖励/惩罚
     * 3: 其他
     */
    private Integer changeType;

    /**
     * 关联的业务ID（如订单ID、评价ID等）
     */
    private Long relatedId;

    /**
     * 变动描述
     */
    private String description;

    /**
     * 创建时间
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
}