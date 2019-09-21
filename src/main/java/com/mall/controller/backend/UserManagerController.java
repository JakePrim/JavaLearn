package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manager/user/")
public class UserManagerController {

    @Autowired
    private IUserService iUserService;

    /**
     * 后台管理员登录
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse<User> login = iUserService.login(username, password);
        if (login.isSuccess()) {
            User user = login.getData();
            if (user.getRole() == Const.Role.ROLE_ADMIN) {
                //拥有管理员权限
                session.setAttribute(Const.CURRENT_USER, user);
            } else {
                return ServerResponse.createByErrorMessage("该用户没有管理员权限");
            }

        }
        return login;
    }
}
