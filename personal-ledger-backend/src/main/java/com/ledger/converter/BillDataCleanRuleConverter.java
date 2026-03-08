package com.ledger.converter;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.dto.BillDataCleanRuleDTO;
import com.ledger.entity.BillDataCleanRule;
import com.ledger.vo.BillDataCleanRuleVO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据清洗规则转换器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Component
public class BillDataCleanRuleConverter {

    /**
     * DTO 转 Entity
     */
    public BillDataCleanRule toEntity(BillDataCleanRuleDTO dto) {
        BillDataCleanRule rule = new BillDataCleanRule();
        rule.setId(dto.getId());
        rule.setRuleType(dto.getRuleType());
        rule.setMatchFields(dto.getMatchFields());
        rule.setTargetValue(dto.getTargetValue());
        rule.setPriority(dto.getPriority());
        rule.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : "1");
        rule.setRemark(dto.getRemark());
        return rule;
    }

    /**
     * Entity 转 VO
     */
    public BillDataCleanRuleVO toVO(BillDataCleanRule rule) {
        BillDataCleanRuleVO vo = new BillDataCleanRuleVO();
        vo.setId(rule.getId());
        vo.setRuleType(rule.getRuleType());
        vo.setRuleTypeName(getRuleTypeName(rule.getRuleType()));
        vo.setMatchFields(rule.getMatchFields());
        
        // 解析 match_fields JSON
        try {
            Map<String, String> matchFieldsMap = JSON.parseObject(
                rule.getMatchFields(), 
                new com.alibaba.fastjson.TypeReference<Map<String, String>>() {}
            );
            vo.setMatchFieldsMap(matchFieldsMap);
        } catch (Exception e) {
            vo.setMatchFieldsMap(new HashMap<>());
        }
        
        vo.setTargetValue(rule.getTargetValue());
        vo.setPriority(rule.getPriority());
        vo.setEnabled(rule.getEnabled());
        vo.setRemark(rule.getRemark());
        vo.setCreatorCode(rule.getCreatorCode());
        vo.setUpdaterCode(rule.getUpdaterCode());
        vo.setCreatorName(rule.getCreatorName());
        vo.setUpdaterName(rule.getUpdaterName());
        vo.setCreateTime(rule.getCreateTime());
        vo.setUpdateTime(rule.getUpdateTime());
        return vo;
    }

    /**
     * 分页转换
     */
    public IPage<BillDataCleanRuleVO> toVOPage(IPage<BillDataCleanRule> page) {
        return page.convert(this::toVO);
    }

    /**
     * 获取规则类型中文名称
     */
    private String getRuleTypeName(String ruleType) {
        if (ruleType == null) {
            return "";
        }
        switch (ruleType) {
            case "PAYMENT_CHANNEL":
                return "支付渠道";
            case "CATEGORY":
                return "分类";
            case "MANUAL_REMARK":
                return "用户备注";
            default:
                return ruleType;
        }
    }
}
