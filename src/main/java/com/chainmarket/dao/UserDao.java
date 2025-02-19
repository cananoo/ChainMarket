package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserDao extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    default User selectByUsername(String username) {
        return selectOne(new QueryWrapper<User>().eq("username", username));
    }
    
    /**
     * 根据手机号查询用户
     */
    default User selectByPhone(String phone) {
        return selectOne(new QueryWrapper<User>().eq("phone", phone));
    }
    
    /**
     * 检查用户名是否存在
     */
    default boolean checkUsernameExists(String username) {
        return exists(new QueryWrapper<User>().eq("username", username));
    }
    
    /**
     * 检查手机号是否存在
     */
    default boolean checkPhoneExists(String phone) {
        return exists(new QueryWrapper<User>().eq("phone", phone));
    }
    
    /**
     * 查询待审核用户列表
     */
    default List<User> selectPendingUsers() {
        return selectList(new QueryWrapper<User>()
                .eq("status", 0)
                .orderByDesc("createTime"));
    }
} 