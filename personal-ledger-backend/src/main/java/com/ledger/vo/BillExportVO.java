package com.ledger.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单导出VO
 *
 * @author personal-ledger
 * @date 2026-03-09
 */
@Data
@ColumnWidth(15)
public class BillExportVO {
    
    @ExcelProperty("交易日期")
    @ColumnWidth(12)
    private String transactionDate;
    
    @ExcelProperty("交易时间")
    @ColumnWidth(10)
    private String transactionTime;
    
    @ExcelProperty("收入金额")
    @ColumnWidth(12)
    private BigDecimal incomeAmount;
    
    @ExcelProperty("支出金额")
    @ColumnWidth(12)
    private BigDecimal expenseAmount;
    
    @ExcelProperty("收支类型")
    @ColumnWidth(10)
    private String amountType;
    
    @ExcelProperty("交易类型")
    @ColumnWidth(12)
    private String transactionType;
    
    @ExcelProperty("分类")
    @ColumnWidth(12)
    private String category;
    
    @ExcelProperty("二级分类")
    @ColumnWidth(12)
    private String subCategory;
    
    @ExcelProperty("支付渠道")
    @ColumnWidth(12)
    private String paymentChannel;
    
    @ExcelProperty("交易描述")
    @ColumnWidth(30)
    private String transactionDesc;
    
    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String manualRemark;
    
    @ExcelProperty("计入统计")
    @ColumnWidth(10)
    private String includeInStatistics;
    
    @ExcelProperty("手动录入")
    @ColumnWidth(10)
    private String manualEntry;
    
    @ExcelProperty("标签")
    @ColumnWidth(20)
    private String tags;
}
