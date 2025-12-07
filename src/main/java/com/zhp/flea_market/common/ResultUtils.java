package com.zhp.flea_market.common;

import java.util.HashMap;

/**
 * 返回工具类
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 错误响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code 错误码
     * @param message 错误消息
     * @return 错误响应
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param message 错误消息
     * @return 错误响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    /**
     * 成功并添加动态参数
     *
     * @param data 返回数据
     * @param hashMap 动态参数
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> BaseResponse<T> successDynamic(T data, HashMap<String,Object> hashMap) {
        return new BaseResponse<>(200, data, "ok", hashMap);
    }
}