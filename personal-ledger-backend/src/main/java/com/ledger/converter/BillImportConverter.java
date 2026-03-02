package com.ledger.converter;

import com.ledger.entity.BillImportDetail;
import com.ledger.entity.BillImportRecord;
import com.ledger.vo.BillImportDetailVO;
import com.ledger.vo.BillImportRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 账单导入转换器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Mapper(componentModel = "spring")
public interface BillImportConverter {
    
    /**
     * 转换为记录VO
     */
    @Mapping(target = "progress", expression = "java(calculateProgress(record))")
    BillImportRecordVO toRecordVO(BillImportRecord record);
    
    /**
     * 转换为明细VO
     */
    BillImportDetailVO toDetailVO(BillImportDetail detail);
    
    /**
     * 计算进度
     */
    default Integer calculateProgress(BillImportRecord record) {
        if (record.getTotalCount() > 0) {
            return record.getProcessedCount() * 100 / record.getTotalCount();
        }
        return 0;
    }
}
