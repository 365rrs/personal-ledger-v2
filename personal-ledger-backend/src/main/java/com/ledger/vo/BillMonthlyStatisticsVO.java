package com.ledger.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 月度统计 VO
 * <p>
 * 一次性返回某年各月的收入、支出、结余和笔数，避免前端按月多次请求
 * </p>
 *
 * @author personal-ledger
 * @date 2026-08-18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillMonthlyStatisticsVO {
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份（1-12）
     */
    private Integer month;
    
    /**
     * 总收入
     */
    private BigDecimal totalIncome;
    
    /**
     * 总支出
     */
    private BigDecimal totalExpense;
    
    /**
     * 结余（收入 - 支出）
     */
    private BigDecimal balance;
    
    /**
     * 账单笔数
     */
    private Integer billCount;
}
