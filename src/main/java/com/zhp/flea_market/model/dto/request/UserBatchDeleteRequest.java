package com.zhp.flea_market.model.dto.request;

import lombok.Data;

/**
 * 批量删除已拒绝用户请求
 */
@Data
public class UserBatchDeleteRequest {
    
    /**
     * 确认标记
     */
    private boolean confirm;
    
    /**
     * 操作备注
     */
    private String remark;
}