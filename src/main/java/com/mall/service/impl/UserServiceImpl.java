package com.mall.service.impl;

import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.common.TokenCache;
import com.mall.dao.UserMapper;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import com.mall.util.MD5Util;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;
import java.util.logging.Logger;

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
        User user = userMapper.selectLogin(username, MD5Util.MD5EncodeUtf8(password));
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //返回用户信息 要将密码置为""
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        //校验用户名是否存在
        ServerResponse<String> response = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!response.isSuccess()) {
            return response;
        }
        //校验邮箱是否存在
        response = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!response.isSuccess()) {
            return response;
        }
        //普通用户权限
        user.setRole(Const.Role.ROLE_CUSTOME);
        //对密码进行md5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //将用户插入数据库
        int insert = userMapper.insert(user);
        if (insert == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createByErrorMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }

            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> forgetGetQuestion(String username) {
        //校验用户名是否存在
        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {//用户名不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //用户名存在 从数据库中查找用户设置的问题
        String question = userMapper.queryQuestionUsername(username);
        //如果问题不为空 则返回
        if (StringUtils.isNoneBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码问题不存在");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int count = userMapper.checkAnswer(username, question, answer);
        if (count > 0) {
            //说明问题正确
            String token = UUID.randomUUID().toString();
            //将token放到本地cache中 设置有效期
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, token);
            return ServerResponse.createBySuccess(token);
        }
        return ServerResponse.createByErrorMessage("答案错误");
    }

    @Override
    public ServerResponse<String> forgetRestPassword(String username, String newPassword, String token) {

        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {//用户名不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //获取缓存的token
        String tokens = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(tokens)) {
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        if (StringUtils.equals(token, tokens)) {
            //将新密码加密
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
            if (rowCount > 0) {
                //密码设置成功后将token缓存清除
                TokenCache.cleatToken(TokenCache.TOKEN_PREFIX + username);
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
        //防止横向越权，要校验一下这个用户的旧密码，一定要指定这个用户，因为我们会查询一个count(1),如果不指定id，那么结果就是true
        int resuleCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resuleCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        //username 不能被更新的
        //email 是否已经存在，并且存在的email，如果相同的话不能是我们当前这个用户的。
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已存在，请更换email在尝试更新");
        }
        //phone 是否已经存在，如果存在且不是当前用户则不能修改
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        //sql会进行null判断 如果为null 就不更新
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if (user != null) {
            if (user.getRole() == Const.Role.ROLE_ADMIN) {
                return ServerResponse.createBySuccess();
            }
        }
        return ServerResponse.createByError();
    }
}
