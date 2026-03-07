package com.ledger.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单统计 VO
 * <p>
 * 用于封装账单统计数据，包括总收入、总支出和结余
 * </p>
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillStatisticsVO {
    
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
}
