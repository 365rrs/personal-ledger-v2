package com.ledger.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 账单分类 VO
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类类型：INCOME-收入分类，EXPENSE-支出分类
     */
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
     * 是否启用：0-禁用，1-启用
     */
    private String enabled;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 子分类列表
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<BillCategoryVO> children;

    /**
     * 使用次数（统计时使用）
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long usageCount;
}
