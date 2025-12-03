package com.zhp.flea_market.exception;


import com.zhp.flea_market.common.ErrorCode;

/**
 * 自定义异常类
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;
    
    /**
     * 错误描述
     */
    private final String description;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.description = message;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getMessage();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.description = message;
    }

    public BusinessException(int code, String message, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
