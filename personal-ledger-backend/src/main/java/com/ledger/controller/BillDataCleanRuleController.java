package com.ledger.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.common.Response;
import com.ledger.dto.BillDataCleanRuleDTO;
import com.ledger.dto.BillDataCleanRuleQueryDTO;
import com.ledger.service.BillDataCleanService;
import com.ledger.vo.BillDataCleanRuleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 数据清洗规则控制器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@RestController
@RequestMapping("/bill-data-clean-rule")
@Api(tags = "数据清洗规则管理")
public class BillDataCleanRuleController {

    @Resource
    private BillDataCleanService billDataCleanService;

    @PostMapping("/page")
    @ApiOperation("分页查询清洗规则")
    public Response<IPage<BillDataCleanRuleVO>> pageRules(@RequestBody BillDataCleanRuleQueryDTO dto) {
        IPage<BillDataCleanRuleVO> page = billDataCleanService.pageRules(dto);
        return Response.success(page);
    }

    @GetMapping("/detail")
    @ApiOperation("查询清洗规则详情")
    public Response<BillDataCleanRuleVO> getRuleById(@RequestParam Long id) {
        BillDataCleanRuleVO vo = billDataCleanService.getRuleById(id);
        return Response.success(vo);
    }

    @PostMapping("/create")
    @ApiOperation("创建清洗规则")
    public Response<Long> createRule(@RequestBody @Validated BillDataCleanRuleDTO dto) {
        Long id = billDataCleanService.createRule(dto);
        return Response.success(id);
    }

    @PostMapping("/update")
    @ApiOperation("更新清洗规则")
    public Response<Void> updateRule(@RequestBody @Validated BillDataCleanRuleDTO dto) {
        billDataCleanService.updateRule(dto);
        return Response.success();
    }

    @PostMapping("/delete")
    @ApiOperation("删除清洗规则")
    public Response<Void> deleteRule(@RequestParam Long id) {
        billDataCleanService.deleteRule(id);
        return Response.success();
    }
    
    @PostMapping("/test-clean-bill")
    @ApiOperation("测试账单清洗（根据账单 ID 清洗并返回对比结果）")
    public Response<com.ledger.vo.BillVO> testCleanBill(@RequestParam Long billId) {
        com.ledger.vo.BillVO vo = billDataCleanService.testCleanBill(billId);
        return Response.success(vo);
    }
    
    @PostMapping("/batch-clean")
    @ApiOperation("批量清洗账单")
    public Response<Integer> batchCleanBills(@RequestBody java.util.List<Long> billIds) {
        int successCount = billDataCleanService.batchCleanBills(billIds);
        return Response.success(successCount);
    }
}
