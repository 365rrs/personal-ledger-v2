package com.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.entity.Bill;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单 Mapper
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {
}
