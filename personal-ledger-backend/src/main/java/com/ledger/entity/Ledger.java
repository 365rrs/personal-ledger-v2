package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单实体
 *
 * @author personal-ledger
 * @version 1.0
 * @date 2025-01-13
 */
@Data
@TableName("t_ledger")
public class Ledger {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private BigDecimal amount;

    private String category;

    private String description;

    private LocalDateTime transactionTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
