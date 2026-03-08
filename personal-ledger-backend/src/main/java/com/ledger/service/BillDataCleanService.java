package com.ledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.dto.BillDataCleanRuleDTO;
import com.ledger.dto.BillDataCleanRuleQueryDTO;
import com.ledger.entity.Bill;
import com.ledger.vo.BillDataCleanRuleVO;

import java.util.List;
import java.util.Map;

/**
 * 数据清洗 Service
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
public interface BillDataCleanService {
    
    /**
     * 清洗字段
     *
     * @param ruleType 规则类型
     * @param billFields 账单字段
     * @return 清洗后的值
     */
    String cleanField(String ruleType, Map<String, String> billFields);
    
    /**
     * 清洗账单数据（完整流程）
     *
     * @param bill 账单对象
     */
    void cleanBill(Bill bill);
    
    /**
     * 根据账单 ID 清洗并返回清洗结果（用于测试验证）
     *
     * @param billId 账单 ID
     * @return 清洗后的账单 VO
     */
    com.ledger.vo.BillVO testCleanBill(Long billId);
    
    /**
     * 分页查询清洗规则
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    IPage<BillDataCleanRuleVO> pageRules(BillDataCleanRuleQueryDTO dto);
    
    /**
     * 根据 ID 查询清洗规则
     *
     * @param id 规则 ID
     * @return 清洗规则 VO
     */
    BillDataCleanRuleVO getRuleById(Long id);
    
    /**
     * 创建清洗规则
     *
     * @param dto 清洗规则 DTO
     * @return 规则 ID
     */
    Long createRule(BillDataCleanRuleDTO dto);
    
    /**
     * 更新清洗规则
     *
     * @param dto 清洗规则 DTO
     */
    void updateRule(BillDataCleanRuleDTO dto);
    
    /**
     * 删除清洗规则
     *
     * @param id 规则 ID
     */
    void deleteRule(Long id);
    
    /**
     * 批量清洗账单
     *
     * @param billIds 账单 ID 列表
     * @return 清洗成功的账单数量
     */
    int batchCleanBills(List<Long> billIds);
}
