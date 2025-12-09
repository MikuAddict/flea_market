package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.mapper.CategoryMapper;
import com.zhp.flea_market.model.entity.Category;
import com.zhp.flea_market.model.vo.CategoryVO;
import com.zhp.flea_market.service.CategoryService;
import org.springframework.stereotype.Service;

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
        // 获取所有分类及其商品数量
        List<Map<String, Object>> categoryDataList = baseMapper.selectAllCategoriesWithProductCount();
        
        // 转换为CategoryVO列表
        return categoryDataList.stream().map(categoryData -> {
            CategoryVO categoryVO = new CategoryVO();
            categoryVO.setId(((Number) categoryData.get("categoryId")).longValue());
            categoryVO.setName((String) categoryData.get("categoryName"));
            categoryVO.setProductCount(((Number) categoryData.get("productCount")).longValue());
            return categoryVO;
        }).collect(Collectors.toList());
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