package com.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.entity.BillTagRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单标签关联 Mapper
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Mapper
public interface BillTagRelationMapper extends BaseMapper<BillTagRelation> {

}
