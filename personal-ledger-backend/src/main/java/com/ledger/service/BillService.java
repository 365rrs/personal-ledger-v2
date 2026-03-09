package com.ledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.dto.BillBatchUpdateDTO;
import com.ledger.dto.BillCategoryStatisticsQueryDTO;
import com.ledger.dto.BillCumulativeExpenseQueryDTO;
import com.ledger.dto.BillDailyExpenseQueryDTO;
import com.ledger.dto.BillDTO;
import com.ledger.dto.BillQueryDTO;
import com.ledger.vo.BillCategoryStatisticsVO;
import com.ledger.vo.BillCumulativeExpenseVO;
import com.ledger.vo.BillDailyExpenseVO;
import com.ledger.vo.BillStatisticsVO;
import com.ledger.vo.BillVO;

import java.util.List;

/**
 * 账单Service
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
public interface BillService {
    
    /**
     * 创建账单
     */
    Long create(BillDTO dto);
    
    /**
     * 更新账单
     */
    void update(BillDTO dto);
    
    /**
     * 删除账单
     */
    void delete(Long id);
    
    /**
     * 查询账单详情
     */
    BillVO getById(Long id);
    
    /**
     * 分页查询账单
     */
    IPage<BillVO> page(BillQueryDTO dto);
    
    /**
     * 查询统计数据
     */
    BillStatisticsVO getStatistics(BillQueryDTO dto);
    
    /**
     * 批量更新账单
     */
    void batchUpdate(BillBatchUpdateDTO dto);
    
    /**
     * 查询每日支出
     */
    List<BillDailyExpenseVO> getDailyExpense(BillDailyExpenseQueryDTO dto);
    
    /**
     * 查询累计支出
     */
    List<BillCumulativeExpenseVO> getCumulativeExpense(BillCumulativeExpenseQueryDTO dto);
    
    /**
     * 按分类统计
     */
    List<BillCategoryStatisticsVO> getCategoryStatistics(BillCategoryStatisticsQueryDTO dto);
}
