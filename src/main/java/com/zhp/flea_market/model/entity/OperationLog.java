package com.zhp.flea_market.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志实体
 */
@Entity
@Table(name = "operation_log")
@Data
public class OperationLog {

    /**
     * 日志ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 操作用户名
     */
    @Column(name = "user_name")
    private String userName;

    /**
     * 操作IP
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * 操作类型 (1-查询, 2-添加, 3-更新, 4-删除, 5-审核, 6-登录, 7-登出)
     */
    @Column(name = "operation_type")
    private Integer operationType;

    /**
     * 操作类型描述
     */
    @Column(name = "operation_type_desc")
    private String operationTypeDesc;

    /**
     * 操作模块
     */
    @Column(name = "operation_module")
    private String operationModule;

    /**
     * 操作内容
     */
    @Column(name = "operation_content")
    private String operationContent;

    /**
     * 操作结果 (1-成功, 0-失败)
     */
    @Column(name = "operation_result")
    private Integer operationResult;

    /**
     * 操作结果描述
     */
    @Column(name = "operation_result_desc")
    private String operationResultDesc;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "operation_time")
    private Date operationTime;

    /**
     * 请求URI
     */
    @Column(name = "request_uri")
    private String requestUri;

    /**
     * 请求方法
     */
    @Column(name = "request_method")
    private String requestMethod;

    /**
     * 请求参数
     */
    @Column(name = "request_params")
    private String requestParams;

    /**
     * 在保存实体之前设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (operationTime == null) {
            operationTime = new Date();
        }
    }
}