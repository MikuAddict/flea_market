package com.zhp.flea_market.model.dto.request;

import lombok.Data;

@Data
public class UserRegisterRequest {
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
     * 联系方式
     */
    private String userPhone;
}
