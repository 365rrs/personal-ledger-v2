package com.ledger.vo;

import lombok.Data;

/**
 * 账单标签 VO
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Data
public class BillTagVO {
    
    /**
     * 主键 ID
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
    
    /**
     * 状态：enable-启用，disable-停用
     */
    private String tagStatus;
    
    /**
     * 使用次数（统计字段）
     */
    private Integer usageCount;
}
