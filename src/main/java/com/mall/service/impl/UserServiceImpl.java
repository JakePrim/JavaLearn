package com.mall.service.impl;

import com.mall.common.ServerResponse;
import com.mall.dao.UserMapper;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("iUserService")//注入controller
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;//注入mapper 数据库操作

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);//检查登录的用户名是否存在

        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //TODO 密码登录md5
        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //返回用户信息 要将密码置为""
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<User> register(User user) {
        return null;
    }
}
