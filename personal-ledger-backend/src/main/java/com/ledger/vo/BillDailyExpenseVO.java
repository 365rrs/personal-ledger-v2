package com.ledger.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 每日支出VO
 *
 * @author personal-ledger
 * @date 2026-01-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillDailyExpenseVO {
    
    /**
     * 日期（格式：yyyy-MM-dd）
     */
    private String date;
    
    /**
     * 支出（正数表示支出，负数表示收入）
     */
    private BigDecimal balance;
}
