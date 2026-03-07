package com.ledger.service;

import java.util.Map;

/**
 * 数据清洗Service
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
}
