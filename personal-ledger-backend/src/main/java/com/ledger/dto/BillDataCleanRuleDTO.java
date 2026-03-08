package com.ledger.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 数据清洗规则 DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@ApiModel("数据清洗规则 DTO")
public class BillDataCleanRuleDTO {

    @ApiModelProperty("主键 ID（更新时必填）")
    private Long id;

    @NotBlank(message = "规则类型不能为空")
    @ApiModelProperty(value = "规则类型", required = true, example = "PAYMENT_CHANNEL")
    private String ruleType;

    @NotBlank(message = "匹配字段不能为空")
    @ApiModelProperty(value = "匹配字段（JSON 格式）", required = true, example = "{\"payment_channel\":\"微信\"}")
    private String matchFields;

    @NotBlank(message = "目标值不能为空")
    @ApiModelProperty(value = "目标值", required = true, example = "微信支付")
    private String targetValue;

    @NotNull(message = "优先级不能为空")
    @ApiModelProperty(value = "优先级", required = true, example = "100")
    private Integer priority;

    @ApiModelProperty(value = "是否启用", example = "1")
    private String enabled;

    @ApiModelProperty(value = "备注说明", example = "微信别名")
    private String remark;
}
