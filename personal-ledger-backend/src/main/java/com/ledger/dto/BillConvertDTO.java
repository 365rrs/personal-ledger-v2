package com.ledger.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 账单转换DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillConvertDTO {
    
    /**
     * 导入记录ID
     */
    @NotNull(message = "导入记录ID不能为空")
    private Long importRecordId;
    
    /**
     * 明细ID列表
     */
    @NotEmpty(message = "明细ID列表不能为空")
    private List<Long> detailIds;
}
