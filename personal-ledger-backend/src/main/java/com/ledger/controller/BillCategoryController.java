package com.ledger.controller;

import com.ledger.common.Response;
import com.ledger.dto.BillCategoryDTO;
import com.ledger.service.BillCategoryService;
import com.ledger.vo.BillCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 账单分类控制器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@RestController
@RequestMapping("/category")
@Tag(name = "账单分类管理")
public class BillCategoryController {

    @Resource
    private BillCategoryService billCategoryService;

    @PostMapping
    @Operation(summary = "创建分类")
    public Response<BillCategoryVO> create(@RequestBody @Validated BillCategoryDTO dto) {
        BillCategoryVO result = billCategoryService.create(dto);
        return Response.success(result);
    }

    @PostMapping("/update")
    @Operation(summary = "更新分类")
    public Response<BillCategoryVO> update(@RequestBody @Validated BillCategoryDTO dto) {
        BillCategoryVO result = billCategoryService.update(dto);
        return Response.success(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除分类")
    public Response<Void> delete(@RequestParam Long id) {
        billCategoryService.delete(id);
        return Response.success();
    }

    @PostMapping("/toggle")
    @Operation(summary = "启用/禁用分类")
    public Response<Void> toggleStatus(@RequestParam Long id, @RequestParam String enabled) {
        billCategoryService.toggleStatus(id, enabled);
        return Response.success();
    }

    @PostMapping("/list")
    @Operation(summary = "查询分类列表（树形结构）")
    public Response<List<BillCategoryVO>> list(
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) String enabled
    ) {
        List<BillCategoryVO> result = billCategoryService.list(categoryType, enabled);
        return Response.success(result);
    }

    @GetMapping("/tree")
    @Operation(summary = "查询分类树")
    public Response<List<BillCategoryVO>> tree(
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) String enabled
    ) {
        List<BillCategoryVO> result = billCategoryService.list(categoryType, enabled);
        return Response.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询分类")
    public Response<BillCategoryVO> getById(@PathVariable Long id) {
        BillCategoryVO result = billCategoryService.getById(id);
        return Response.success(result);
    }

    @PostMapping("/sort/update")
    @Operation(summary = "更新分类排序")
    public Response<Void> updateSortOrder(@RequestParam Long id, @RequestParam Integer sortOrder) {
        billCategoryService.updateSortOrder(id, sortOrder);
        return Response.success();
    }
}
