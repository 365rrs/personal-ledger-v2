package com.ledger.dto;

import lombok.Data;

/**
 * 分类统计查询DTO
 *
 * @author personal-ledger
 * @date 2026-01-13
 */
@Data
public class BillCategoryStatisticsQueryDTO {
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份（可选，不传则按年统计）
     */
    private Integer month;
    
    /**
     * 收支类型：INCOME-收入，EXPENSE-支出
     */
    private String amountType;
}
