package com.ledger.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 账单分类 DTO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillCategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（更新时需要）
     */
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    /**
     * 分类类型：INCOME-收入分类，EXPENSE-支出分类
     */
    @NotBlank(message = "分类类型不能为空")
    private String categoryType;

    /**
     * 父分类 ID（二级分类使用）
     */
    private Long parentId;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序序号
     */
    @NotNull(message = "排序序号不能为空")
    private Integer sortOrder;
}
