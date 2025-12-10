package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.TradeRecordMapper;
import com.zhp.flea_market.model.entity.Order;
import com.zhp.flea_market.model.entity.Product;
import com.zhp.flea_market.model.entity.TradeRecord;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.model.vo.TradeRecordVO;
import com.zhp.flea_market.service.OrderService;
import com.zhp.flea_market.service.ProductService;
import com.zhp.flea_market.service.TradeRecordService;
import com.zhp.flea_market.service.UserService;
import com.zhp.flea_market.utils.PageUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 交易记录服务实现类
 */
@Service
public class TradeRecordServiceImpl extends ServiceImpl<TradeRecordMapper, TradeRecord> implements TradeRecordService {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    /**
     * 创建交易记录
     *
     * @param orderId 订单ID
     * @param productId 二手物品ID
     * @param productName 二手物品名称
     * @param buyerId 买家ID
     * @param buyerName 买家名称
     * @param sellerId 卖家ID
     * @param sellerName 卖家名称
     * @param amount 交易金额
     * @param paymentMethod 支付方式
     * @param paymentMethodDesc 支付方式描述
     * @param remark 交易备注
     * @return 交易记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public Long createTradeRecord(Long orderId, Long productId, String productName, 
                                Long buyerId, String buyerName, Long sellerId, String sellerName,
                                BigDecimal amount, Integer paymentMethod, String paymentMethodDesc, 
                                String remark) {
        // 参数校验
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID无效");
        }
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "二手物品ID无效");
        }
        if (buyerId == null || buyerId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "买家ID无效");
        }
        if (sellerId == null || sellerId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "卖家ID无效");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "交易金额无效");
        }

        // 创建交易记录
        TradeRecord tradeRecord = new TradeRecord();
        tradeRecord.setOrderId(orderId);
        tradeRecord.setProductId(productId);
        tradeRecord.setBuyerId(buyerId);
        tradeRecord.setSellerId(sellerId);
        tradeRecord.setTradeStatus(1); // 交易成功

        boolean saved = this.save(tradeRecord);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建交易记录失败");
        }

        return tradeRecord.getId();
    }

    /**
     * 更新交易记录状态
     *
     * @param tradeRecordId 交易记录ID
     * @param tradeStatus 交易状态
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTradeStatus(Long tradeRecordId, Integer tradeStatus) {
        // 参数校验
        if (tradeRecordId == null || tradeRecordId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "交易记录ID无效");
        }
        if (tradeStatus == null || tradeStatus < 1 || tradeStatus > 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "交易状态无效");
        }

        // 检查交易记录是否存在
        TradeRecord tradeRecord = this.getById(tradeRecordId);
        if (tradeRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "交易记录不存在");
        }

        // 更新状态
        TradeRecord updateRecord = new TradeRecord();
        updateRecord.setId(tradeRecordId);
        updateRecord.setTradeStatus(tradeStatus);

        return this.updateById(updateRecord);
    }

    /**
     * 关联评价
     *
     * @param tradeRecordId 交易记录ID
     * @param reviewId 评价ID
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean linkReview(Long tradeRecordId, Long reviewId) {
        // 参数校验
        if (tradeRecordId == null || tradeRecordId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "交易记录ID无效");
        }
        if (reviewId == null || reviewId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价ID无效");
        }

        // 检查交易记录是否存在
        TradeRecord tradeRecord = this.getById(tradeRecordId);
        if (tradeRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "交易记录不存在");
        }

        // 更新交易记录状态为已完成评价
        TradeRecord updateRecord = new TradeRecord();
        updateRecord.setId(tradeRecordId);
        updateRecord.setTradeStatus(2); // 已完成评价

        return this.updateById(updateRecord);
    }

    /**
     * 获取交易记录详情
     *
     * @param id 交易记录ID
     * @param request HTTP请求
     * @return 交易记录详情
     */
    @Override
    public TradeRecordVO getTradeRecordDetail(Long id, HttpServletRequest request) {
        // 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "交易记录ID无效");
        }

        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        // 检查交易记录是否存在
        TradeRecord tradeRecord = this.getById(id);
        if (tradeRecord == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "交易记录不存在");
        }

        // 权限校验：只能查看自己的交易记录，或管理员可以查看所有记录
        if (!userService.isAdmin(currentUser) && 
            !validateTradeRecordPermission(tradeRecord, currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该交易记录");
        }

        // 转换为VO对象

        return convertToTradeRecordVO(tradeRecord);
    }

    /**
     * 获取买家的交易记录列表
     *
     * @param request HTTP请求
     * @param page 分页参数
     * @return 交易记录VO分页列表
     */
    @Override
    public Page<TradeRecordVO> getBuyerTradeRecords(HttpServletRequest request, Page<TradeRecord> page) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        QueryWrapper<TradeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buyer_id", currentUser.getId());
        queryWrapper.orderByDesc("trade_time");

        return PageUtils.getTradeRecordPageResult(this, page, queryWrapper, this::convertToTradeRecordVO);
    }

    /**
     * 获取卖家的交易记录列表
     *
     * @param request HTTP请求
     * @param page 分页参数
     * @return 交易记录VO分页列表
     */
    @Override
    public Page<TradeRecordVO> getSellerTradeRecords(HttpServletRequest request, Page<TradeRecord> page) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        QueryWrapper<TradeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("seller_id", currentUser.getId());
        queryWrapper.orderByDesc("trade_time");

        return PageUtils.getTradeRecordPageResult(this, page, queryWrapper, this::convertToTradeRecordVO);
    }

    /**
     * 获取所有交易记录（管理员权限）
     *
     * @param page 分页参数
     * @param tradeStatus 交易状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 交易记录VO分页列表
     */
    @Override
    public Page<TradeRecordVO> getAllTradeRecords(Page<TradeRecord> page, Integer tradeStatus, 
                                             Date startDate, Date endDate, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        // 权限校验：只有管理员可以查看所有交易记录
        if (!userService.isAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看所有交易记录");
        }

        QueryWrapper<TradeRecord> queryWrapper = getQueryWrapper(null, null, tradeStatus, startDate, endDate);
        
        return PageUtils.getTradeRecordPageResult(this, page, queryWrapper, this::convertToTradeRecordVO);
    }

    /**
     * 获取指定时间范围内的交易总额（积分兑换交易不计入金额）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易总额
     */
    @Override
    public BigDecimal getTotalTradeAmount(Date startDate, Date endDate) {
        QueryWrapper<TradeRecord> queryWrapper = getQueryWrapper(null, null, null, startDate, endDate);
        
        List<TradeRecord> records = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (TradeRecord record : records) {
            if (record.getOrderId() != null) {
                try {
                    Order order = orderService.getById(record.getOrderId());
                    Product product = productService.getById(order.getProductId());
                    // 排除积分兑换交易（支付方式为2）
                    if (product.getPrice() != null && product.getPaymentMethod() != 2) {
                        totalAmount = totalAmount.add(product.getPrice());
                    }
                } catch (Exception ignored) {
                }
            }
        }
        
        return totalAmount;
    }

    /**
     * 获取指定时间范围内的交易数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易数量
     */
    @Override
    public Long getTradeCount(Date startDate, Date endDate) {
        QueryWrapper<TradeRecord> queryWrapper = getQueryWrapper(null, null, null, startDate, endDate);
        return this.count(queryWrapper);
    }

    /**
     * 获取查询条件
     *
     * @param buyerId 买家ID
     * @param sellerId 卖家ID
     * @param tradeStatus 交易状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 查询条件
     */
    @Override
    public QueryWrapper<TradeRecord> getQueryWrapper(Long buyerId, Long sellerId, Integer tradeStatus, 
                                                  Date startDate, Date endDate) {
        QueryWrapper<TradeRecord> queryWrapper = new QueryWrapper<>();
        
        if (buyerId != null && buyerId > 0) {
            queryWrapper.eq("buyer_id", buyerId);
        }
        
        if (sellerId != null && sellerId > 0) {
            queryWrapper.eq("seller_id", sellerId);
        }
        
        if (tradeStatus != null) {
            queryWrapper.eq("trade_status", tradeStatus);
        }
        
        if (startDate != null) {
            queryWrapper.ge("trade_time", startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le("trade_time", endDate);
        }
        
        queryWrapper.orderByDesc("trade_time");
        
        return queryWrapper;
    }

    /**
     * 验证交易记录权限
     *
     * @param tradeRecord 交易记录
     * @param userId 用户ID
     * @return 是否有权限
     */
    @Override
    public boolean validateTradeRecordPermission(TradeRecord tradeRecord, Long userId) {
        return (tradeRecord.getBuyerId() != null && tradeRecord.getBuyerId().equals(userId)) ||
               (tradeRecord.getSellerId() != null && tradeRecord.getSellerId().equals(userId));
    }

    /**
     * 将交易记录实体转换为VO对象
     *
     * @param tradeRecord 交易记录实体
     * @return 交易记录VO对象
     */
    private TradeRecordVO convertToTradeRecordVO(TradeRecord tradeRecord) {
        TradeRecordVO tradeRecordVO = new TradeRecordVO();
        
        // 复制基本属性
        tradeRecordVO.setId(tradeRecord.getId());
        tradeRecordVO.setOrderId(tradeRecord.getOrderId());
        tradeRecordVO.setProductId(tradeRecord.getProductId());
        tradeRecordVO.setBuyerId(tradeRecord.getBuyerId());
        tradeRecordVO.setSellerId(tradeRecord.getSellerId());
        tradeRecordVO.setTradeTime(tradeRecord.getTradeTime());
        tradeRecordVO.setTradeStatus(tradeRecord.getTradeStatus());
        tradeRecordVO.setTradeStatusDesc(getTradeStatusDesc(tradeRecord.getTradeStatus()));

        // 获取订单信息以获取金额
        if (tradeRecord.getOrderId() != null) {
            try {
                Order order = orderService.getById(tradeRecord.getOrderId());
                Product product = productService.getById(order.getProductId());
                tradeRecordVO.setAmount(product.getPrice());
            } catch (Exception e) {
                tradeRecordVO.setAmount(null);
            }
        }
        
        // 获取二手物品信息
        if (tradeRecord.getProductId() != null) {
            try {
                Product product = productService.getById(tradeRecord.getProductId());
                if (product != null) {
                    tradeRecordVO.setProductName(product.getProductName());
                }
            } catch (Exception e) {
                // 如果获取二手物品失败，二手物品名称设为null
                tradeRecordVO.setProductName(null);
            }
        }
        
        // 获取买家信息
        if (tradeRecord.getBuyerId() != null) {
            try {
                User buyer = userService.getById(tradeRecord.getBuyerId());
                if (buyer != null) {
                    tradeRecordVO.setBuyerName(buyer.getUserName());
                }
            } catch (Exception e) {
                // 如果获取买家失败，买家名称设为null
                tradeRecordVO.setBuyerName(null);
            }
        }
        
        // 获取卖家信息
        if (tradeRecord.getSellerId() != null) {
            try {
                User seller = userService.getById(tradeRecord.getSellerId());
                if (seller != null) {
                    tradeRecordVO.setSellerName(seller.getUserName());
                }
            } catch (Exception e) {
                // 如果获取卖家失败，卖家名称设为null
                tradeRecordVO.setSellerName(null);
            }
        }
        
        return tradeRecordVO;
    }

    /**
     * 获取交易状态描述
     *
     * @param tradeStatus 交易状态
     * @return 状态描述
     */
    private String getTradeStatusDesc(Integer tradeStatus) {
        return switch (tradeStatus) {
            case 1 -> "交易成功";
            case 2 -> "已完成评价";
            case 3 -> "已退款";
            default -> "未知状态";
        };
    }
}