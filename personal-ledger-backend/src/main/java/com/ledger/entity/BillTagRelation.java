package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账单标签关联实体
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bill_tag_relation")
public class BillTagRelation extends BaseEntity {

    /**
     * 账单 ID
     */
    private Long billId;

    /**
     * 标签 ID
     */
    private Long tagId;
}
