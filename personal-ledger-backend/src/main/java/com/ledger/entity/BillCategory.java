package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账单分类实体类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_category")
public class BillCategory extends BaseEntity {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
}
