package com.ledger.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.common.Response;
import com.ledger.dto.BillConvertDTO;
import com.ledger.dto.BillImportDetailQueryDTO;
import com.ledger.dto.BillImportRecordQueryDTO;
import com.ledger.dto.BillSkipDTO;
import com.ledger.service.BillImportService;
import com.ledger.vo.BillImportDetailVO;
import com.ledger.vo.BillImportRecordVO;
import com.ledger.vo.BillImportStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 账单导入控制器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Tag(name = "账单导入管理")
@RestController
@RequestMapping("/bill-import")
public class BillImportController {
    
    @Resource
    private BillImportService billImportService;
    
    @Operation(summary = "上传文件并导入")
    @PostMapping("/upload")
    public Response<Long> upload(@Parameter(description = "文件") @RequestParam("file") MultipartFile file) {
        Long importRecordId = billImportService.uploadAndImport(file);
        return Response.success(importRecordId);
    }
    
    @Operation(summary = "查询导入记录列表")
    @PostMapping("/record/page")
    public Response<IPage<BillImportRecordVO>> pageRecords(@RequestBody BillImportRecordQueryDTO dto) {
        IPage<BillImportRecordVO> page = billImportService.pageRecords(dto);
        return Response.success(page);
    }
    
    @Operation(summary = "查询导入记录详情")
    @GetMapping("/record")
    public Response<BillImportRecordVO> getRecord(@Parameter(description = "导入记录ID") @RequestParam Long id) {
        BillImportRecordVO vo = billImportService.getRecordById(id);
        return Response.success(vo);
    }
    
    @Operation(summary = "查询导入明细列表")
    @PostMapping("/detail/page")
    public Response<IPage<BillImportDetailVO>> pageDetails(@Validated @RequestBody BillImportDetailQueryDTO dto) {
        IPage<BillImportDetailVO> page = billImportService.pageDetails(dto);
        return Response.success(page);
    }
    
    @Operation(summary = "查询导入统计")
    @GetMapping("/statistics")
    public Response<BillImportStatisticsVO> getStatistics(@Parameter(description = "导入记录ID") @RequestParam Long importRecordId) {
        BillImportStatisticsVO statistics = billImportService.getStatistics(importRecordId);
        return Response.success(statistics);
    }
    
    @Operation(summary = "转换为账单")
    @PostMapping("/convert")
    public Response<Void> convertToBill(@Validated @RequestBody BillConvertDTO dto) {
        billImportService.convertToBill(dto);
        return Response.success();
    }
    
    @Operation(summary = "跳过记录")
    @PostMapping("/skip")
    public Response<Void> skipRecords(@Validated @RequestBody BillSkipDTO dto) {
        billImportService.skipRecords(dto);
        return Response.success();
    }
    
    @Operation(summary = "删除导入记录")
    @PostMapping("/record/delete")
    public Response<Void> deleteRecord(@RequestParam Long id) {
        billImportService.deleteRecord(id);
        return Response.success();
    }
}
