package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 支付渠道实体类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "bill_payment_channel")
public class BillPaymentChannel extends BaseEntity {
    
    /**
     * 渠道名称
     */
    @TableField(value = "channel_name")
    private String channelName;
    
    /**
     * 渠道类型：CASH-现金，BANK_CARD-银行卡，CREDIT_CARD-信用卡，E_WALLET-电子钱包，OTHER-其他
     */
    @TableField(value = "channel_type")
    private String channelType;
    
    /**
     * 图标
     */
    @TableField(value = "icon")
    private String icon;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    @TableField(value = "enabled")
    private String enabled;
    
    /**
     * 排序序号
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;
}
