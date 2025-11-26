package com.zhp.flea_market.repository;

import com.zhp.flea_market.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据用户账号查询用户
     */
    Optional<User> findByUserAccount(String userAccount);
    /**
     * 根据用户账号判断用户是否存在
     */
    boolean existsByUserAccount(String userAccount);
}
