package com.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.common.Response;
import com.ledger.dto.PendingExpenseDTO;
import com.ledger.dto.PendingExpenseQueryDTO;
import com.ledger.dto.RecurringExpenseDTO;
import com.ledger.service.PendingExpenseService;
import com.ledger.vo.BatchOperationResult;
import com.ledger.vo.PendingExpenseStatisticsVO;
import com.ledger.vo.PendingExpenseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 待支出管理Controller
 *
 * @author personal-ledger
 * @date 2025-01-15
 */
@Slf4j
@RestController
@RequestMapping("/pending-expense")
@Api(tags = "待支出管理")
public class PendingExpenseController {
    
    @Resource
    private PendingExpenseService pendingExpenseService;
    
    // ================== CRUD 操作 ==================
    
    /**
     * 创建待支出项目
     *
     * @param dto 待支出项目DTO
     * @return 创建的项目ID
     */
    @PostMapping
    @ApiOperation("创建待支出项目")
    public Response<Long> create(@Valid @RequestBody PendingExpenseDTO dto) {
        log.info("接收创建待支出项目请求 - expenseName: {}", dto.getExpenseName());
        Long id = pendingExpenseService.create(dto);
        return Response.success(id);
    }
    
    /**
     * 更新待支出项目
     *
     * @param id  项目ID
     * @param dto 待支出项目DTO
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @ApiOperation("更新待支出项目")
    public Response<Void> update(@PathVariable Long id, @Valid @RequestBody PendingExpenseDTO dto) {
        log.info("接收更新待支出项目请求 - id: {}, expenseName: {}", id, dto.getExpenseName());
        pendingExpenseService.update(id, dto);
        return Response.success();
    }
    
    /**
     * 删除待支出项目
     *
     * @param id 项目ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除待支出项目")
    public Response<Void> delete(@PathVariable Long id) {
        log.info("接收删除待支出项目请求 - id: {}", id);
        pendingExpenseService.delete(id);
        return Response.success();
    }
    
    /**
     * 获取待支出项目详情
     *
     * @param id 项目ID
     * @return 项目详情VO
     */
    @GetMapping("/{id}")
    @ApiOperation("获取待支出项目详情")
    public Response<PendingExpenseVO> getById(@PathVariable Long id) {
        log.info("接收查询待支出项目详情请求 - id: {}", id);
        PendingExpenseVO vo = pendingExpenseService.getById(id);
        return Response.success(vo);
    }
    
    // ================== 周期性支出批量创建 ==================
    
    /**
     * 批量创建周期性支出
     *
     * @param dto 周期性支出DTO
     * @return 创建的项目ID列表
     */
    @PostMapping("/recurring")
    @ApiOperation("批量创建周期性支出")
    public Response<List<Long>> createRecurring(@Valid @RequestBody RecurringExpenseDTO dto) {
        log.info("接收批量创建周期性支出请求 - expenseName: {}, year: {}, months: {}, days: {}", 
                 dto.getExpenseName(), dto.getYear(), dto.getMonths(), dto.getDays());
        List<Long> ids = pendingExpenseService.createRecurring(dto);
        return Response.success(ids);
    }
    
    // ================== 状态管理 ==================
    
    /**
     * 标记为已完成
     *
     * @param id 项目ID
     * @return 成功响应
     */
    @PutMapping("/{id}/complete")
    @ApiOperation("标记为已完成")
    public Response<Void> markAsCompleted(@PathVariable Long id) {
        log.info("接收标记为已完成请求 - id: {}", id);
        pendingExpenseService.markAsCompleted(id);
        return Response.success();
    }
    
    /**
     * 标记为已取消
     *
     * @param id 项目ID
     * @return 成功响应
     */
    @PutMapping("/{id}/cancel")
    @ApiOperation("标记为已取消")
    public Response<Void> markAsCancelled(@PathVariable Long id) {
        log.info("接收标记为已取消请求 - id: {}", id);
        pendingExpenseService.markAsCancelled(id);
        return Response.success();
    }
    
    /**
     * 标记为待支付
     *
     * @param id 项目ID
     * @return 成功响应
     */
    @PutMapping("/{id}/pending")
    @ApiOperation("标记为待支付")
    public Response<Void> markAsPending(@PathVariable Long id) {
        log.info("接收标记为待支付请求 - id: {}", id);
        pendingExpenseService.markAsPending(id);
        return Response.success();
    }
    
    // ================== 批量操作 ==================
    
    /**
     * 批量标记为已完成
     *
     * @param ids 项目ID列表
     * @return 批量操作结果
     */
    @PostMapping("/batch/complete")
    @ApiOperation("批量标记为已完成")
    public Response<BatchOperationResult> batchMarkAsCompleted(@RequestBody List<Long> ids) {
        log.info("接收批量标记为已完成请求 - ids: {}", ids);
        BatchOperationResult result = pendingExpenseService.batchMarkAsCompleted(ids);
        return Response.success(result);
    }
    
    /**
     * 批量标记为已取消
     *
     * @param ids 项目ID列表
     * @return 批量操作结果
     */
    @PostMapping("/batch/cancel")
    @ApiOperation("批量标记为已取消")
    public Response<BatchOperationResult> batchMarkAsCancelled(@RequestBody List<Long> ids) {
        log.info("接收批量标记为已取消请求 - ids: {}", ids);
        BatchOperationResult result = pendingExpenseService.batchMarkAsCancelled(ids);
        return Response.success(result);
    }
    
