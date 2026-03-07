package com.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.dto.BillQueryDTO;
import com.ledger.entity.Bill;
import com.ledger.vo.BillStatisticsVO;
import org.apache.ibatis.annotations.Param;

/**
 * 账单Mapper
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
public interface BillMapper extends BaseMapper<Bill> {
    /**
     * 分页查询账单列表
     *
     * @param page 分页对象
     * @param dto  查询条件
     * @return 账单 Entity 列表
     */
    IPage<Bill> selectBillPage(Page<Bill> page, @Param("dto") BillQueryDTO dto);

    /**
     * 查询统计数据
     *
     * @param dto 查询条件
     * @return 统计数据
     */
    BillStatisticsVO getStatistics(@Param("dto") BillQueryDTO dto);
}