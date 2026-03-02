package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 导入明细实体类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_import_detail")
public class BillImportDetail extends BaseEntity {
    
    /**
     * 导入记录ID
     */
    private Long importRecordId;
    
    /**
     * 原始数据（JSON格式）
     */
    private String originalData;
    
    /**
     * 类型
     */
    private String type;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;
    
    /**
     * 导入状态：SUCCESS-成功，FAILED-失败
     */
    private String importStatus;
    
    /**
     * 重复状态：UNCHECKED-未检查，UNIQUE-唯一，DUPLICATE-重复
     */
    private String duplicateStatus;
    
    /**
     * 重复的账单ID
     */
    private Long duplicateLedgerId;
    
    /**
     * 数据指纹
     */
    private String dataHash;
    
    /**
     * 转账单状态：PENDING-待转换，CONVERTED-已转换，SKIPPED-已跳过，DUPLICATE-重复跳过，CONVERT_FAILED-转换失败
     */
    private String convertStatus;
    
    /**
     * 转换错误信息
     */
    private String convertErrorMessage;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 关联的账单ID
     */
    private Long ledgerId;
}
