package com.ledger.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 待支出 DTO (创建/更新请求)
 *
 * @author personal-ledger
 * @date 2025-01-14
 */
@Data
public class PendingExpenseDTO {
    
    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    @Length(max = 100, message = "项目名称长度不能超过100字符")
    private String expenseName;
    
    /**
     * 金额
     */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @DecimalMax(value = "999999.99", message = "金额不能超过999999.99")
    @Digits(integer = 6, fraction = 2, message = "金额格式不正确")
    private BigDecimal amount;
    
    /**
     * 支付日期
     */
    @NotNull(message = "支付日期不能为空")
    private LocalDate paymentDate;
    
    /**
     * 周期：MONTHLY-每月, YEARLY-每年, ONETIME-一次性
     */
    @NotBlank(message = "周期不能为空")
    @Pattern(regexp = "MONTHLY|YEARLY|ONETIME", message = "周期必须是MONTHLY、YEARLY或ONETIME")
    private String period;
    
    /**
     * 计划类型：RIGID-刚性支出, INTENDED-意向计划支出
     */
    @NotBlank(message = "计划类型不能为空")
    @Pattern(regexp = "RIGID|INTENDED", message = "计划类型必须是RIGID或INTENDED")
    private String planType;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 备注
     */
    @Length(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}
