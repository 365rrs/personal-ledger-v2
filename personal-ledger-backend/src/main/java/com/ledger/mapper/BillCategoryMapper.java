package com.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.entity.BillCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 账单分类 Mapper 接口
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Mapper
public interface BillCategoryMapper extends BaseMapper<BillCategory> {

}
