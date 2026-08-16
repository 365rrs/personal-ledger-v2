package com.ledger.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 待支出 Excel 导入 DTO
 * 用于从 Excel 文件中导入待支出项目数据
 *
 * @author personal-ledger
 * @date 2025-01-14
 */
@Data
@ColumnWidth(15)
public class PendingExpenseImportDTO {
    
    /**
     * 项目名称（必填）
     */
    @ExcelProperty("项目名称")
    @ColumnWidth(20)
    private String expenseName;
    
    /**
     * 金额（必填，范围：0.01 - 999999.99）
     */
    @ExcelProperty("金额")
    @ColumnWidth(12)
    private BigDecimal amount;
    
    /**
     * 支付日期（必填）
     */
    @ExcelProperty("支付日期")
    @ColumnWidth(12)
    private LocalDate paymentDate;
    
    /**
     * 周期（必填）
     * 可选值：MONTHLY-每月, YEARLY-每年, ONETIME-一次性
     */
    @ExcelProperty("周期")
    @ColumnWidth(12)
    private String period;
    
    /**
     * 计划类型（必填）
     * 可选值：RIGID-刚性支出, INTENDED-意向计划支出
     */
    @ExcelProperty("计划类型")
    @ColumnWidth(15)
    private String planType;
    
    /**
     * 状态（可选，默认为 PENDING）
     * 可选值：PENDING-待支付, COMPLETED-已完成, CANCELLED-已取消
     */
    @ExcelProperty("状态")
    @ColumnWidth(12)
    private String status;
    
    /**
     * 分类名称（可选）
     */
    @ExcelProperty("分类名称")
    @ColumnWidth(15)
    private String categoryName;
    
    /**
     * 备注（可选）
     */
    @ExcelProperty("备注")
    @ColumnWidth(30)
    private String remark;
}
