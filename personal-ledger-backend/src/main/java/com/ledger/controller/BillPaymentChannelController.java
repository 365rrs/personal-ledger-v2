package com.ledger.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.common.Response;
import com.ledger.dto.BillPaymentChannelDTO;
import com.ledger.service.BillPaymentChannelService;
import com.ledger.vo.BillPaymentChannelVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 支付渠道 Controller
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@RestController
@RequestMapping("/payment-channel")
@Api(tags = "支付渠道管理")
public class BillPaymentChannelController {
    
    @Resource
    private BillPaymentChannelService billPaymentChannelService;
    
    @PostMapping
    @ApiOperation("创建支付渠道")
    public Response<BillPaymentChannelVO> create(@RequestBody @Validated BillPaymentChannelDTO dto) {
        BillPaymentChannelVO result = billPaymentChannelService.create(dto);
        return Response.success(result);
    }
    
    @PutMapping
    @ApiOperation("更新支付渠道")
    public Response<BillPaymentChannelVO> update(@RequestBody @Validated BillPaymentChannelDTO dto) {
        BillPaymentChannelVO result = billPaymentChannelService.update(dto);
        return Response.success(result);
    }
    
    @DeleteMapping
    @ApiOperation("删除支付渠道")
    public Response<Void> delete(@RequestParam Long id) {
        billPaymentChannelService.delete(id);
        return Response.success();
    }
    
    @GetMapping
    @ApiOperation("查询支付渠道详情")
    public Response<BillPaymentChannelVO> getById(@RequestParam Long id) {
        BillPaymentChannelVO result = billPaymentChannelService.getById(id);
        return Response.success(result);
    }
    
    @PostMapping("/list")
    @ApiOperation("分页查询支付渠道")
    public Response<IPage<BillPaymentChannelVO>> page(@RequestBody BillPaymentChannelDTO dto) {
        IPage<BillPaymentChannelVO> result = billPaymentChannelService.page(dto);
        return Response.success(result);
    }
    
    @PostMapping("/list-all")
    @ApiOperation("查询支付渠道列表（不分页）")
    public Response<List<BillPaymentChannelVO>> list(@RequestBody BillPaymentChannelDTO dto) {
        List<BillPaymentChannelVO> result = billPaymentChannelService.list(dto);
        return Response.success(result);
    }
    
    @PostMapping("/toggle")
    @ApiOperation("启用/禁用支付渠道")
    public Response<Void> toggle(@RequestParam Long id, @RequestParam String enabled) {
        billPaymentChannelService.toggleStatus(id, enabled);
        return Response.success();
    }
    
    @PostMapping("/sort")
    @ApiOperation("更新支付渠道排序")
    public Response<Void> updateSortOrder(@RequestParam Long id, @RequestParam Integer sortOrder) {
        billPaymentChannelService.updateSortOrder(id, sortOrder);
        return Response.success();
    }
}
