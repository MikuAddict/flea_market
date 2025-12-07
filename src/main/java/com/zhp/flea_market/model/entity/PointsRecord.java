package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 积分记录实体
 */
@Entity
@Table(name = "points_record")
@Data
public class PointsRecord {

    /**
     * 记录ID
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
    private Date createTime;

    /**
     * 在保存实体之前设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = new Date();
        }
    }
}