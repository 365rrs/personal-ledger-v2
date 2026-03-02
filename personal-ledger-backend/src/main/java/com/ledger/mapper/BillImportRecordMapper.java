package com.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.entity.BillImportRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导入记录 Mapper
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Mapper
public interface BillImportRecordMapper extends BaseMapper<BillImportRecord> {
}
