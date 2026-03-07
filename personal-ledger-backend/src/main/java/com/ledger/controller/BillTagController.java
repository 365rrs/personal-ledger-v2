package com.ledger.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.common.Response;
import com.ledger.dto.BillTagDTO;
import com.ledger.dto.BillTagQueryDTO;
import com.ledger.service.BillTagService;
import com.ledger.vo.BillTagVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 账单标签 Controller
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Slf4j
@RestController
@RequestMapping("/tag")
public class BillTagController {

    @Resource
    private BillTagService billTagService;

    @PostMapping("/page")
    public Response<IPage<BillTagVO>> page(@RequestBody BillTagQueryDTO dto) {
        IPage<BillTagVO> page = billTagService.page(dto);
        return Response.success(page);
    }

    @GetMapping("/{id}")
    public Response<BillTagVO> getById(@PathVariable Long id) {
        BillTagVO vo = billTagService.getById(id);
        return Response.success(vo);
    }

    @PostMapping("/create")
    public Response<Long> create(@RequestBody @Valid BillTagDTO dto) {
        Long id = billTagService.create(dto);
        return Response.success(id);
    }

    @PostMapping("/update")
    public Response<Void> update(@RequestBody @Valid BillTagDTO dto) {
        billTagService.update(dto);
        return Response.success();
    }

    @PostMapping("/delete")
    public Response<Void> delete(@RequestBody Long id) {
        billTagService.delete(id);
        return Response.success();
    }

    @PostMapping("/status/update")
    public Response<Void> updateStatus(@RequestParam Long id, @RequestParam String status) {
        billTagService.updateStatus(id, status);
        return Response.success();
    }
}
