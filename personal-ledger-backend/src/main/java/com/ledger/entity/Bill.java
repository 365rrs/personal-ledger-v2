package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 账单实体类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "bill")
public class Bill extends BaseEntity {
    /**
     * 交易日期
     */
    @TableField(value = "transaction_date")
    private LocalDate transactionDate;

    /**
     * 交易时间
     */
    @TableField(value = "transaction_time")
    private LocalTime transactionTime;

    /**
     * 收入金额
     */
    @TableField(value = "income_amount")
    private BigDecimal incomeAmount;

    /**
     * 支出金额
     */
    @TableField(value = "expense_amount")
    private BigDecimal expenseAmount;

    /**
     * 金额类型：INCOME-收入，EXPENSE-支出
     */
    @TableField(value = "amount_type")
    private String amountType;

    /**
     * 交易类型（原始值）
     */
    @TableField(value = "transaction_type")
    private String transactionType;

    /**
     * 交易描述
     */
    @TableField(value = "transaction_desc")
    private String transactionDesc;

    /**
     * 支付渠道
     */
    @TableField(value = "payment_channel")
    private String paymentChannel;

    /**
     * 支付渠道 ID
     */
    @TableField(value = "payment_channel_id")
    private Long paymentChannelId;

    /**
     * 分类名称（兼容旧版本）
     */
    @TableField(value = "category")
    private String category;

    /**
     * 分类 ID
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 二级分类名称（兼容旧版本）
     */
    @TableField(value = "sub_category")
    private String subCategory;

    /**
     * 二级分类 ID
     */
    @TableField(value = "sub_category_id")
    private Long subCategoryId;

    /**
     * 手工备注
     */
    @TableField(value = "manual_remark")
    private String manualRemark;

    /**
     * 是否计入收支统计：0-不计入，1-计入
     */
    @TableField(value = "include_in_statistics")
    private String includeInStatistics;

    /**
     * 是否手工记账：0-否，1-是
     */
    @TableField(value = "manual_entry")
    private String manualEntry;

    /**
     * 数据指纹（用于重复检测）
     */
    @TableField(value = "data_hash")
    private String dataHash;
}