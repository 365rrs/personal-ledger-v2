package com.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.entity.PendingExpense;
import com.ledger.vo.PendingExpenseStatisticsVO.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 待支出Mapper
 *
 * @author personal-ledger
 * @date 2025-01-14
 */
@Mapper
public interface PendingExpenseMapper extends BaseMapper<PendingExpense> {
    // 基础 CRUD 方法由 BaseMapper 自动继承
    
    /**
     * 按月份统计待支出
     * <p>
     * 按支付日期的年月分组，统计每月的待支出金额总和和项目数量
     * 仅包含状态为 PENDING 的项目
     * </p>
     *
     * @param year 年份（可选，如果为null则统计所有年份）
     * @return 月度统计列表
     */
    List<MonthlyStatistics> getMonthlyStatistics(@Param("year") Integer year);
    
    /**
     * 按分类统计待支出
     * <p>
     * 按分类分组，统计每个分类的待支出金额总和和项目数量
     * 仅包含状态为 PENDING 的项目
     * 通过 LEFT JOIN 关联分类表获取分类名称
     * </p>
     *
     * @param year 年份（可选，如果为null则统计所有年份）
     * @return 分类统计列表
     */
    List<CategoryStatistics> getCategoryStatistics(@Param("year") Integer year);
    
    /**
     * 按周期统计待支出
     * <p>
     * 按周期（MONTHLY/YEARLY/ONETIME）分组，统计每种周期的待支出金额总和和项目数量
     * 包含所有状态的项目
     * </p>
     *
     * @param year 年份（可选，如果为null则统计所有年份）
     * @return 周期统计列表
     */
    List<PeriodStatistics> getPeriodStatistics(@Param("year") Integer year);
    
    /**
     * 按计划类型统计待支出
     * <p>
     * 按计划类型（RIGID/INTENDED）分组，统计每种类型的待支出金额总和和项目数量
     * 包含所有状态的项目
     * 注意：百分比需要在 Service 层计算
     * </p>
     *
     * @param year 年份（可选，如果为null则统计所有年份）
     * @return 计划类型统计列表
     */
    List<PlanTypeStatistics> getPlanTypeStatistics(@Param("year") Integer year);
    
    /**
     * 按状态统计待支出
     * <p>
     * 按状态（PENDING/COMPLETED/CANCELLED）分组，统计每种状态的待支出金额总和和项目数量
     * </p>
     *
     * @return 状态统计列表
     */
    List<StatusStatistics> getStatusStatistics();
}
