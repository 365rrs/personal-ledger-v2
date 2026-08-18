package com.ledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.dto.PendingExpenseDTO;
import com.ledger.dto.PendingExpenseQueryDTO;
import com.ledger.dto.RecurringExpenseDTO;
import com.ledger.vo.BatchOperationResult;
import com.ledger.vo.PendingExpenseStatisticsVO;
import com.ledger.vo.PendingExpenseVO;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * 待支出Service接口
 *
 * @author personal-ledger
 * @date 2025-01-15
 */
public interface PendingExpenseService {
    
    // ================== 基础 CRUD ==================
    
    /**
     * 创建待支出项目
     *
     * @param dto 待支出DTO
     * @return 项目ID
     */
    Long create(PendingExpenseDTO dto);
    
    /**
     * 更新待支出项目
     *
     * @param id 项目ID
     * @param dto 待支出DTO
     */
    void update(Long id, PendingExpenseDTO dto);
    
    /**
     * 删除待支出项目（逻辑删除）
     *
     * @param id 项目ID
     */
    void delete(Long id);
    
    /**
     * 根据ID查询待支出项目详情
     *
     * @param id 项目ID
     * @return 待支出VO
     */
    PendingExpenseVO getById(Long id);
    
    // ================== 周期性支出批量创建 ==================
    
    /**
     * 批量创建周期性支出
     *
     * @param dto 周期性支出DTO
     * @return 创建的所有项目ID列表
     */
    List<Long> createRecurring(RecurringExpenseDTO dto);
    
    // ================== 状态管理 ==================
    
    /**
     * 标记为已完成
     *
     * @param id 项目ID
     */
    void markAsCompleted(Long id);
    
    /**
     * 标记为已取消
     *
     * @param id 项目ID
     */
    void markAsCancelled(Long id);
    
    /**
     * 标记为待支付
     *
     * @param id 项目ID
     */
    void markAsPending(Long id);
    
    // ================== 批量操作 ==================
    
    /**
     * 批量标记为已完成
     *
     * @param ids 项目ID列表
     * @return 批量操作结果
     */
    BatchOperationResult batchMarkAsCompleted(List<Long> ids);
    
    /**
     * 批量标记为已取消
     *
     * @param ids 项目ID列表
     * @return 批量操作结果
     */
    BatchOperationResult batchMarkAsCancelled(List<Long> ids);
    
    /**
     * 批量删除
     *
     * @param ids 项目ID列表
     * @return 批量操作结果
     */
    BatchOperationResult batchDelete(List<Long> ids);
    
    // ================== 查询与分页 ==================
    
    /**
     * 分页查询待支出项目
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<PendingExpenseVO> page(PendingExpenseQueryDTO queryDTO);
    
    // ================== 统计分析 ==================
    
    /**
     * 获取综合统计信息
     *
     * @param queryDTO 查询条件
     * @return 统计信息
     */
    PendingExpenseStatisticsVO getStatistics(PendingExpenseQueryDTO queryDTO);
    
    /**
     * 获取待支付总金额
     *
     * @param year 年份（可为null，表示统计所有年份）
     * @return 待支付总金额
     */
    BigDecimal getTotalPendingAmount(Integer year);
    
    /**
     * 根据查询条件计算待支付金额总和
     *
     * @param queryDTO 查询条件（可为null）
     * @return 待支付总金额
     */
    BigDecimal getPendingAmountByQuery(PendingExpenseQueryDTO queryDTO);
    
    /**
     * 按月份统计待支出
     *
     * @param year 年份（可为null，表示统计所有年份）
     * @return 月度统计列表
     */
    List<PendingExpenseStatisticsVO.MonthlyStatistics> getMonthlyStatistics(Integer year);
    
    /**
     * 按分类统计待支出
     *
     * @param year 年份（可为null，表示统计所有年份）
     * @return 分类统计列表
     */
    List<PendingExpenseStatisticsVO.CategoryStatistics> getCategoryStatistics(Integer year);
    
    /**
     * 按周期统计待支出
     *
     * @param year 年份（可为null，表示统计所有年份）
     * @return 周期统计列表
     */
    List<PendingExpenseStatisticsVO.PeriodStatistics> getPeriodStatistics(Integer year);
    
    /**
     * 按计划类型统计待支出
     *
     * @param year 年份（可为null，表示统计所有年份）
     * @return 计划类型统计列表
     */
    List<PendingExpenseStatisticsVO.PlanTypeStatistics> getPlanTypeStatistics(Integer year);
    
    // ================== 导入导出 ==================
    
    /**
     * 导出待支出项目为Excel
     *
     * @param response HTTP响应
     * @param queryDTO 查询条件
     */
    void exportToExcel(HttpServletResponse response, PendingExpenseQueryDTO queryDTO);
}
