package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.mapper.CategoryMapper;
import com.zhp.flea_market.model.entity.Category;
import com.zhp.flea_market.model.vo.CategoryVO;
import com.zhp.flea_market.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 获取所有分类信息
     */
    @Override
    public List<Category> getCategoryList() {
        return this.list();
    }

    /**
     * 查询所有二手物品类别及其商品数量
     */
    @Override
    public List<CategoryVO> getCategoryListWithProductCount() {
        // 获取所有分类
        List<Category> categories = this.list();
        
        // 获取每个分类下的商品数量
        Map<Long, Long> productCountMap = baseMapper.selectProductCountGroupByCategory();
        
        // 转换为CategoryVO并设置商品数量
        List<CategoryVO> categoryVOs = new ArrayList<>();
        for (Category category : categories) {
            CategoryVO categoryVO = new CategoryVO();
            BeanUtils.copyProperties(category, categoryVO);
            
            // 设置该分类下的商品数量
            Long productCount = productCountMap.getOrDefault(category.getId(), 0L);
            categoryVO.setProductCount(productCount);
            
            categoryVOs.add(categoryVO);
        }
        
        return categoryVOs;
    }

    /**
     * 添加分类信息
     */
    @Override
    public boolean addCategory(Category category) {
        // 添加分类信息
        return this.save(category);
    }

    /**
     * 更新分类信息
     */
    @Override
    public boolean updateCategory(Category category) {
        // 更新分类信息
        return this.updateById(category);
    }

    /**
     * 删除分类信息
     */
    @Override
    public boolean deleteCategory(Long id) {
        // 删除指定ID的分类
        return this.removeById(id);
    }
}