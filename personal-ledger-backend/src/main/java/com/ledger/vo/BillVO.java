package com.ledger.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 账单VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillVO {
    
    private Long id;
    private LocalDate transactionDate;
    private LocalTime transactionTime;
    private BigDecimal incomeAmount;
    private BigDecimal expenseAmount;
    private String amountType;
    private String transactionType;
    private String transactionDesc;
    private String paymentChannel;
    private String category;
    private String subCategory;
    private String manualRemark;
    private String includeInStatistics;
    private String manualEntry;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
