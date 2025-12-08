package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhp.flea_market.model.dto.response.StatisticsResponse;
import com.zhp.flea_market.model.entity.*;
import com.zhp.flea_market.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private TradeRecordService tradeRecordService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取月度交易二手物品分类排行
     *
     * @param month 月份
     * @param year 年份
     * @param limit 限制数量
     * @return 二手物品分类排行列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticsResponse.ProductRankingItem> getMonthlyTopSellingCategories(int month, int year, int limit) {
        // 计算开始和结束日期
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date endDate = calendar.getTime();

        // 获取指定时间范围内的交易记录
        QueryWrapper<TradeRecord> queryWrapper = tradeRecordService.getQueryWrapper(null, null, null, startDate, endDate);
        List<TradeRecord> tradeRecords = tradeRecordService.list(queryWrapper);

        if (CollectionUtils.isEmpty(tradeRecords)) {
            return new ArrayList<>();
        }

        // 获取所有分类
        List<Category> categories = categoryService.getCategoryList();
        if (CollectionUtils.isEmpty(categories)) {
            return new ArrayList<>();
        }

        // 按分类统计交易物品数量
        List<StatisticsResponse.ProductRankingItem> result = new ArrayList<>();
        for (Category category : categories) {
            // 统计该分类下的交易物品数量
            long categoryTradeCount = tradeRecords.stream()
                    .filter(record -> record.getProductId() != null)
                    .map(record -> {
                        Product product = productService.getById(record.getProductId());
                        return product != null && product.getCategoryId() != null && 
                               product.getCategoryId().equals(category.getId()) ? 1 : 0;
                    })
                    .reduce(0, Integer::sum);

            // 计算该分类下的交易总金额
            BigDecimal categoryTradeAmount = tradeRecords.stream()
                    .filter(record -> record.getProductId() != null)
                    .map(record -> {
                        Product product = productService.getById(record.getProductId());
                        if (product != null && product.getCategoryId() != null && 
                            product.getCategoryId().equals(category.getId()) && 
                            record.getOrderId() != null) {
                            try {
                                Order order = orderService.getById(record.getOrderId());
                                if (order != null && order.getAmount() != null) {
                                    return order.getAmount();
                                }
                            } catch (Exception e) {
                                // 忽略获取订单失败的情况
                            }
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 创建分类排行项
            StatisticsResponse.ProductRankingItem item = new StatisticsResponse.ProductRankingItem();
            item.setProductId(category.getId()); // 使用分类ID作为产品ID
            item.setProductName(category.getName()); // 使用分类名称作为产品名称
            item.setTradeCount(categoryTradeCount); // 分类下的物品交易数量
            item.setTradeAmount(categoryTradeAmount); // 分类下的交易总金额
            item.setImageUrl(""); // 分类没有图片，留空

            result.add(item);
        }

        // 按分类下物品交易数量降序排序，如果数量相同则按金额排序
        result.sort((a, b) -> {
            int countCompare = b.getTradeCount().compareTo(a.getTradeCount());
            if (countCompare != 0) {
                return countCompare;
            }
            return b.getTradeAmount().compareTo(a.getTradeAmount());
        });

        // 返回前N项
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取活跃用户排行
     *
     * @param limit 限制数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户排行列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticsResponse.UserRankingItem> getActiveUsersRanking(int limit, Date startDate, Date endDate) {
        // 获取指定时间范围内的交易记录
        QueryWrapper<TradeRecord> queryWrapper = tradeRecordService.getQueryWrapper(null, null, null, startDate, endDate);
        List<TradeRecord> tradeRecords = tradeRecordService.list(queryWrapper);

        if (CollectionUtils.isEmpty(tradeRecords)) {
            return new ArrayList<>();
        }

        // 按用户ID分组，统计每个用户（买家和卖家）的交易次数和金额
        Map<Long, List<TradeRecord>> userBuyerGroups = tradeRecords.stream()
                .filter(record -> record.getBuyerId() != null)
                .collect(Collectors.groupingBy(TradeRecord::getBuyerId));

        Map<Long, List<TradeRecord>> userSellerGroups = tradeRecords.stream()
                .filter(record -> record.getSellerId() != null)
                .collect(Collectors.groupingBy(TradeRecord::getSellerId));

        // 合并买卖双方的交易记录
        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(userBuyerGroups.keySet());
        allUserIds.addAll(userSellerGroups.keySet());

        // 计算每个用户的统计信息
        List<StatisticsResponse.UserRankingItem> result = new ArrayList<>();
        for (Long userId : allUserIds) {
            List<TradeRecord> buyerRecords = userBuyerGroups.getOrDefault(userId, new ArrayList<>());
            List<TradeRecord> sellerRecords = userSellerGroups.getOrDefault(userId, new ArrayList<>());

            // 计算总交易次数和金额
            Long tradeCount = (long) (buyerRecords.size() + sellerRecords.size());
            
            BigDecimal buyerAmount = buyerRecords.stream()
                    .map(record -> {
                        if (record.getOrderId() != null) {
                            try {
                                Order order = orderService.getById(record.getOrderId());
                                if (order != null && order.getAmount() != null) {
                                    return order.getAmount();
                                }
                            } catch (Exception e) {
                                // 忽略获取订单失败的情况
                            }
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal sellerAmount = sellerRecords.stream()
                    .map(record -> {
                        if (record.getOrderId() != null) {
                            try {
                                Order order = orderService.getById(record.getOrderId());
                                if (order != null && order.getAmount() != null) {
                                    return order.getAmount();
                                }
                            } catch (Exception e) {
                                // 忽略获取订单失败的情况
                            }
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalAmount = buyerAmount.add(sellerAmount);

            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                continue;
            }

            // 创建排行项
            StatisticsResponse.UserRankingItem item = new StatisticsResponse.UserRankingItem();
            item.setUserId(userId);
            item.setUserName(user.getUserName());
            item.setTradeCount(tradeCount);
            item.setTradeAmount(totalAmount);
            item.setAvatar(user.getUserAvatar());

            result.add(item);
        }

        // 按交易次数降序排序，如果次数相同则按金额排序
        result.sort((a, b) -> {
            int countCompare = b.getTradeCount().compareTo(a.getTradeCount());
            if (countCompare != 0) {
                return countCompare;
            }
            return b.getTradeAmount().compareTo(a.getTradeAmount());
        });

        // 返回前N项
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取需求量大二手物品分类排行（需求量 = 在售物品数量 / 已售物品数量）
     *
     * @param limit 限制数量
     * @return 二手物品分类排行列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticsResponse.ProductRankingItem> getHighDemandCategories(int limit) {
        // 获取所有已上架二手物品（在售物品）
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        productQueryWrapper.eq("status", 1); // 1表示已上架
        List<Product> products = productService.list(productQueryWrapper);

        if (CollectionUtils.isEmpty(products)) {
            return new ArrayList<>();
        }

        // 获取所有分类
        List<Category> categories = categoryService.getCategoryList();
        if (CollectionUtils.isEmpty(categories)) {
            return new ArrayList<>();
        }

        // 按分类统计需求量（在售物品数量 / 已售物品数量）
        List<StatisticsResponse.ProductRankingItem> result = new ArrayList<>();
        for (Category category : categories) {
            // 统计该分类下的在售物品数量
            long categoryOnSaleCount = products.stream()
                    .filter(product -> product.getCategoryId() != null && 
                                      product.getCategoryId().equals(category.getId()))
                    .count();

            // 统计该分类下的已售物品数量
            long categorySoldCount = products.stream()
                    .filter(product -> product.getCategoryId() != null && 
                                      product.getCategoryId().equals(category.getId()))
                    .mapToLong(product -> {
                        QueryWrapper<TradeRecord> tradeQueryWrapper = new QueryWrapper<>();
                        tradeQueryWrapper.eq("product_id", product.getId())
                                .eq("trade_status", 1); // 交易成功的记录
                        return tradeRecordService.count(tradeQueryWrapper);
                    })
                    .sum();

            // 计算分类需求量（在售物品数量 / 已售物品数量，避免除零）
            double categoryDemandRatio = categorySoldCount > 0 ? 
                    (double) categoryOnSaleCount / categorySoldCount : 
                    categoryOnSaleCount; // 如果已售为0，则需求量等于在售数量

            // 创建分类排行项
            StatisticsResponse.ProductRankingItem item = new StatisticsResponse.ProductRankingItem();
            item.setProductId(category.getId()); // 使用分类ID作为产品ID
            item.setProductName(category.getName()); // 使用分类名称作为产品名称
            item.setTradeCount((long) categoryDemandRatio); // 分类需求量（取整）
            item.setTradeAmount(new BigDecimal(categoryDemandRatio)); // 存储精确的需求比率
            item.setImageUrl(""); // 分类没有图片，留空

            result.add(item);
        }

        // 按分类需求量降序排序（需求量越高越靠前）
        result.sort((a, b) -> b.getTradeAmount().compareTo(a.getTradeAmount()));

        // 返回前N项
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取闲置量大二手物品分类排行（闲置量 = 在售物品数量与已售物品数量的比值）
     *
     * @param limit 限制数量
     * @return 二手物品分类排行列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticsResponse.ProductRankingItem> getHighInventoryCategories(int limit) {
        // 获取所有已上架二手物品
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        productQueryWrapper.eq("status", 1); // 1表示已上架
        List<Product> products = productService.list(productQueryWrapper);

        if (CollectionUtils.isEmpty(products)) {
            return new ArrayList<>();
        }

        // 获取所有分类
        List<Category> categories = categoryService.getCategoryList();
        if (CollectionUtils.isEmpty(categories)) {
            return new ArrayList<>();
        }

        // 按分类统计闲置物品数量
        List<StatisticsResponse.ProductRankingItem> result = new ArrayList<>();
        for (Category category : categories) {
            // 统计该分类下的闲置物品数量（上架超过30天且无交易记录）
            long categoryInventoryCount = products.stream()
                    .filter(product -> product.getCategoryId() != null && 
                                      product.getCategoryId().equals(category.getId()))
                    .filter(product -> {
                        // 计算上架天数
                        long listingDays = 0;
                        if (product.getCreateTime() != null) {
                            long diffInMillis = System.currentTimeMillis() - product.getCreateTime().getTime();
                            listingDays = diffInMillis / (1000 * 60 * 60 * 24);
                        }

                        // 检查是否有交易记录
                        QueryWrapper<TradeRecord> tradeQueryWrapper = new QueryWrapper<>();
                        tradeQueryWrapper.eq("product_id", product.getId());
                        long tradeCount = tradeRecordService.count(tradeQueryWrapper);

                        // 上架超过30天且无交易记录
                        return listingDays >= 30 && tradeCount == 0;
                    })
                    .count();

            // 创建分类排行项
            StatisticsResponse.ProductRankingItem item = new StatisticsResponse.ProductRankingItem();
            item.setProductId(category.getId()); // 使用分类ID作为产品ID
            item.setProductName(category.getName()); // 使用分类名称作为产品名称
            item.setTradeCount(categoryInventoryCount); // 分类下的闲置物品数量
            item.setTradeAmount(BigDecimal.ZERO); // 闲置排行不涉及金额
            item.setImageUrl(""); // 分类没有图片，留空

            result.add(item);
        }

        // 按分类下闲置物品数量降序排序
        result.sort((a, b) -> b.getTradeCount().compareTo(a.getTradeCount()));

        // 返回前N项
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取综合统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 综合统计信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatisticsResponse getComprehensiveStatistics(Date startDate, Date endDate) {
        StatisticsResponse response = new StatisticsResponse();
        response.setStatisticsType("综合统计");
        response.setStatisticsTime(new Date());

        // 获取总交易金额和数量
        BigDecimal totalTradeAmount = tradeRecordService.getTotalTradeAmount(startDate, endDate);
        Long totalTradeCount = tradeRecordService.getTradeCount(startDate, endDate);
        
        response.setTotalTradeAmount(totalTradeAmount);
        response.setTotalTradeCount(totalTradeCount);

        // 获取月度交易二手物品排行（当前月份）
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        response.setMonthlyProductRanking(getMonthlyTopSellingCategories(currentMonth, currentYear, 10));

        // 获取活跃用户排行
        response.setActiveUserRanking(getActiveUsersRanking(10, startDate, endDate));

        // 获取需求量大二手物品排行
        response.setHighDemandRanking(getHighDemandCategories(10));

        // 获取闲置量大二手物品分类排行
        response.setHighInventoryRanking(getHighInventoryCategories(10));

        return response;
    }

    /**
     * 获取月度统计数据
     *
     * @param month 月份
     * @param year 年份
     * @return 月度统计数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatisticsResponse getMonthlyStatistics(int month, int year) {
        // 计算月份的开始和结束日期
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date endDate = calendar.getTime();

        // 获取综合统计
        StatisticsResponse response = getComprehensiveStatistics(startDate, endDate);
        response.setStatisticsType("月度统计");

        return response;
    }

    /**
     * 获取用户交易统计
     *
     * @param userId 用户ID
     * @return 用户交易统计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatisticsResponse getUserTradeStatistics(Long userId) {
        StatisticsResponse response = new StatisticsResponse();
        response.setStatisticsType("用户交易统计");
        response.setStatisticsTime(new Date());

        // 获取用户的交易记录（作为买家和卖家）
        QueryWrapper<TradeRecord> buyerQueryWrapper = new QueryWrapper<>();
        buyerQueryWrapper.eq("buyer_id", userId);
        List<TradeRecord> buyerRecords = tradeRecordService.list(buyerQueryWrapper);

        QueryWrapper<TradeRecord> sellerQueryWrapper = new QueryWrapper<>();
        sellerQueryWrapper.eq("seller_id", userId);
        List<TradeRecord> sellerRecords = tradeRecordService.list(sellerQueryWrapper);

        // 计算总交易金额和数量
        BigDecimal buyerAmount = buyerRecords.stream()
                .map(record -> {
                    if (record.getOrderId() != null) {
                        try {
                            Order order = orderService.getById(record.getOrderId());
                            if (order != null && order.getAmount() != null) {
                                return order.getAmount();
                            }
                        } catch (Exception e) {
                            // 忽略获取订单失败的情况
                        }
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal sellerAmount = sellerRecords.stream()
                .map(record -> {
                    if (record.getOrderId() != null) {
                        try {
                            Order order = orderService.getById(record.getOrderId());
                            if (order != null && order.getAmount() != null) {
                                return order.getAmount();
                            }
                        } catch (Exception e) {
                            // 忽略获取订单失败的情况
                        }
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalAmount = buyerAmount.add(sellerAmount);
        Long totalCount = (long) (buyerRecords.size() + sellerRecords.size());

        response.setTotalTradeAmount(totalAmount);
        response.setTotalTradeCount(totalCount);

        return response;
    }

    /**
     * 获取二手物品分类交易统计
     *
     * @param categoryId 分类ID
     * @return 二手物品分类交易统计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatisticsResponse getCategoryTradeStatistics(Long categoryId) {
        StatisticsResponse response = new StatisticsResponse();
        response.setStatisticsType("二手物品分类交易统计");
        response.setStatisticsTime(new Date());

        // 获取该分类下的所有二手物品
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        productQueryWrapper.eq("category_id", categoryId);
        List<Product> categoryProducts = productService.list(productQueryWrapper);

        if (CollectionUtils.isEmpty(categoryProducts)) {
            response.setTotalTradeAmount(BigDecimal.ZERO);
            response.setTotalTradeCount(0L);
            return response;
        }

        // 获取该分类下所有物品的交易记录
        List<Long> productIds = categoryProducts.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        QueryWrapper<TradeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("product_id", productIds);
        List<TradeRecord> records = tradeRecordService.list(queryWrapper);

        // 计算总交易金额和数量
        BigDecimal totalAmount = records.stream()
                .map(record -> {
                    if (record.getOrderId() != null) {
                        try {
                            Order order = orderService.getById(record.getOrderId());
                            if (order != null && order.getAmount() != null) {
                                return order.getAmount();
                            }
                        } catch (Exception e) {
                            // 忽略获取订单失败的情况
                        }
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalCount = (long) records.size();

        response.setTotalTradeAmount(totalAmount);
        response.setTotalTradeCount(totalCount);

        return response;
    }
}