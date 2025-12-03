package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}