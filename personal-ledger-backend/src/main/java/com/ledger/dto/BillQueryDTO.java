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
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String paymentChannel;
    
    private String manualEntry;
}
