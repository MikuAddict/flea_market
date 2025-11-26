package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.mapper.CategoryMapper;
import com.zhp.flea_market.model.entity.Category;
import com.zhp.flea_market.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 获取所有分类信息
     * @return
     */
    @Override
    public List<Category> getCategoryList() {
        return this.list();
    }

    /**
     * 添加分类信息
     * @param category
     * @return
     */
    @Override
    public boolean addCategory(Category category) {
        // 添加分类信息
        return this.save(category);
    }

    /**
     * 更新分类信息
     * @param category
     * @return
     */
    @Override
    public boolean updateCategory(Category category) {
        // 更新分类信息
        return this.updateById(category);
    }

    /**
     * 删除分类信息
     * @param id
     * @return
     */
    @Override
    public boolean deleteCategory(Long id) {
        // 删除指定ID的分类
        return this.removeById(id);
    }
}