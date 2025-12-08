package com.zhp.flea_market.model.dto.request;

import lombok.Data;

/**
 * 用户状态更新请求（通用方法，支持多种状态变更）
 */
@Data
public class UserAuditRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户状态 (0-待审核, 1-已通过, 2-已拒绝, 3-已禁用)
     */
    private Integer auditStatus;
}