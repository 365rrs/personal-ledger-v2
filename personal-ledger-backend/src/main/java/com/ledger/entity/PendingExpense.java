package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待支出实体类
 *
 * @author personal-ledger
 * @date 2025-01-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "pending_expense")
public class PendingExpense extends BaseEntity {
    
    /**
     * 项目名称
     */
    @TableField(value = "expense_name")
    private String expenseName;
    
    /**
     * 金额
     */
    @TableField(value = "amount")
    private BigDecimal amount;
    
    /**
     * 支付日期
     */
    @TableField(value = "payment_date")
    private LocalDate paymentDate;
    
    /**
     * 周期：MONTHLY-每月, YEARLY-每年, ONETIME-一次性
     */
    @TableField(value = "period")
    private String period;
    
    /**
     * 计划类型：RIGID-刚性支出, INTENDED-意向计划支出
     */
    @TableField(value = "plan_type")
    private String planType;
    
    /**
     * 支出状态：PENDING-待支付, COMPLETED-已完成, CANCELLED-已取消
     */
    @TableField(value = "status")
    private String status;
    
    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Long categoryId;
    
    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
    
    /**
     * 完成时间
     */
    @TableField(value = "completed_time")
    private LocalDateTime completedTime;
    
    /**
     * 取消时间
     */
    @TableField(value = "cancelled_time")
    private LocalDateTime cancelledTime;
}
