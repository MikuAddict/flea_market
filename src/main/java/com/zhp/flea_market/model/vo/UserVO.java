package com.zhp.flea_market.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class UserVO {
    /**
     * id
     */
    private Long id;

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
    private Integer point;

    /**
     * 注册时间
     */
    private Date registerTime;
}
