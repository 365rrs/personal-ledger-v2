package com.ledger.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据清洗规则查询 DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@ApiModel("数据清洗规则查询 DTO")
public class BillDataCleanRuleQueryDTO {

    @ApiModelProperty("当前页码")
    private Integer current = 1;

    @ApiModelProperty("每页大小")
    private Integer size = 10;

    @ApiModelProperty("规则类型：PAYMENT_CHANNEL-支付渠道，CATEGORY-分类，MANUAL_REMARK-手工备注")
    private String ruleType;

    @ApiModelProperty("是否启用：0-禁用，1-启用")
    private String enabled;

    @ApiModelProperty("关键字（搜索目标值或备注）")
    private String keywords;
}
