package com.ledger.dto;

import lombok.Data;

/**
 * 账单标签查询 DTO
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Data
public class BillTagQueryDTO {
    
    /**
     * 当前页
     */
    private Integer current = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 20;
    
    /**
     * 标签名称（模糊查询）
     */
    private String tagName;
    
    /**
     * 标签分类
     */
    private String tagCategory;
    
    /**
     * 状态：enable-启用，disable-停用
     */
    private String tagStatus;
}
