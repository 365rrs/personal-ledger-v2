package com.ledger.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 待支出项目查询 DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class PendingExpenseQueryDTO {
    
    /**
     * 项目名称关键词搜索
     */
    private String expenseNameKeyword;
    
    /**
     * 周期筛选（可多选）
     * 可选值: MONTHLY, YEARLY, ONETIME
     */
    private List<String> periods;
    
    /**
     * 计划类型筛选（可多选）
     * 可选值: RIGID, INTENDED
     */
    private List<String> planTypes;
    
    /**
     * 状态筛选（可多选）
     * 可选值: PENDING, COMPLETED, CANCELLED
     */
    private List<String> statuses;
    
    /**
     * 分类 ID 筛选
     */
    private Long categoryId;
    
    /**
     * 支付日期范围 - 开始日期
     */
    private LocalDate paymentDateStart;
    
    /**
     * 支付日期范围 - 结束日期
     */
    private LocalDate paymentDateEnd;
    
    /**
     * 排序字段
     * 可选值: paymentDate, createTime, amount
     * 默认值: paymentDate
     */
    private String sortField = "paymentDate";
    
    /**
     * 排序方向
     * 可选值: asc, desc
     * 默认值: asc
     */
    private String sortOrder = "asc";
    
    /**
     * 页码（从 1 开始）
     * 默认值: 1
     */
    private Integer pageNum = 1;
    
    /**
     * 每页条数
     * 默认值: 20
     */
    private Integer pageSize = 20;
}
