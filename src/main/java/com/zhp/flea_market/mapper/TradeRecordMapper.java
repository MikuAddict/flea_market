package com.zhp.flea_market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhp.flea_market.model.entity.TradeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易记录 Mapper
 */
@Mapper
public interface TradeRecordMapper extends BaseMapper<TradeRecord> {
}