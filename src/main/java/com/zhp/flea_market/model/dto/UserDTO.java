package com.zhp.flea_market.model.dto;

import lombok.Data;
import java.util.Date;

@Data
public class UserDTO {
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
     * 联系方式
     */
    private String userPhone;

    /**
     * 用户积分
     */
    private Integer point;

    /**
     * 创建时间
     */
    private Date createTime;
}
