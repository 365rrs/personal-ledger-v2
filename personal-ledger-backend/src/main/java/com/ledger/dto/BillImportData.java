package com.ledger.dto;

import com.ledger.entity.BillImportRecordData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 账单导入数据包装类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
public class BillImportData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 导入元信息
     */
    private BillImportInfo importInfo;

    /**
     * 账单记录列表
     */
    private List<BillImportRecordData> records;
}
