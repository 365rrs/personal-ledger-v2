package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据清洗规则实体类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_data_clean_rule")
public class BillDataCleanRule extends BaseEntity {
    
    /**
     * 规则类型：PAYMENT_CHANNEL-支付渠道，CATEGORY-分类，MANUAL_REMARK-手工备注
     */
    private String ruleType;
    
    /**
     * 匹配字段（JSON格式）
     */
    private String matchFields;
    
    /**
     * 目标值
     */
    private String targetValue;
    
    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    private String enabled;
    
    /**
     * 备注说明
     */
    private String remark;
}
