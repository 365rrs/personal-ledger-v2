package com.ledger.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 导入明细VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillImportDetailVO {
    
    private Long id;
    private Long importRecordId;
    private String type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionTime;
    private String importStatus;
    private String duplicateStatus;
    private Long duplicateLedgerId;
    private String convertStatus;
    private String convertErrorMessage;
    private String errorMessage;
    private Long ledgerId;
}
