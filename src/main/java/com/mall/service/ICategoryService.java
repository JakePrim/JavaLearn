package com.mall.service;

import com.mall.common.ServerResponse;

import javax.servlet.http.HttpSession;

public interface ICategoryService {
    ServerResponse addCategory(String name, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);
}
