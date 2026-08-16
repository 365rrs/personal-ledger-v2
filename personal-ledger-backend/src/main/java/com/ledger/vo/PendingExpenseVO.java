package com.ledger.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待支出VO（列表查询返回）
 *
 * @author personal-ledger
 * @date 2025-01-15
 */
@Data
public class PendingExpenseVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 项目名称
     */
    private String expenseName;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 支付日期
     */
    private LocalDate paymentDate;
    
    /**
     * 周期：MONTHLY-每月, YEARLY-每年, ONETIME-一次性
     */
    private String period;
    
    /**
     * 周期名称（中文）
     */
    private String periodName;
    
    /**
     * 计划类型：RIGID-刚性支出, INTENDED-意向计划支出
     */
    private String planType;
    
    /**
     * 计划类型名称（中文）
     */
    private String planTypeName;
    
    /**
     * 支出状态：PENDING-待支付, COMPLETED-已完成, CANCELLED-已取消
     */
    private String status;
    
    /**
     * 状态名称（中文）
     */
    private String statusName;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    
    /**
     * 取消时间
     */
    private LocalDateTime cancelledTime;
    
    // ============ 审计信息字段 ============
    
    /**
     * 创建人姓名
     */
    private String creatorName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新人姓名
     */
    private String updaterName;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
