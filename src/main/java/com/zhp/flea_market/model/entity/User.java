package com.zhp.flea_market.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 用户表
 */
@Entity
@Table(name = "user")
@Data
public class User implements Serializable {

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色：user/admin/ban
     */
    @Column(columnDefinition = "varchar(20) default 'user'")
    private String userRole = "user";
    /**
     * 用户状态 (0-待审核, 1-已通过, 2-已拒绝)
     */
    @Column(columnDefinition = "int default 0")
    private Integer userStatus = 0;
    /**
     * 联系方式
     */
    private String userPhone;
    /**
     * 用户积分
     */
    @Column(columnDefinition = "decimal(10,2) default 0.00")
    private BigDecimal point = BigDecimal.ZERO;
    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;
    /**
     * 创建时间
     */
    @Column(name = "create_time", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    /**
     * 在保存实体之前设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = new Date();
        }
        updateTime = new Date();
    }
}
