package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.TradeRecord;
import com.zhp.flea_market.model.vo.TradeRecordVO;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易记录服务接口
 */
public interface TradeRecordService extends IService<TradeRecord> {

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
    Long createTradeRecord(Long orderId, Long productId, String productName,
                         Long buyerId, String buyerName, Long sellerId, String sellerName,
                         BigDecimal amount, Integer paymentMethod, String paymentMethodDesc,
                         String remark);

    /**
     * 更新交易记录状态
     *
     * @param tradeRecordId 交易记录ID
     * @param tradeStatus 交易状态
     * @return 是否更新成功
     */
    boolean updateTradeStatus(Long tradeRecordId, Integer tradeStatus);

    /**
     * 关联评价
     *
     * @param tradeRecordId 交易记录ID
     * @param reviewId 评价ID
     * @return 是否更新成功
     */
    boolean linkReview(Long tradeRecordId, Long reviewId);

    /**
     * 获取交易记录详情
     *
     * @param id 交易记录ID
     * @param request HTTP请求
     * @return 交易记录详情
     */
    TradeRecordVO getTradeRecordDetail(Long id, HttpServletRequest request);

    /**
     * 获取买家的交易记录列表
     *
     * @param request HTTP请求
     * @param page 分页参数
     * @return 交易记录VO分页列表
     */
    Page<TradeRecordVO> getBuyerTradeRecords(HttpServletRequest request, Page<TradeRecord> page);

    /**
     * 获取卖家的交易记录列表
     *
     * @param request HTTP请求
     * @param page 分页参数
     * @return 交易记录VO分页列表
     */
    Page<TradeRecordVO> getSellerTradeRecords(HttpServletRequest request, Page<TradeRecord> page);

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
    Page<TradeRecordVO> getAllTradeRecords(Page<TradeRecord> page, Integer tradeStatus, 
                                      Date startDate, Date endDate, HttpServletRequest request);

    /**
     * 获取指定时间范围内的交易总额
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易总额
     */
    BigDecimal getTotalTradeAmount(Date startDate, Date endDate);

    /**
     * 获取指定时间范围内的交易数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 交易数量
     */
    Long getTradeCount(Date startDate, Date endDate);

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
    QueryWrapper<TradeRecord> getQueryWrapper(Long buyerId, Long sellerId, Integer tradeStatus, 
                                           Date startDate, Date endDate);

    /**
     * 验证交易记录权限
     *
     * @param tradeRecord 交易记录
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean validateTradeRecordPermission(TradeRecord tradeRecord, Long userId);
}