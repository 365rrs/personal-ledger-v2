package com.ledger.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 累计支出VO
 *
 * @author personal-ledger
 * @date 2026-01-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillCumulativeExpenseVO {
    
    /**
     * 日期（格式：yyyy-MM-dd）
     */
    private String date;
    
    /**
     * 累计支出
     */
    private BigDecimal cumulativeExpense;
    
    /**
     * 当日支出
     */
    private BigDecimal dailyExpense;
}
