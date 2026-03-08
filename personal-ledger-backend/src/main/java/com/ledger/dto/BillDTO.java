package com.ledger.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 账单 DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillDTO {
    
    private Long id;
    
    @NotNull(message = "交易日期不能为空")
    private LocalDate transactionDate;
    
    private LocalTime transactionTime;
    
    @NotNull(message = "金额类型不能为空")
    private String amountType;
    
    private String transactionType;
    
    private BigDecimal incomeAmount;
    
    private BigDecimal expenseAmount;
    
    /**
     * 分类 ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String category;
    
    /**
     * 二级分类 ID
     */
    private Long subCategoryId;
    
    /**
     * 二级分类名称
     */
    private String subCategory;
    
    private String transactionDesc;
    
    private String paymentChannel;
    
    /**
     * 支付渠道 ID
     */
    private Long paymentChannelId;
    
    private String manualRemark;
    
    private String includeInStatistics;
    
    private List<Long> tagIds;
}
