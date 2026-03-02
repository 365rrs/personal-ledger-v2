package com.ledger.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 跳过记录DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillSkipDTO {
    
    /**
     * 明细ID列表
     */
    @NotEmpty(message = "明细ID列表不能为空")
    private List<Long> detailIds;
}
