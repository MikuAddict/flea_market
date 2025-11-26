package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户账号查询用户
     */
    @Select("SELECT * FROM user WHERE user_account = #{userAccount}")
    User findByUserAccount(@Param("userAccount") String userAccount);
    
    /**
     * 根据用户账号判断用户是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM user WHERE user_account = #{userAccount}")
    boolean existsByUserAccount(@Param("userAccount") String userAccount);
}
