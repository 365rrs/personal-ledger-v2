package com.ledger.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入记录VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillImportRecordVO {
    
    private Long id;
    private String fileName;
    private Long fileSize;
    private Integer totalCount;
    private Integer processedCount;
    private Integer successCount;
    private Integer failCount;
    private String status;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    
    /**
     * 进度百分比
     */
    private Integer progress;
}
