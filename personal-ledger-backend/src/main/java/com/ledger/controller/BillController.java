package com.ledger.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.common.Response;
import com.ledger.dto.BillBatchUpdateDTO;
import com.ledger.dto.BillDTO;
import com.ledger.dto.BillQueryDTO;
import com.ledger.service.BillService;
import com.ledger.vo.BillStatisticsVO;
import com.ledger.vo.BillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
}
