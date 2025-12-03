package com.zhp.flea_market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhp.flea_market.model.entity.PointsRecord;

import java.util.List;

/**
 * 积分记录服务
 */
public interface PointsRecordService extends IService<PointsRecord> {

    /**
     * 创建积分记录
     *
     * @param userId 用户ID
     * @param pointsChange 积分变化值
     * @param changeType 变动类型
     * @param relatedId 关联业务ID
     * @param description 描述
     * @return 是否创建成功
     */
    boolean createPointsRecord(Long userId, Integer pointsChange, Integer changeType, 
                              Long relatedId, String description);

    /**
     * 获取用户的积分记录列表
     *
     * @param userId 用户ID
     * @return 积分记录列表
     */
    List<PointsRecord> getPointsRecordsByUserId(Long userId);

    /**
     * 根据业务ID获取积分记录
     *
     * @param relatedId 关联业务ID
     * @param changeType 变动类型
     * @return 积分记录
     */
    PointsRecord getPointsRecordByRelatedId(Long relatedId, Integer changeType);
}