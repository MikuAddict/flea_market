package com.zhp.flea_market.model.dto.request;

import lombok.Data;

/**
 * 用户审核请求
 */
@Data
public class UserAuditRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 审核状态 (0-待审核, 1-已通过, 2-已拒绝)
     */
    private Integer auditStatus;
    
    /**
     * 审核说明
     */
    private String auditRemark;
}