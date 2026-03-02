package com.ledger.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 账单导入元信息
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillImportInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 导出时间
     */
    private String exportTime;

    /**
     * 账号
     */
    private String accountNumber;

    /**
     * 币种
     */
    private String currency;

    /**
     * 起始日期
     */
    private String startDate;

    /**
     * 终止日期
     */
    private String endDate;
}
