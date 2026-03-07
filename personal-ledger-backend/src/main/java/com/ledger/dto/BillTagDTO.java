package com.ledger.dto;

import lombok.Data;

/**
 * 账单标签创建/更新 DTO
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Data
public class BillTagDTO {
    
    /**
     * 主键 ID（更新时必填）
     */
    private Long id;
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * 标签分类
     */
    private String tagCategory;
    
    /**
     * 标签颜色
     */
    private String tagColor;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
}
