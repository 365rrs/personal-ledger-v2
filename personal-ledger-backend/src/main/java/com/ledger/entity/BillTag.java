package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账单标签实体
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bill_tag")
public class BillTag extends BaseEntity {

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签分类
     */
    private String tagCategory;

    /**
     * 标签颜色
     */
    private String tagColor;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 状态：enable-启用，disable-停用
     */
    private String tagStatus;
}
