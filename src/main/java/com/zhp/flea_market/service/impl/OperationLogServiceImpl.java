package com.zhp.flea_market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhp.flea_market.common.ErrorCode;
import com.zhp.flea_market.exception.BusinessException;
import com.zhp.flea_market.mapper.OperationLogMapper;
import com.zhp.flea_market.model.entity.OperationLog;
import com.zhp.flea_market.model.entity.User;
import com.zhp.flea_market.service.OperationLogService;
import com.zhp.flea_market.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Autowired
    private UserService userService;

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long logOperation(Long userId, String userName, HttpServletRequest request,
                            Integer operationType, String operationTypeDesc, String operationModule,
                            String operationContent, Integer operationResult, 
                            String operationResultDesc, String errorMessage) {
        // 获取请求信息
        String ipAddress = getClientIpAddress(request);
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        String requestParams = getRequestParams(request);

        // 创建操作日志
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(userId);
        operationLog.setUserName(userName);
        operationLog.setIpAddress(ipAddress);
        operationLog.setOperationType(operationType);
        operationLog.setOperationTypeDesc(operationTypeDesc);
        operationLog.setOperationModule(operationModule);
        operationLog.setOperationContent(operationContent);
        operationLog.setOperationResult(operationResult);
        operationLog.setOperationResultDesc(operationResultDesc);
        operationLog.setErrorMessage(errorMessage);
        operationLog.setRequestUri(requestUri);
        operationLog.setRequestMethod(requestMethod);
        operationLog.setRequestParams(requestParams);

        boolean saved = this.save(operationLog);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存操作日志失败");
        }

        return operationLog.getId();
    }

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
    @Override
    public List<OperationLog> getOperationLogList(Page<OperationLog> page, Long userId, Integer operationType,
                                                 String operationModule, Date startDate, Date endDate,
                                                 HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUserPermitNull(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }

        // 权限校验：只有管理员可以查看所有用户的日志，普通用户只能查看自己的日志
        if (!userService.isAdmin(currentUser) && (userId == null || !userId.equals(currentUser.getId()))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该用户的操作日志");
        }

        QueryWrapper<OperationLog> queryWrapper = getQueryWrapper(userId, operationType, operationModule, startDate, endDate);
        
        Page<OperationLog> resultPage = this.page(page, queryWrapper);
        return resultPage.getRecords();
    }

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
    @Override
    public QueryWrapper<OperationLog> getQueryWrapper(Long userId, Integer operationType,
                                                     String operationModule, Date startDate, Date endDate) {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        
        if (userId != null && userId > 0) {
            queryWrapper.eq("user_id", userId);
        }
        
        if (operationType != null) {
            queryWrapper.eq("operation_type", operationType);
        }
        
        if (StringUtils.hasText(operationModule)) {
            queryWrapper.eq("operation_module", operationModule);
        }
        
        if (startDate != null) {
            queryWrapper.ge("operation_time", startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le("operation_time", endDate);
        }
        
        queryWrapper.orderByDesc("operation_time");
        
        return queryWrapper;
    }

    /**
     * 清理过期日志
     *
     * @param days 保留天数
     * @return 清理数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredLogs(int days) {
        if (days <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "保留天数必须大于0");
        }

        // 计算截止日期
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime - days * 24 * 60 * 60 * 1000L;
        Date expireDate = new Date(expireTime);

        // 删除过期日志
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("operation_time", expireDate);
        
        return this.baseMapper.delete(queryWrapper);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 获取请求参数
     *
     * @param request HTTP请求
     * @return 请求参数字符串
     */
    private String getRequestParams(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        
        request.getParameterMap().forEach((key, values) -> {
            params.append(key).append("=");
            if (values != null && values.length > 0) {
                // 对敏感参数进行脱敏处理
                if ("password".equalsIgnoreCase(key) || "token".equalsIgnoreCase(key)) {
                    params.append("***");
                } else {
                    params.append(String.join(",", values));
                }
            }
            params.append("&");
        });

        // 移除最后一个&
        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }

        return params.toString();
    }
}