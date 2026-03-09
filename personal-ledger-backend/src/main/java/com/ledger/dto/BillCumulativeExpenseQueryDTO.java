package com.ledger.dto;

import lombok.Data;

/**
 * 累计支出查询DTO
 *
 * @author personal-ledger
 * @date 2026-01-13
 */
@Data
public class BillCumulativeExpenseQueryDTO {
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份
     */
    private Integer month;
}
