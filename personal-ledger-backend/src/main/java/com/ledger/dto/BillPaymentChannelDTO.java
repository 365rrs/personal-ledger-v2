package com.ledger.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 支付渠道 DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillPaymentChannelDTO {
    
    private Long id;
    
    @NotBlank(message = "渠道名称不能为空")
    @Size(max = 50, message = "渠道名称长度不能超过 50 个字符")
    private String channelName;
    
    /**
     * 渠道类型：CASH-现金，BANK_CARD-银行卡，CREDIT_CARD-信用卡，E_WALLET-电子钱包，OTHER-其他
     */
    private String channelType;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    private String enabled;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 分页参数：当前页
     */
    private Integer current;
    
    /**
     * 分页参数：每页条数
     */
    private Integer size;
}
