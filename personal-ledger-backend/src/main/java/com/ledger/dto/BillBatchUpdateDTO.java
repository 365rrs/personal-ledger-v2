package com.ledger.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * 批量更新账单 DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillBatchUpdateDTO {
    
    /**
     * 账单 ID 列表
     */
    @NotEmpty(message = "账单 ID 列表不能为空")
    private List<Long> billIds;
    
    /**
     * 需要更新的字段列表（明确指定要更新哪些字段）
     * 可选值：paymentChannel, categoryId, category, subCategoryId, subCategory, 
     *        manualRemark, tagIds, includeInStatistics
     */
    private Set<String> updateFields;
    
    /**
     * 支付渠道（可选）
     */
    private String paymentChannel;
    
    /**
     * 分类 ID（可选）
     */
    private Long categoryId;
    
    /**
     * 分类名称（可选）
     */
    private String category;
    
    /**
     * 二级分类 ID（可选）
     */
    private Long subCategoryId;
    
    /**
     * 二级分类名称（可选）
     */
    private String subCategory;
    
    /**
     * 用户备注（可选）
     */
    private String manualRemark;
    
    /**
     * 标签 ID 列表（可选，用于批量添加/替换标签）
     */
    private List<Long> tagIds;
    
    /**
     * 是否计入收支（可选）
     */
    private String includeInStatistics;
}
