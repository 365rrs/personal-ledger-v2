package com.ledger.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.common.Response;
import com.ledger.dto.BillBatchUpdateDTO;
import com.ledger.dto.BillCategoryStatisticsQueryDTO;
import com.ledger.dto.BillCumulativeExpenseQueryDTO;
import com.ledger.dto.BillDailyExpenseQueryDTO;
import com.ledger.dto.BillDTO;
import com.ledger.dto.BillMonthlyStatisticsQueryDTO;
import com.ledger.dto.BillQueryDTO;
import com.ledger.service.BillService;
import com.ledger.vo.BillCategoryStatisticsVO;
import com.ledger.vo.BillCumulativeExpenseVO;
import com.ledger.vo.BillDailyExpenseVO;
import com.ledger.vo.BillExportVO;
import com.ledger.vo.BillMonthlyStatisticsVO;
import com.ledger.vo.BillQianjiExportVO;
import com.ledger.vo.BillStatisticsVO;
import com.ledger.vo.BillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 账单控制器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Tag(name = "账单管理")
@RestController
@RequestMapping("/bill")
public class BillController {
    
    @Resource
    private BillService billService;
    
    @Operation(summary = "创建账单")
    @PostMapping
    public Response<Long> create(@Validated @RequestBody BillDTO dto) {
        Long id = billService.create(dto);
        return Response.success(id);
    }
    
    @Operation(summary = "更新账单")
    @PostMapping("/update")
    public Response<Void> update(@Validated @RequestBody BillDTO dto) {
        billService.update(dto);
        return Response.success();
    }
    
    @Operation(summary = "删除账单")
    @PostMapping("/delete")
    public Response<Void> delete(@RequestParam Long id) {
        billService.delete(id);
        return Response.success();
    }
    
    @Operation(summary = "查询账单详情")
    @GetMapping
    public Response<BillVO> getById(@Parameter(description = "账单ID") @RequestParam Long id) {
        BillVO vo = billService.getById(id);
        return Response.success(vo);
    }
    
    @Operation(summary = "分页查询账单")
    @PostMapping("/page")
    public Response<IPage<BillVO>> page(@Validated @RequestBody BillQueryDTO dto) {
        IPage<BillVO> page = billService.page(dto);
        return Response.success(page);
    }
    
    @Operation(summary = "查询统计数据")
    @PostMapping("/statistics")
    public Response<BillStatisticsVO> getStatistics(@RequestBody BillQueryDTO dto) {
        BillStatisticsVO statistics = billService.getStatistics(dto);
        return Response.success(statistics);
    }
    
    @Operation(summary = "批量更新账单")
    @PostMapping("/batchUpdate")
    public Response<Void> batchUpdate(@Validated @RequestBody BillBatchUpdateDTO dto) {
        billService.batchUpdate(dto);
        return Response.success();
    }
    
    @Operation(summary = "查询每日支出")
    @PostMapping("/dailyExpense")
    public Response<List<BillDailyExpenseVO>> getDailyExpense(@RequestBody BillDailyExpenseQueryDTO dto) {
        List<BillDailyExpenseVO> list = billService.getDailyExpense(dto);
        return Response.success(list);
    }
    
    @Operation(summary = "查询累计支出")
    @PostMapping("/cumulativeExpense")
    public Response<List<BillCumulativeExpenseVO>> getCumulativeExpense(@RequestBody BillCumulativeExpenseQueryDTO dto) {
        List<BillCumulativeExpenseVO> list = billService.getCumulativeExpense(dto);
        return Response.success(list);
    }
    
    @Operation(summary = "按分类统计")
    @PostMapping("/categoryStatistics")
    public Response<List<BillCategoryStatisticsVO>> getCategoryStatistics(@RequestBody BillCategoryStatisticsQueryDTO dto) {
        List<BillCategoryStatisticsVO> list = billService.getCategoryStatistics(dto);
        return Response.success(list);
    }
    
    @Operation(summary = "查询年度各月统计")
    @PostMapping("/monthlyStatistics")
    public Response<List<BillMonthlyStatisticsVO>> getMonthlyStatistics(@RequestBody BillMonthlyStatisticsQueryDTO dto) {
        List<BillMonthlyStatisticsVO> list = billService.getMonthlyStatistics(dto);
        return Response.success(list);
    }
    
    @Operation(summary = "导出账单")
    @PostMapping("/export")
    public void export(@RequestBody BillQueryDTO dto, HttpServletResponse response) throws IOException {
        // 查询数据
        List<BillExportVO> list = billService.exportBills(dto);
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("账单导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 导出
        EasyExcel.write(response.getOutputStream(), BillExportVO.class)
                .sheet("账单列表")
                .doWrite(list);
    }
    
    @Operation(summary = "导出账单为钱迹格式")
    @PostMapping("/exportQianji")
    public void exportQianji(@RequestBody BillQueryDTO dto, HttpServletResponse response) throws IOException {
        // 查询数据
        List<BillQianjiExportVO> list = billService.exportQianjiBills(dto);
        
        // 设置响应头为 Excel 格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("钱迹导入数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 使用 EasyExcel 导出为 Excel 格式
        EasyExcel.write(response.getOutputStream(), BillQianjiExportVO.class)
                .sheet("钱迹导入")
                .doWrite(list);
    }
}