    /**
     * 批量删除
     *
     * @param ids 项目ID列表
     * @return 批量操作结果
     */
    @PostMapping("/batch/delete")
    @ApiOperation("批量删除")
    public Response<BatchOperationResult> batchDelete(@RequestBody List<Long> ids) {
        log.info("接收批量删除请求 - ids: {}", ids);
        BatchOperationResult result = pendingExpenseService.batchDelete(ids);
        return Response.success(result);
    }
    
    // ================== 查询与分页 ==================
    
    /**
     * 分页查询待支出项目
     *
     * @param queryDTO 查询条件DTO
     * @return 分页结果
     */
    @PostMapping("/page")
    @ApiOperation("分页查询待支出项目")
    public Response<Page<PendingExpenseVO>> page(@RequestBody PendingExpenseQueryDTO queryDTO) {
        log.info("接收分页查询待支出项目请求 - pageNum: {}, pageSize: {}", 
                 queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<PendingExpenseVO> page = pendingExpenseService.page(queryDTO);
        return Response.success(page);
    }
    
    // ================== 统计分析 ==================
    
    /**
     * 获取综合统计信息
     *
     * @param queryDTO 查询条件DTO（可选）
     * @return 统计结果VO
     */
    @PostMapping("/statistics")
    @ApiOperation("获取综合统计信息")
    public Response<PendingExpenseStatisticsVO> getStatistics(@RequestBody(required = false) PendingExpenseQueryDTO queryDTO) {
        log.info("接收获取综合统计信息请求");
        PendingExpenseStatisticsVO statistics = pendingExpenseService.getStatistics(queryDTO);
        return Response.success(statistics);
    }
    
    /**
     * 获取待支付总金额
     *
     * @param year 年份（可选）
     * @return 待支付总金额
     */
    @GetMapping("/statistics/total-pending")
    @ApiOperation("获取待支付总金额")
    public Response<BigDecimal> getTotalPendingAmount(@RequestParam(required = false) Integer year) {
        log.info("接收获取待支付总金额请求 - year: {}", year);
        BigDecimal totalAmount = pendingExpenseService.getTotalPendingAmount(year);
        return Response.success(totalAmount);
    }
    
    /**
     * 根据查询条件计算待支付金额总和
     *
     * @param queryDTO 查询条件DTO
     * @return 待支付总金额
     */
    @PostMapping("/statistics/pending-amount")
    @ApiOperation("根据查询条件计算待支付金额总和")
    public Response<BigDecimal> getPendingAmountByQuery(@RequestBody(required = false) PendingExpenseQueryDTO queryDTO) {
        log.info("接收根据查询条件计算待支付金额总和请求");
        BigDecimal totalAmount = pendingExpenseService.getPendingAmountByQuery(queryDTO);
        return Response.success(totalAmount);
    }
    
    /**
     * 按月份统计
     *
     * @param year 年份
     * @return 月度统计结果列表
     */
    @GetMapping("/statistics/monthly")
    @ApiOperation("按月份统计")
    public Response<List<PendingExpenseStatisticsVO.MonthlyStatistics>> getMonthlyStatistics(@RequestParam Integer year) {
        log.info("接收按月份统计请求 - year: {}", year);
        List<PendingExpenseStatisticsVO.MonthlyStatistics> monthlyStatistics = 
                pendingExpenseService.getMonthlyStatistics(year);
        return Response.success(monthlyStatistics);
    }
    
    /**
     * 按分类统计
     *
     * @param year 年份（可选）
     * @return 分类统计结果列表
     */
    @GetMapping("/statistics/category")
    @ApiOperation("按分类统计")
    public Response<List<PendingExpenseStatisticsVO.CategoryStatistics>> getCategoryStatistics(@RequestParam(required = false) Integer year) {
        log.info("接收按分类统计请求 - year: {}", year);
        List<PendingExpenseStatisticsVO.CategoryStatistics> categoryStatistics = 
                pendingExpenseService.getCategoryStatistics(year);
        return Response.success(categoryStatistics);
    }
    
    /**
     * 按周期统计
     *
     * @param year 年份（可选）
     * @return 周期统计结果列表
     */
    @GetMapping("/statistics/period")
    @ApiOperation("按周期统计")
    public Response<List<PendingExpenseStatisticsVO.PeriodStatistics>> getPeriodStatistics(@RequestParam(required = false) Integer year) {
        log.info("接收按周期统计请求 - year: {}", year);
        List<PendingExpenseStatisticsVO.PeriodStatistics> periodStatistics = 
                pendingExpenseService.getPeriodStatistics(year);
        return Response.success(periodStatistics);
    }
    
    /**
     * 按计划类型统计
     *
     * @param year 年份（可选）
     * @return 计划类型统计结果列表
     */
    @GetMapping("/statistics/plan-type")
    @ApiOperation("按计划类型统计")
    public Response<List<PendingExpenseStatisticsVO.PlanTypeStatistics>> getPlanTypeStatistics(@RequestParam(required = false) Integer year) {
        log.info("接收按计划类型统计请求 - year: {}", year);
        List<PendingExpenseStatisticsVO.PlanTypeStatistics> planTypeStatistics = 
                pendingExpenseService.getPlanTypeStatistics(year);
        return Response.success(planTypeStatistics);
    }
    
    // ================== 导入导出 ==================
    
    /**
     * 导出为Excel
     *
     * @param response 响应对象
     * @param queryDTO 查询条件DTO（用于筛选导出数据）
     */
    @PostMapping("/export")
    @ApiOperation("导出为Excel")
    public void exportToExcel(HttpServletResponse response, 
                              @RequestBody(required = false) PendingExpenseQueryDTO queryDTO) {
        log.info("接收导出为Excel请求");
        pendingExpenseService.exportToExcel(response, queryDTO);
    }
}
