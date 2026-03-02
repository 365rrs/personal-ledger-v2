package com.ledger.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 账单导入记录数据（用于 EasyExcel 解析）
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillImportRecordData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易日期
     */
    @ExcelProperty(index = 0)
    private String transactionDate;

    /**
     * 交易时间
     */
    @ExcelProperty(index = 1)
    private String transactionTime;

    /**
     * 收入金额
     */
    @ExcelProperty(index = 2)
    private String incomeAmount;

    /**
     * 支出金额
     */
    @ExcelProperty(index = 3)
    private String expenseAmount;

    /**
     * 账户余额
     */
    @ExcelProperty(index = 4)
    private String balance;

    /**
     * 交易类型
     */
    @ExcelProperty(index = 5)
    private String transactionType;

    /**
     * 交易描述
     */
    @ExcelProperty(index = 6)
    private String transactionDesc;
}
