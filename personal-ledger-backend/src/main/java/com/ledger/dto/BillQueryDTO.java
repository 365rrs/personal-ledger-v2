package com.ledger.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 账单查询DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillQueryDTO {
    
    @NotNull(message = "当前页不能为空")
    private Integer current;
    
    @NotNull(message = "每页条数不能为空")
    private Integer size;
    
    private String transactionType;
    
    private String category;
    
    private String subCategory;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String paymentChannel;
    
    private String manualEntry;
    
    private String includeInStatistics;
    
    private String amountType;
    
    private String tags;
    
    private java.util.List<Long> tagIds;  // 标签 ID 列表
    
    private String keywords;
    
    private Double minAmount;
    
    private Double maxAmount;
}
