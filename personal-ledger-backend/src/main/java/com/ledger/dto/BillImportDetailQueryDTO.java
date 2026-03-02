package com.ledger.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 导入明细查询DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillImportDetailQueryDTO {
    
    /**
     * 导入记录ID
     */
    @NotNull(message = "导入记录ID不能为空")
    private Long importRecordId;
    
    /**
     * 当前页
     */
    private Integer current = 1;
    
    /**
     * 每页条数
     */
    private Integer size = 20;
    
    /**
     * 导入状态
     */
    private String importStatus;
    
    /**
     * 重复状态
     */
    private String duplicateStatus;
    
    /**
     * 转换状态
     */
    private String convertStatus;
}
