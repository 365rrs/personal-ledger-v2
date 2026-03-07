package com.ledger.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单统计VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillStatisticsVO {
    
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
}
