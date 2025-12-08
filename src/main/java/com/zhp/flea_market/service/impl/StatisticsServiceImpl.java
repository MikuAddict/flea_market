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

    /**
     * 获取月度交易商品排行
     *
     * @param month 月份
     * @param year 年份
     * @param limit 限制数量
     * @return 商品排行列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticsResponse.ProductRankingItem> getMonthlyTopSellingProducts(int month, int year, int limit) {
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

        // 按商品ID分组，统计每个商品的交易次数和金额
        Map<Long, List<TradeRecord>> productGroups = tradeRecords.stream()
                .filter(record -> record.getProductId() != null)
                .collect(Collectors.groupingBy(TradeRecord::getProductId));

        // 计算每个商品的统计信息
        List<StatisticsResponse.ProductRankingItem> result = new ArrayList<>();
        for (Map.Entry<Long, List<TradeRecord>> entry : productGroups.entrySet()) {
            Long productId = entry.getKey();
            List<TradeRecord> records = entry.getValue();

            // 计算交易次数和总金额
            Long tradeCount = (long) records.size();
            BigDecimal tradeAmount = records.stream()
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

            // 获取商品信息
            Product product = productService.getById(productId);
            if (product == null) {
                continue;
            }

            // 创建排行项
            StatisticsResponse.ProductRankingItem item = new StatisticsResponse.ProductRankingItem();
            item.setProductId(productId);
            item.setProductName(product.getProductName());
            item.setTradeCount(tradeCount);
            item.setTradeAmount(tradeAmount);
            item.setImageUrl(product.getImageUrl());

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
     * 获取需求量大商品排行
     *
     * @param limit 限制数量
     * @return 商品排行列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticsResponse.ProductRankingItem> getHighDemandProducts(int limit) {
        // 获取所有已上架商品
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        productQueryWrapper.eq("status", 1); // 1表示已上架
        List<Product> products = productService.list(productQueryWrapper);

        if (CollectionUtils.isEmpty(products)) {
            return new ArrayList<>();
        }

        // 统计每个商品在购物车中的数量
        List<StatisticsResponse.ProductRankingItem> result = new ArrayList<>();
        for (Product product : products) {
            // 获取商品在购物车中的数量
            QueryWrapper<ShoppingCart> cartQueryWrapper = new QueryWrapper<>();
            cartQueryWrapper.eq("product_id", product.getId());
            long cartCount = shoppingCartService.count(cartQueryWrapper);

            // 获取商品的订单数量
            QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
            orderQueryWrapper.eq("product_id", product.getId())
                    .in("status", 0, 1, 2); // 排除已取消的订单
            long orderCount = orderService.count(orderQueryWrapper);

            // 计算需求指数（购物车数量 + 订单数量）
            long demandIndex = cartCount + orderCount;

            // 创建排行项
            StatisticsResponse.ProductRankingItem item = new StatisticsResponse.ProductRankingItem();
            item.setProductId(product.getId());
            item.setProductName(product.getProductName());
            item.setTradeCount(demandIndex); // 使用需求指数作为交易次数
            item.setTradeAmount(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
            item.setImageUrl(product.getImageUrl());

            result.add(item);
        }

        // 按需求指数降序排序
        result.sort((a, b) -> b.getTradeCount().compareTo(a.getTradeCount()));

        // 返回前N项
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取闲置量大商品排行
     *
     * @param limit 限制数量
     * @return 商品排行列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticsResponse.ProductRankingItem> getHighInventoryProducts(int limit) {
        // 获取所有已上架商品
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        productQueryWrapper.eq("status", 1); // 1表示已上架
        productQueryWrapper.orderByDesc("create_time"); // 按创建时间降序，优先显示上架时间长的商品
        List<Product> products = productService.list(productQueryWrapper);

        if (CollectionUtils.isEmpty(products)) {
            return new ArrayList<>();
        }

        // 计算商品上架天数
        List<StatisticsResponse.ProductRankingItem> result = new ArrayList<>();
        for (Product product : products) {
            long listingDays = 0;
            if (product.getCreateTime() != null) {
                long diffInMillis = System.currentTimeMillis() - product.getCreateTime().getTime();
                listingDays = diffInMillis / (1000 * 60 * 60 * 24);
            }

            // 检查商品是否有交易记录
            QueryWrapper<TradeRecord> tradeQueryWrapper = new QueryWrapper<>();
            tradeQueryWrapper.eq("product_id", product.getId());
            long tradeCount = tradeRecordService.count(tradeQueryWrapper);

            // 只有上架时间长且交易少的商品才被认为是闲置商品
            if (listingDays >= 30 && tradeCount == 0) { // 上架超过30天且无交易记录
                // 创建排行项
                StatisticsResponse.ProductRankingItem item = new StatisticsResponse.ProductRankingItem();
                item.setProductId(product.getId());
                item.setProductName(product.getProductName());
                item.setTradeCount(listingDays); // 使用上架天数作为"交易次数"的替代指标
                item.setTradeAmount(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
                item.setImageUrl(product.getImageUrl());

                result.add(item);
            }
        }

        // 按上架天数降序排序
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

        // 获取月度交易商品排行（当前月份）
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        response.setMonthlyProductRanking(getMonthlyTopSellingProducts(currentMonth, currentYear, 10));

        // 获取活跃用户排行
        response.setActiveUserRanking(getActiveUsersRanking(10, startDate, endDate));

        // 获取需求量大商品排行
        response.setHighDemandRanking(getHighDemandProducts(10));

        // 获取闲置量大商品排行
        response.setHighInventoryRanking(getHighInventoryProducts(10));

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
     * 获取商品交易统计
     *
     * @param productId 商品ID
     * @return 商品交易统计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatisticsResponse getProductTradeStatistics(Long productId) {
        StatisticsResponse response = new StatisticsResponse();
        response.setStatisticsType("商品交易统计");
        response.setStatisticsTime(new Date());

        // 获取商品的交易记录
        QueryWrapper<TradeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
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