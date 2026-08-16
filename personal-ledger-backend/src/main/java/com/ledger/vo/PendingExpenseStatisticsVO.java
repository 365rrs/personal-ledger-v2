package com.ledger.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 待支出统计结果 VO
 * <p>
 * 用于封装待支出项目的综合统计信息，包括多维度统计数据
 * </p>
 *
 * @author personal-ledger
 * @date 2025-01-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingExpenseStatisticsVO {
    
    /**
     * 总金额统计
     */
    private BigDecimal totalAmount;
    
    /**
     * 总数量统计
     */
    private Integer totalCount;
    
    /**
     * 按月份统计列表
     */
    private List<MonthlyStatistics> monthlyStatistics;
    
    /**
     * 按分类统计列表
     */
    private List<CategoryStatistics> categoryStatistics;
    
    /**
     * 按周期统计列表
     */
    private List<PeriodStatistics> periodStatistics;
    
    /**
     * 按计划类型统计列表
     */
    private List<PlanTypeStatistics> planTypeStatistics;
    
    /**
     * 按状态统计列表
     */
    private List<StatusStatistics> statusStatistics;
    
    /**
     * 月度统计内部类
     * <p>
     * 用于按月份维度统计待支出金额和数量
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStatistics {
        
        /**
         * 年月（格式：2024-01）
         */
        private String yearMonth;
        
        /**
         * 金额
         */
        private BigDecimal amount;
        
        /**
         * 数量
         */
        private Integer count;
    }
    
    /**
     * 分类统计内部类
     * <p>
     * 用于按分类维度统计待支出金额和数量
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatistics {
        
        /**
         * 分类ID
         */
        private Long categoryId;
        
        /**
         * 分类名称
         */
        private String categoryName;
        
        /**
         * 金额
         */
        private BigDecimal amount;
        
        /**
         * 数量
         */
        private Integer count;
    }
    
    /**
     * 周期统计内部类
     * <p>
     * 用于按周期维度统计待支出金额和数量
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodStatistics {
        
        /**
         * 周期代码（MONTHLY/YEARLY/ONETIME）
         */
        private String period;
        
        /**
         * 周期名称（每月/每年/一次性）
         */
        private String periodName;
        
        /**
         * 金额
         */
        private BigDecimal amount;
        
        /**
         * 数量
         */
        private Integer count;
    }
    
    /**
     * 计划类型统计内部类
     * <p>
     * 用于按计划类型维度统计待支出金额、数量和占比
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanTypeStatistics {
        
        /**
         * 计划类型代码（RIGID/INTENDED）
         */
        private String planType;
        
        /**
         * 计划类型名称（刚性支出/意向计划支出）
         */
        private String planTypeName;
        
        /**
         * 金额
         */
        private BigDecimal amount;
        
        /**
         * 数量
         */
        private Integer count;
        
        /**
         * 占比（百分比，如 65.5 表示 65.5%）
         */
        private BigDecimal percentage;
    }
    
    /**
     * 状态统计内部类
     * <p>
     * 用于按状态维度统计待支出金额和数量
     * </p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusStatistics {
        
        /**
         * 状态代码（PENDING/COMPLETED/CANCELLED）
         */
        private String status;
        
        /**
         * 状态名称（待支付/已完成/已取消）
         */
        private String statusName;
        
        /**
         * 金额
         */
        private BigDecimal amount;
        
        /**
         * 数量
         */
        private Integer count;
    }
}
