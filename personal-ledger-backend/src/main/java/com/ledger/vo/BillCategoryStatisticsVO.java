package com.ledger.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 分类统计VO
 *
 * @author personal-ledger
 * @date 2026-01-13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillCategoryStatisticsVO {
    
    /**
     * 分类名称
     */
    private String category;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 笔数
     */
    private Integer count;
    
    /**
     * 占比（百分比）
     */
    private BigDecimal percentage;
}
