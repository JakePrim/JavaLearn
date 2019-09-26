package com.mall.controller.backend;

import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.User;
import com.mall.service.ICategoryService;
import com.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    //添加分类
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName,
                                      @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        //判断登录
        ServerResponse response = allowLogic(session);
        if (response.isSuccess())
            return iCategoryService.addCategory(categoryName, parentId);
        return response;
    }

    //更新品类名字
    @RequestMapping("update_category_name.do")
    @ResponseBody
    public ServerResponse updateCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        ServerResponse serverResponse = allowLogic(session);
        if (serverResponse.isSuccess()) {
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        }
        return serverResponse;
    }

    /**
     * 判断该用户是否有操作权限
     *
     * @param session
     * @return
     */
    private ServerResponse allowLogic(HttpSession session) {
        //判断登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        //校验是否为管理员
        ServerResponse response = iUserService.checkAdminRole(user);
        if (!response.isSuccess()) {
            return ServerResponse.createByErrorMessage("该用户不是管理员");
        }
        return response;
    }
}
