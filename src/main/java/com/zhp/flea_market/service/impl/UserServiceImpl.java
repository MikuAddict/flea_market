package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.constant.CommonConstant;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.mapper.UserMapper;
import com.zhp.flea_market.model.dto.request.UserQueryRequest;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.enums.UserRoleEnum;
import com.zhp.flea_market.model.vo.LoginUserVO;
import com.zhp.flea_market.model.vo.UserVO;
import com.zhp.flea_market.service.UserService;
import com.zhp.flea_market.service.ImageStorageService;
import com.zhp.flea_market.utils.PageUtils;
import com.zhp.flea_market.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.zhp.flea_market.constant.UserConstant.USER_LOGIN_STATE;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "114514";

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param userName      用户昵称
     * @param userPhone     联系方式
     * @return 新用户 id
     */
    @Override
    public long register(String userAccount, String userPassword, String userName, String userPhone) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, userName, userPhone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (userPhone.length() != 11){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号长度错误");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_account", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName(userName);
            user.setUserPhone(userPhone);
            // 设置默认值
            user.setUserRole("user");
            user.setUserStatus(0); // 待审核状态
            user.setPoint(BigDecimal.ZERO);
            // 设置创建时间
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    /**
     * 用户登录
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request) {
// 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        
        // 检查用户审核状态
        if (user.getUserStatus() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "用户账号未初始化，请联系管理员");
        }
        
        // 0-待审核, 1-已通过, 2-已拒绝
        if (user.getUserStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "您的账号正在审核中，请耐心等待");
        }
        
        if (user.getUserStatus() == 2) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "您的账号审核未通过，无法登录");
        }
        
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先尝试从统一认证拦截器设置的request属性中获取用户信息
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null || currentUser.getId() == null) {
            // 如果request属性中没有用户信息，尝试从session中获取
            Object userObj = request.getSession().getAttribute("USER_LOGIN_STATE");
            currentUser = (User) userObj;
            if (currentUser == null || currentUser.getId() == null) {
                return null;
            }
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 从统一认证拦截器设置的request属性中获取用户信息
        User user = (User) request.getAttribute("currentUser");
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setRegisterTime(user.getCreateTime());
        return userVO;
    }

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList
     * @return
     */
    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        BigDecimal userPoint = userQueryRequest.getPoint();
        String userRole = userQueryRequest.getUserRole();
        Integer userStatus = userQueryRequest.getUserStatus();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.eq(userPoint != null, "point", userPoint);
        queryWrapper.eq(userStatus != null, "user_status", userStatus);
        queryWrapper.like(StringUtils.isNotBlank(userName), "user_name", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取已登录用户的查询条件
     */
    @Override
    public User getByIdWithLock(Long id) {
        return baseMapper.selectByIdWithLock(id);
    }

    /**
     * 更新用户积分
     *
     * @param userId 用户ID
     * @param points 积分变化值（正数为增加，负数为减少）
     * @return 是否更新成功
     */
    @Override
    public boolean updateUserPoints(Long userId, BigDecimal points) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }
        
        // 获取用户当前积分
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 计算新积分（确保积分不为负数，处理积分可能为null的情况）
        BigDecimal currentPoints = user.getPoint() != null ? user.getPoint() : BigDecimal.ZERO;
        BigDecimal newPoints = currentPoints.add(points);
        if (newPoints.compareTo(BigDecimal.ZERO) < 0) {
            newPoints = BigDecimal.ZERO;
        }
        
        System.out.println("更新用户积分 - 用户ID: " + userId + 
                ", 当前积分: " + currentPoints + 
                ", 积分变化: " + points + 
                ", 新积分: " + newPoints);
        
        // 更新积分
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPoint(newPoints);
        updateUser.setUpdateTime(new Date());
        
        boolean result = this.updateById(updateUser);
        System.out.println("积分更新结果: " + result);
        return result;
    }

    /**
     * 获取用户积分
     *
     * @param userId 用户ID
     * @return 用户积分
     */
    @Override
    public BigDecimal getUserPoints(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }
        
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 处理积分可能为null的情况
        BigDecimal points = user.getPoint() != null ? user.getPoint() : BigDecimal.ZERO;
        System.out.println("获取用户积分 - 用户ID: " + userId + ", 积分值: " + points + ", 原始积分: " + user.getPoint());
        return points;
    }

    /**
     * 更新用户状态（管理员）
     *
     * @param userId 用户ID
     * @param userStatus 用户状态 (0-待审核, 1-已通过, 2-已拒绝, 3-已禁用)
     * @param request HTTP请求
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer userStatus, HttpServletRequest request) {
        // 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }
        
        if (userStatus == null || userStatus < 0 || userStatus > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户状态无效");
        }
        
        // 获取当前登录用户
        User currentUser = getLoginUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        
        // 权限校验：只有管理员可以更新用户状态
        if (!isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限更新用户状态");
        }
        
        // 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 特殊处理：如果设置为已禁用状态(3)，需要额外检查是否允许禁用
        if (userStatus == 3) {
            // 检查是否尝试禁用自己
            if (userId.equals(currentUser.getId())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能禁用当前登录用户");
            }
            
            // 检查是否尝试禁用其他管理员
            if ("admin".equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能禁用管理员用户");
            }
        }
        
        // 更新用户状态
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUserStatus(userStatus);

        return this.updateById(updateUser);
    }
}
