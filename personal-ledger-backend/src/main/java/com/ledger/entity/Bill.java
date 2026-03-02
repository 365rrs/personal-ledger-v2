package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 账单实体类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill")
public class Bill extends BaseEntity {
    
    /**
     * 交易日期
     */
    private LocalDate transactionDate;
    
    /**
     * 交易时间
     */
    private LocalTime transactionTime;
    
    /**
     * 收入金额
     */
    private BigDecimal incomeAmount;
    
    /**
     * 支出金额
     */
    private BigDecimal expenseAmount;
    
    /**
     * 交易类型：INCOME-收入，EXPENSE-支出
     */
    private String transactionType;
    
    /**
     * 交易描述
     */
    private String transactionDesc;
    
    /**
     * 支付渠道
     */
    private String paymentChannel;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 二级分类
     */
    private String subCategory;
    
    /**
     * 手工备注
     */
    private String manualRemark;
    
    /**
     * 是否计入收支统计：0-不计入，1-计入
     */
    private String includeInStatistics;
    
    /**
     * 是否手工记账：0-否，1-是
     */
    private String manualEntry;
    
    /**
     * 数据指纹（用于重复检测）
     */
    private String dataHash;
}
