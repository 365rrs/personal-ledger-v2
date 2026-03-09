package com.ledger.dto;

import lombok.Data;

/**
 * 每日支出DTO
 *
 * @author personal-ledger
 * @date 2026-01-13
 */
@Data
public class BillDailyExpenseQueryDTO {
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份
     */
    private Integer month;
}
