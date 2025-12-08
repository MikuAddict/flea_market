package com.zhp.flea_market.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品类别实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("category")
public class Category {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 逻辑删除字段
     */
    @TableLogic
    private Integer deleted = 0;
}
