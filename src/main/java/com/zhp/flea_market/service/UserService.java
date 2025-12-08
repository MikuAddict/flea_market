package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.dto.request.UserQueryRequest;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.LoginUserVO;
import com.zhp.flea_market.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param userName      用户昵称
     * @param userPhone     联系方式
     */
    long register(String userAccount, String userPassword, String userName, String userPhone);

    /**
     * 用户登录
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);
    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
    /**
     * 根据 ID 查询二手物品信息并加锁
     *
     * @param id 二手物品 ID
     * @return 二手物品信息
     */
    User getByIdWithLock(Long id);

    /**
     * 更新用户积分
     *
     * @param userId 用户ID
     * @param points 积分变化值（正数为增加，负数为减少）
     * @return 是否更新成功
     */
    boolean updateUserPoints(Long userId, BigDecimal points);

    /**
     * 获取用户积分
     *
     * @param userId 用户ID
     * @return 用户积分
     */
    BigDecimal getUserPoints(Long userId);

    /**
     * 更新用户状态（管理员）
     *
     * @param userId 用户ID
     * @param userStatus 用户状态 (0-待审核, 1-已通过, 2-已拒绝, 3-已禁用)
     * @param request HTTP请求
     * @return 是否更新成功
     */
    boolean updateUserStatus(Long userId, Integer userStatus, HttpServletRequest request);

}
