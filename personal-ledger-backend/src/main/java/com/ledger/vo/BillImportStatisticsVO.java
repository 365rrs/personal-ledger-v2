package com.ledger.vo;

import lombok.Data;

/**
 * 导入统计VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillImportStatisticsVO {
    
    /**
     * 总记录数
     */
    private Integer totalCount;
    
    /**
     * 成功数
     */
    private Integer successCount;
    
    /**
     * 失败数
     */
    private Integer failCount;
    
    /**
     * 唯一记录数
     */
    private Integer uniqueCount;
    
    /**
     * 重复记录数
     */
    private Integer duplicateCount;
    
    /**
     * 待转换数
     */
    private Integer pendingCount;
    
    /**
     * 已转换数
     */
    private Integer convertedCount;
}
