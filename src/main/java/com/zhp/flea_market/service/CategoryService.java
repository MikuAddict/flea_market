package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 查询所有二手物品类别
     */
    List<Category> getCategoryList();
    /**
     * 添加分类
     */
    boolean addCategory(Category category);
    /**
     * 更新分类
     */
    boolean updateCategory(Category category);
    /**
     * 删除分类
     */
    boolean deleteCategory(Long id);
}
