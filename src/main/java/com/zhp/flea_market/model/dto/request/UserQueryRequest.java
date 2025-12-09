package com.zhp.flea_market.model.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户积分
     */
    private BigDecimal point;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 用户状态：0-待审核, 1-已通过, 2-已拒绝, 3-已禁用
     */
    private Integer userStatus;

    private static final long serialVersionUID = 1L;
}