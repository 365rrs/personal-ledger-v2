package com.ledger.dto;

import lombok.Data;

/**
 * 月度统计查询 DTO
 *
 * @author personal-ledger
 * @date 2026-08-18
 */
@Data
public class BillMonthlyStatisticsQueryDTO {
    
    /**
     * 年份（不传则默认当前年份）
     */
    private Integer year;
    
    /**
     * 是否只统计计入收支的数据：1-是，0-否。默认 1
     */
    private String includeInStatistics;
}
