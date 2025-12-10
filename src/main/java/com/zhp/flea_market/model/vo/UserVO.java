package com.zhp.flea_market.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserVO {
    /**
     * id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;


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
    private String userRole;

    /**
     * 用户积分
     */
    private BigDecimal point;

    /**
     * 注册时间
     */
    private Date registerTime;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户状态 (0-待审核, 1-已通过, 2-已拒绝, 3-已封禁)
     */
    private Integer userStatus;
}
