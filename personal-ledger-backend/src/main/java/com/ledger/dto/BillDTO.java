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
    
    private String category;
    
    private String subCategory;
    
    private String transactionDesc;
    
    private String paymentChannel;
    
    private String manualRemark;
    
    private String includeInStatistics;
    
    private List<Long> tagIds;
}
