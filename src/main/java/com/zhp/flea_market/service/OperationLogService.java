package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.OperationLog;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 记录操作日志
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @param request HTTP请求
     * @param operationType 操作类型
     * @param operationTypeDesc 操作类型描述
     * @param operationModule 操作模块
     * @param operationContent 操作内容
     * @param operationResult 操作结果 (1-成功, 0-失败)
     * @param operationResultDesc 操作结果描述
     * @param errorMessage 异常信息
     * @return 日志ID
     */
    Long logOperation(Long userId, String userName, HttpServletRequest request,
                     Integer operationType, String operationTypeDesc, String operationModule,
                     String operationContent, Integer operationResult, 
                     String operationResultDesc, String errorMessage);

    /**
     * 获取操作日志列表
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param operationModule 操作模块
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param request HTTP请求
     * @return 操作日志列表
     */
    List<OperationLog> getOperationLogList(Page<OperationLog> page, Long userId, Integer operationType,
                                         String operationModule, Date startDate, Date endDate,
                                         HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param operationModule 操作模块
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 查询条件
     */
    QueryWrapper<OperationLog> getQueryWrapper(Long userId, Integer operationType,
                                              String operationModule, Date startDate, Date endDate);

    /**
     * 清理过期日志
     *
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredLogs(int days);
}