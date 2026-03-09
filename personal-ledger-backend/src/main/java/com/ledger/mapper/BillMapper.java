package com.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.dto.BillQueryDTO;
import com.ledger.entity.Bill;
import com.ledger.vo.BillCategoryStatisticsVO;
import com.ledger.vo.BillCumulativeExpenseVO;
import com.ledger.vo.BillDailyExpenseVO;
import com.ledger.vo.BillStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账单Mapper
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Mapper
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

    /**
     * 更新账单（动态更新非空字段）
     *
     * @param bill 账单实体
     * @return 影响行数
     */
    int updateBill(Bill bill);

    /**
     * 查询每日支出
     *
     * @param year  年份
     * @param month 月份
     * @return 每日支出列表
     */
    List<BillDailyExpenseVO> selectDailyExpense(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 查询累计支出
     *
     * @param year  年份
     * @param month 月份
     * @return 累计支出列表
     */
    List<BillCumulativeExpenseVO> selectCumulativeExpense(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 按分类统计
     *
     * @param year       年份
     * @param month      月份（可选）
     * @param amountType 收支类型
     * @return 分类统计列表
     */
    List<BillCategoryStatisticsVO> selectCategoryStatistics(@Param("year") Integer year, 
                                                             @Param("month") Integer month, 
                                                             @Param("amountType") String amountType);
}