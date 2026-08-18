package com.ledger.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 待支出导出DTO
 *
 * @author personal-ledger
 * @date 2025-01-15
 */
@Data
public class PendingExpenseExportDTO {
    
    @ExcelProperty("项目名称")
    private String expenseName;
    
    @ExcelProperty("金额")
    private BigDecimal amount;
    
    @ExcelProperty("支付日期")
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate paymentDate;
    
    @ExcelProperty("周期")
    private String period;
    
    @ExcelProperty("计划类型")
    private String planType;
    
    @ExcelProperty("状态")
    private String status;
    
    @ExcelProperty("分类名称")
    private String categoryName;
    
    @ExcelProperty("备注")
    private String remark;
}
