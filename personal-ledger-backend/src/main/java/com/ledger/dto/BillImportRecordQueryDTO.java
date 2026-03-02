package com.ledger.dto;

import lombok.Data;

/**
 * 导入记录查询DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillImportRecordQueryDTO {
    
    /**
     * 当前页
     */
    private Integer current = 1;
    
    /**
     * 每页条数
     */
    private Integer size = 10;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 文件名（模糊查询）
     */
    private String fileName;
}
