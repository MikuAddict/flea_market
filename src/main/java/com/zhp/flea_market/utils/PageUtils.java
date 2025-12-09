package com.zhp.flea_market.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.vo.TradeRecordVO;
import com.zhp.flea_market.model.entity.TradeRecord;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * 分页工具类
 */
public class PageUtils {

    /**
     * 通用分页查询方法
     *
     * @param service 服务类实例
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @param <T> 实体类型
     * @return 查询结果列表
     */
    public static <T> List<T> getPageResult(IService<T> service, Page<T> page, QueryWrapper<T> queryWrapper) {
        Page<T> resultPage = service.page(page, queryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 通用分页查询方法（带查询条件构建器）
     *
     * @param service 服务类实例
     * @param page 分页参数
     * @param queryBuilder 查询条件构建器
     * @param <T> 实体类型
     * @return 查询结果列表
     */
    public static <T> List<T> getPageResult(IService<T> service, Page<T> page, Consumer<QueryWrapper<T>> queryBuilder) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryBuilder.accept(queryWrapper);
        return getPageResult(service, page, queryWrapper);
    }

    /**
     * TradeRecord专用分页查询方法，可直接转换为TradeRecordVO
     *
     * @param service TradeRecordService实例
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @param converter 转换函数
     * @return TradeRecordVO分页结果
     */
    public static Page<TradeRecordVO> getTradeRecordPageResult(
            IService<TradeRecord> service, 
            Page<TradeRecord> page, 
            QueryWrapper<TradeRecord> queryWrapper,
            Function<TradeRecord, TradeRecordVO> converter) {
        
        Page<TradeRecord> resultPage = service.page(page, queryWrapper);
        
        // 转换为VO对象
        Page<TradeRecordVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        voPage.setRecords(resultPage.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList()));
                
        return voPage;
    }
}