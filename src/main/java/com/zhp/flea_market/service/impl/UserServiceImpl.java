package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.mapper.UserMapper;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.LoginUserVO;
import com.zhp.flea_market.model.vo.UserVO;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "zhanghaipeng";

    @Override
    public long register(String userAccount, String userPassword, String userName, String userPhone) {
        // 检查账号是否已存在
        if (userMapper.existsByUserAccount(userAccount)) {
            throw new RuntimeException("账号已存在");
        }

        // 创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        // 实际项目中应该使用加密算法处理密码
        user.setUserPassword(userPassword);
        user.setUserName(userName);
        user.setUserPhone(userPhone);
        user.setUserRole("user"); // 默认角色
        user.setPoint(0); // 默认积分
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public LoginUserVO login(String userAccount, String userPassword) {
        User user = userMapper.findByUserAccount(userAccount);

        if (user == null || !user.getUserPassword().equals(userPassword)) {
            throw new RuntimeException("账号或密码错误");
        }

        return getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 在实际应用中，应该从session或token中获取用户信息
        // 这里简化处理，返回null
        return null;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 允许未登录的情况下获取用户信息
        return getLoginUser(request);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        User user = getLoginUser(request);
        return user != null && "admin".equals(user.getUserRole());
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && "admin".equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 在实际应用中，应该清除session或token
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }

        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUserName(user.getUserName());
        userVO.setUserAvatar(user.getUserAvatar());
        userVO.setUserRole(user.getUserRole());
        userVO.setPoint(user.getPoint());
        userVO.setRegisterTime(user.getCreateTime());
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (userList == null || userList.isEmpty()) {
            return new ArrayList<>();
        }

        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }
}
