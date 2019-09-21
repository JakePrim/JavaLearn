package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.User;

public interface IUserService {
    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String str, String type);

    ServerResponse<String> forgetGetQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetRestPassword(String username, String newPassword, String token);

    ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew);

    ServerResponse<User> updateInformation(User user);

    ServerResponse<User> getInformation(Integer userId);
}
