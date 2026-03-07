package com.ledger.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ledger.entity.BillDataCleanRule;
import com.ledger.mapper.BillDataCleanRuleMapper;
import com.ledger.service.BillDataCleanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 数据清洗Service实现类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@Service
public class BillDataCleanServiceImpl implements BillDataCleanService {
    
    @Resource
    private BillDataCleanRuleMapper billDataCleanRuleMapper;
    
    @Override
    public String cleanField(String ruleType, Map<String, String> billFields) {
        // 查询所有启用的规则，按优先级倒序
        LambdaQueryWrapper<BillDataCleanRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillDataCleanRule::getRuleType, ruleType)
                .eq(BillDataCleanRule::getEnabled, "1")
                .orderByDesc(BillDataCleanRule::getPriority);
        
        List<BillDataCleanRule> rules = billDataCleanRuleMapper.selectList(wrapper);
        
        // 遍历规则进行匹配
        for (BillDataCleanRule rule : rules) {
            Map<String, String> matchFields = JSON.parseObject(
                rule.getMatchFields(), 
                new TypeReference<Map<String, String>>() {}
            );
            
            // 检查所有字段是否匹配
            boolean allMatch = true;
            for (Map.Entry<String, String> entry : matchFields.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                
                if (!fieldValue.equals(billFields.get(fieldName))) {
                    allMatch = false;
                    break;
                }
            }
            
            if (allMatch) {
                return rule.getTargetValue();
            }
        }
        
        return null;
    }
}
