package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhp.flea_market.mapper.CategoryMapper;
import com.zhp.flea_market.mapper.OrderMapper;
import com.zhp.flea_market.mapper.ProductMapper;
import com.zhp.flea_market.mapper.UserMapper;
import com.zhp.flea_market.model.dto.response.MonthlyStatisticsResponse;
import com.zhp.flea_market.model.entity.Category;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务实现类
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    @Override
    public MonthlyStatisticsResponse getMonthlyStatistics(Integer year, Integer month) {
        MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
        response.setYear(year);
        response.setMonth(month);
        
        // 获取所有统计信息
        response.setMonthlyCategoryRanking(getMonthlyCategoryRanking(year, month).getMonthlyCategoryRanking());
        response.setActiveUserRanking(getMonthlyActiveUserRanking(year, month).getActiveUserRanking());
        response.setCategoryOnSaleInventory(getMonthlyCategoryOnSaleInventory(year, month).getCategoryOnSaleInventory());
        response.setCategorySoldInventory(getMonthlyCategorySoldInventory(year, month).getCategorySoldInventory());
        
        return response;
    }

    @Override
    public MonthlyStatisticsResponse getMonthlyCategoryRanking(Integer year, Integer month) {
        MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
        response.setYear(year);
        response.setMonth(month);
        
        // 获取指定月份的开始和结束时间
        YearMonth yearMonth = YearMonth.of(year, month);
        Date startDate = Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        
        // 查询该月内完成的订单（状态为2-已完成）
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("status", 2) // 已完成订单
                   .ge("finish_time", startDate)
                   .le("finish_time", endDate)
                   .eq("deleted", 0);
        List<Order> completedOrders = orderMapper.selectList(orderWrapper);
        
        // 获取订单对应的商品信息
        List<Long> productIds = completedOrders.stream()
                .map(Order::getProductId)
                .distinct()
                .collect(Collectors.toList());
                
        if (productIds.isEmpty()) {
            response.setMonthlyCategoryRanking(new ArrayList<>());
            return response;
        }
        
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.in("id", productIds).eq("deleted", 0);
        List<Product> products = productMapper.selectList(productWrapper);
        
        // 按分类统计交易次数和金额
        Map<Long, CategoryStats> categoryStatsMap = new HashMap<>();
        
        for (Order order : completedOrders) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(order.getProductId()))
                    .findFirst()
                    .orElse(null);
                    
            if (product != null) {
                Long categoryId = product.getCategoryId();
                CategoryStats stats = categoryStatsMap.getOrDefault(categoryId, new CategoryStats());
                stats.tradeCount++;
                if (product.getPrice() != null) {
                    stats.totalAmount += product.getPrice().doubleValue();
                }
                categoryStatsMap.put(categoryId, stats);
            }
        }
        
        // 获取分类信息
        List<Category> categories = categoryMapper.selectList(new QueryWrapper<Category>().eq("deleted", 0));
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        
        // 转换为响应格式
        List<MonthlyStatisticsResponse.CategoryRankingItem> rankingItems = categoryStatsMap.entrySet().stream()
                .map(entry -> {
                    MonthlyStatisticsResponse.CategoryRankingItem item = new MonthlyStatisticsResponse.CategoryRankingItem();
                    item.setCategoryId(entry.getKey());
                    item.setCategoryName(categoryNameMap.getOrDefault(entry.getKey(), "未知分类"));
                    item.setTradeCount(entry.getValue().tradeCount);
                    item.setTotalAmount(entry.getValue().totalAmount);
                    return item;
                })
                .sorted((a, b) -> Long.compare(b.getTradeCount(), a.getTradeCount())) // 按交易次数降序排序
                .collect(Collectors.toList());
        
        response.setMonthlyCategoryRanking(rankingItems);
        return response;
    }

    @Override
    public MonthlyStatisticsResponse getMonthlyActiveUserRanking(Integer year, Integer month) {
        MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
        response.setYear(year);
        response.setMonth(month);
        
        // 获取指定月份的开始和结束时间
        YearMonth yearMonth = YearMonth.of(year, month);
        Date startDate = Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        
        // 查询该月内完成的订单（状态为2-已完成）
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("status", 2) // 已完成订单
                   .ge("finish_time", startDate)
                   .le("finish_time", endDate)
                   .eq("deleted", 0);
        List<Order> completedOrders = orderMapper.selectList(orderWrapper);
        
        // 获取订单对应的商品信息
        List<Long> productIds = completedOrders.stream()
                .map(Order::getProductId)
                .distinct()
                .collect(Collectors.toList());
                
        if (productIds.isEmpty()) {
            response.setActiveUserRanking(new ArrayList<>());
            return response;
        }
        
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.in("id", productIds).eq("deleted", 0);
        List<Product> products = productMapper.selectList(productWrapper);
        
        // 按用户统计交易次数和金额（买家）
        Map<Long, UserStats> userStatsMap = new HashMap<>();
        
        for (Order order : completedOrders) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(order.getProductId()))
                    .findFirst()
                    .orElse(null);
                    
            if (product != null) {
                Long buyerId = order.getBuyerId();
                UserStats stats = userStatsMap.getOrDefault(buyerId, new UserStats());
                stats.tradeCount++;
                if (product.getPrice() != null) {
                    stats.totalAmount += product.getPrice().doubleValue();
                }
                userStatsMap.put(buyerId, stats);
            }
        }
        
        // 获取用户信息
        List<Long> userIds = new ArrayList<>(userStatsMap.keySet());
        List<User> users = userMapper.selectList(new QueryWrapper<User>().in("id", userIds).eq("deleted", 0));
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // 转换为响应格式
        List<MonthlyStatisticsResponse.UserRankingItem> rankingItems = userStatsMap.entrySet().stream()
                .map(entry -> {
                    MonthlyStatisticsResponse.UserRankingItem item = new MonthlyStatisticsResponse.UserRankingItem();
                    item.setUserId(entry.getKey());
                    User user = userMap.get(entry.getKey());
                    if (user != null) {
                        item.setUserName(user.getUserName());
                        item.setAvatar(user.getUserAvatar());
                    } else {
                        item.setUserName("未知用户");
                        item.setAvatar("");
                    }
                    item.setTradeCount(entry.getValue().tradeCount);
                    item.setTotalAmount(entry.getValue().totalAmount);
                    return item;
                })
                .sorted((a, b) -> Long.compare(b.getTradeCount(), a.getTradeCount())) // 按交易次数降序排序
                .collect(Collectors.toList());
        
        response.setActiveUserRanking(rankingItems);
        return response;
    }

    @Override
    public MonthlyStatisticsResponse getMonthlyCategoryOnSaleInventory(Integer year, Integer month) {
        MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
        response.setYear(year);
        response.setMonth(month);
        
        // 获取指定月份的开始和结束时间
        YearMonth yearMonth = YearMonth.of(year, month);
        Date startDate = Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        
        // 查询该月内状态为1（已通过）的商品
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("status", 1) // 已通过（在售）
                     .ge("create_time", startDate)
                     .le("create_time", endDate)
                     .eq("deleted", 0);
        List<Product> onSaleProducts = productMapper.selectList(productWrapper);
        
        // 按分类统计在售商品数量
        Map<Long, Long> categoryCountMap = onSaleProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategoryId, Collectors.counting()));
        
        // 获取分类信息
        List<Category> categories = categoryMapper.selectList(new QueryWrapper<Category>().eq("deleted", 0));
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        
        // 转换为响应格式
        List<MonthlyStatisticsResponse.CategoryInventoryItem> inventoryItems = categoryCountMap.entrySet().stream()
                .map(entry -> {
                    MonthlyStatisticsResponse.CategoryInventoryItem item = new MonthlyStatisticsResponse.CategoryInventoryItem();
                    item.setCategoryId(entry.getKey());
                    item.setCategoryName(categoryNameMap.getOrDefault(entry.getKey(), "未知分类"));
                    item.setItemCount(entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare(b.getItemCount(), a.getItemCount())) // 按数量降序排序
                .collect(Collectors.toList());
        
        response.setCategoryOnSaleInventory(inventoryItems);
        return response;
    }

    @Override
    public MonthlyStatisticsResponse getMonthlyCategorySoldInventory(Integer year, Integer month) {
        MonthlyStatisticsResponse response = new MonthlyStatisticsResponse();
        response.setYear(year);
        response.setMonth(month);
        
        // 获取指定月份的开始和结束时间
        YearMonth yearMonth = YearMonth.of(year, month);
        Date startDate = Date.from(yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        
        // 查询该月内状态为3（已售出）的商品
        QueryWrapper<Product> productWrapper = new QueryWrapper<>();
        productWrapper.eq("status", 3) // 已售出
                     .ge("update_time", startDate)
                     .le("update_time", endDate)
                     .eq("deleted", 0);
        List<Product> soldProducts = productMapper.selectList(productWrapper);
        
        // 按分类统计已售商品数量
        Map<Long, Long> categoryCountMap = soldProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategoryId, Collectors.counting()));
        
        // 获取分类信息
        List<Category> categories = categoryMapper.selectList(new QueryWrapper<Category>().eq("deleted", 0));
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        
        // 转换为响应格式
        List<MonthlyStatisticsResponse.CategoryInventoryItem> inventoryItems = categoryCountMap.entrySet().stream()
                .map(entry -> {
                    MonthlyStatisticsResponse.CategoryInventoryItem item = new MonthlyStatisticsResponse.CategoryInventoryItem();
                    item.setCategoryId(entry.getKey());
                    item.setCategoryName(categoryNameMap.getOrDefault(entry.getKey(), "未知分类"));
                    item.setItemCount(entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare(b.getItemCount(), a.getItemCount())) // 按数量降序排序
                .collect(Collectors.toList());
        
        response.setCategorySoldInventory(inventoryItems);
        return response;
    }
    
    // 辅助类：分类统计信息
    private static class CategoryStats {
        long tradeCount = 0;
        double totalAmount = 0.0;
    }
    
    // 辅助类：用户统计信息
    private static class UserStats {
        long tradeCount = 0;
        double totalAmount = 0.0;
    }
}