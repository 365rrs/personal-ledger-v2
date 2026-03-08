package com.ledger.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据清洗规则 VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@ApiModel("数据清洗规则 VO")
public class BillDataCleanRuleVO {

    @ApiModelProperty("主键 ID")
    private Long id;

    @ApiModelProperty("规则类型")
    private String ruleType;

    @ApiModelProperty("规则类型中文")
    private String ruleTypeName;

    @ApiModelProperty("匹配字段（JSON 格式）")
    private String matchFields;

    @ApiModelProperty("匹配字段解析后的 Map")
    private Map<String, String> matchFieldsMap;

    @ApiModelProperty("目标值")
    private String targetValue;

    @ApiModelProperty("优先级")
    private Integer priority;

    @ApiModelProperty("是否启用：0-禁用，1-启用")
    private String enabled;

    @ApiModelProperty("备注说明")
    private String remark;

    @ApiModelProperty("创建人编码")
    private String creatorCode;

    @ApiModelProperty("更新人编码")
    private String updaterCode;

    @ApiModelProperty("创建人姓名")
    private String creatorName;

    @ApiModelProperty("更新人姓名")
    private String updaterName;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
