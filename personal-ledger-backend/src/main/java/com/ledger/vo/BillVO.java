package com.ledger.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 账单VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillVO {
    
    private Long id;
    private LocalDate transactionDate;
    private LocalTime transactionTime;
    private BigDecimal incomeAmount;
    private BigDecimal expenseAmount;
    private String amountType;
    private String transactionType;
    private String transactionDesc;
    /**
     * 支付渠道名称
     */
    private String paymentChannel;
    /**
     * 支付渠道 ID
     */
    private Long paymentChannelId;
    /**
     * 支付渠道类型（用于前端展示图标）
     */
    private String paymentChannelType;
    /**
     * 支付渠道图标
     */
    private String paymentChannelIcon;
    /**
     * 分类名称（兼容旧版本）
     */
    private String category;
    /**
     * 分类 ID
     */
    private Long categoryId;
    /**
     * 二级分类名称（兼容旧版本）
     */
    private String subCategory;
    /**
     * 二级分类 ID
     */
    private Long subCategoryId;
    private String manualRemark;
    private String includeInStatistics;
    private String manualEntry;
    private java.util.List<Long> tagIds;  // 标签 ID 列表
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // ============ 以下字段用于清洗测试对比 ============
    
    /**
     * 原始分类（清洗前）
     */
    private String originalCategory;
    
    /**
     * 原始支付渠道（清洗前）
     */
    private String originalPaymentChannel;
    
    /**
     * 原始用户备注（清洗前）
     */
    private String originalManualRemark;
}
