package com.ledger.vo;

import lombok.Data;

/**
 * 支付渠道 VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillPaymentChannelVO {
    
    private Long id;
    private String channelName;
    private String channelType;
    private String icon;
    private String enabled;
    private Integer sortOrder;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
}
