package com.ledger.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.converter.BillDataCleanRuleConverter;
import com.ledger.dto.BillDataCleanRuleDTO;
import com.ledger.dto.BillDataCleanRuleQueryDTO;
import com.ledger.entity.Bill;
import com.ledger.entity.BillCategory;
import com.ledger.entity.BillDataCleanRule;
import com.ledger.mapper.BillDataCleanRuleMapper;
import com.ledger.service.BillDataCleanService;
import com.ledger.vo.BillDataCleanRuleVO;
import com.ledger.vo.BillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据清洗 Service 实现类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@Service
public class BillDataCleanServiceImpl implements BillDataCleanService {

    @Resource
    private BillDataCleanRuleMapper billDataCleanRuleMapper;

    @Resource
    private BillDataCleanRuleConverter converter;

    @Resource
    private com.ledger.mapper.BillMapper billMapper;

    @Resource
    private com.ledger.converter.BillConverter billConverter;

    @Resource
    private com.ledger.mapper.BillCategoryMapper billCategoryMapper;

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
            MatchFieldsConfig matchFieldsConfig = parseMatchFields(rule.getMatchFields());

            // 检查所有字段是否匹配
            boolean allMatch = true;
            for (MatchField field : matchFieldsConfig.getFields()) {
                String fieldName = field.getName();
                String fieldValue = field.getValue();
                String matchMode = field.getMatchMode();

                String actualValue = billFields.get(fieldName);
                if (actualValue == null) {
                    allMatch = false;
                    break;
                }

                // 根据匹配模式进行匹配
                if (!matchByMode(actualValue, fieldValue, matchMode)) {
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

    @Override
    public void cleanBill(Bill bill) {
        log.info("开始清洗账单数据");

        // 提取账单字段到 Map
        Map<String, String> billFields = extractBillFields(bill);

        // 1. 清洗分类（必填）
        String category = cleanField("CATEGORY", billFields);
        if (category == null) {
            // 如果没有匹配到规则，使用默认分类
            category = "INCOME".equals(bill.getAmountType()) ? "其他收入" : "其他支出";
        }

        // 根据分类名称查询分类 ID
        Long categoryId = findCategoryIdByCategoryName(category, bill.getAmountType());
        if (categoryId != null) {
            bill.setCategory(category);
            bill.setCategoryId(categoryId);
            log.debug("分类清洗结果：{} (ID: {})", category, categoryId);
        } else {
            // 如果找不到分类 ID，只保存名称
            bill.setCategory(category);
            log.warn("未找到分类 '{}' 的 ID，仅保存名称", category);
        }

        // 2. 清洗支付渠道（可选）
        String paymentChannel = cleanField("PAYMENT_CHANNEL", billFields);
        if (paymentChannel != null) {
            bill.setPaymentChannel(paymentChannel);
            log.debug("支付渠道清洗结果：{}", paymentChannel);
        }

        // 3. 清洗用户备注（可选）
        String manualRemark = cleanField("MANUAL_REMARK", billFields);
        if (manualRemark != null) {
            bill.setManualRemark(manualRemark);
            log.debug("用户备注清洗结果：{}", manualRemark);
        }

        log.info("账单数据清洗完成");
    }

    @Override
    public BillVO testCleanBill(Long billId) {
        log.info("=== 开始测试清洗账单 - ID: {} ===", billId);

        // 查询原始账单
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new RuntimeException("账单不存在 - ID: " + billId);
        }
        log.info("原始账单数据：{}", bill);

        // 保存原始值用于对比
        String originalCategory = bill.getCategory();
        String originalPaymentChannel = bill.getPaymentChannel();
        String originalManualRemark = bill.getManualRemark();

        // 执行清洗
        cleanBill(bill);
        log.info("清洗后账单数据：{}", bill);

        // 转换为 VO 返回
        BillVO vo = billConverter.toVO(bill);

        // 添加清洗对比信息
        vo.setOriginalCategory(originalCategory);
        vo.setOriginalPaymentChannel(originalPaymentChannel);
        vo.setOriginalManualRemark(originalManualRemark);

        log.info("=== 账单清洗测试完成 - ID: {} ===", billId);
        return vo;
    }

    /**
     * 根据分类名称查询分类 ID
     *
     * @param categoryName 分类名称
     * @param amountType   账单类型（INCOME/EXPENSE）
     * @return 分类 ID，如果找不到返回 null
     */
    private Long findCategoryIdByCategoryName(String categoryName, String amountType) {
        if (categoryName == null || amountType == null) {
            return null;
        }

        // 确定分类类型
        String categoryType = "INCOME".equals(amountType) ? "INCOME" : "EXPENSE";

        // 查询一级分类
        LambdaQueryWrapper<BillCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillCategory::getCategoryName, categoryName)
                .eq(BillCategory::getCategoryType, categoryType)
                .isNull(BillCategory::getParentId)  // 只查一级分类
                .eq(BillCategory::getEnabled, "1")  // 只查启用的分类
                .last("LIMIT 1");

        BillCategory category = billCategoryMapper.selectOne(wrapper);

        if (category != null) {
            log.debug("找到分类：{} (ID: {}, Type: {})", category.getCategoryName(), category.getId(), category.getCategoryType());
            return category.getId();
        } else {
            log.debug("未找到分类：{}, Type: {}", categoryName, categoryType);
            return null;
        }
    }

    /**
     * 提取账单字段到 Map
     */
    private Map<String, String> extractBillFields(Bill bill) {
        Map<String, String> fields = new HashMap<>();

        // 使用驼峰命名（与 Bill 实体字段名一致）
        if (bill.getAmountType() != null) {
            fields.put("amountType", bill.getAmountType());
        }
        if (bill.getPaymentChannel() != null) {
            fields.put("paymentChannel", bill.getPaymentChannel());
        }
        if (bill.getTransactionType() != null) {
            fields.put("transactionType", bill.getTransactionType());
        }
        if (bill.getCategory() != null) {
            fields.put("category", bill.getCategory());
        }
        if (bill.getCategoryId() != null) {
            fields.put("categoryId", bill.getCategoryId().toString());
        }
        if (bill.getManualRemark() != null) {
            fields.put("manualRemark", bill.getManualRemark());
        }
        if (bill.getTransactionDesc() != null) {
            fields.put("transactionDesc", bill.getTransactionDesc());
        }

        log.debug("提取的账单字段：{}", fields);
        return fields;
    }

    /**
     * 解析匹配字段配置
     */
    private MatchFieldsConfig parseMatchFields(String matchFieldsJson) {
        try {
            // 尝试解析新格式
            MatchFieldsConfig config = JSON.parseObject(matchFieldsJson, MatchFieldsConfig.class);
            if (config != null && config.getFields() != null) {
                return config;
            }
        } catch (Exception e) {
            log.warn("解析新格式失败，尝试旧格式：{}", e.getMessage());
        }

        // 兼容旧格式
        MatchFieldsConfig config = new MatchFieldsConfig();
        config.setFields(new java.util.ArrayList<>());

        try {
            Map<String, String> oldFormat = JSON.parseObject(matchFieldsJson,
                    new com.alibaba.fastjson.TypeReference<Map<String, String>>() {
                    });
            if (oldFormat != null) {
                for (Map.Entry<String, String> entry : oldFormat.entrySet()) {
                    MatchField field = new MatchField();
                    field.setName(entry.getKey());
                    field.setValue(entry.getValue());
                    field.setMatchMode("exact"); // 旧格式默认精确匹配
                    config.getFields().add(field);
                }
            }
        } catch (Exception e) {
            log.error("解析旧格式失败：{}", e.getMessage());
        }

        return config;
    }

    /**
     * 根据匹配模式进行匹配
     */
    private boolean matchByMode(String actualValue, String expectedValue, String matchMode) {
        if (actualValue == null || expectedValue == null) {
            return false;
        }

        switch (matchMode) {
            case "exact":
                // 精确匹配：完全相等
                return actualValue.equals(expectedValue);
            case "contains":
                // 包含匹配：包含关键词
                return actualValue.contains(expectedValue);
            case "regex":
                // 正则匹配
                try {
                    return actualValue.matches(expectedValue);
                } catch (Exception e) {
                    log.error("正则表达式匹配失败：{}", e.getMessage());
                    return false;
                }
            default:
                // 默认精确匹配
                return actualValue.equals(expectedValue);
        }
    }

    /**
     * 匹配字段配置类
     */
    @lombok.Data
    private static class MatchFieldsConfig {
        private java.util.List<MatchField> fields;
    }

    /**
     * 单个匹配字段
     */
    @lombok.Data
    private static class MatchField {
        private String name;
        private String matchMode;
        private String value;
    }

    @Override
    public IPage<BillDataCleanRuleVO> pageRules(BillDataCleanRuleQueryDTO dto) {
        Page<BillDataCleanRule> page = new Page<>(dto.getCurrent(), dto.getSize());

        LambdaQueryWrapper<BillDataCleanRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dto.getRuleType()), BillDataCleanRule::getRuleType, dto.getRuleType())
                .eq(StringUtils.hasText(dto.getEnabled()), BillDataCleanRule::getEnabled, dto.getEnabled())
                .and(StringUtils.hasText(dto.getKeywords()), w ->
                        w.like(BillDataCleanRule::getTargetValue, dto.getKeywords())
                                .or()
                                .like(BillDataCleanRule::getRemark, dto.getKeywords())
                )
                .orderByDesc(BillDataCleanRule::getPriority);

        IPage<BillDataCleanRule> rulePage = billDataCleanRuleMapper.selectPage(page, wrapper);
        return converter.toVOPage(rulePage);
    }

    @Override
    public BillDataCleanRuleVO getRuleById(Long id) {
        log.info("查询清洗规则详情 - ID: {}", id);
        BillDataCleanRule rule = billDataCleanRuleMapper.selectById(id);
        if (rule == null) {
            log.error("规则不存在 - ID: {}", id);
            throw new RuntimeException("规则不存在");
        }
        log.info("查询到的规则：{}", rule);
        BillDataCleanRuleVO vo = converter.toVO(rule);
        log.info("转换后的 VO: {}", vo);
        log.info("VO 中的 matchFieldsMap: {}", vo.getMatchFieldsMap());
        return vo;
    }

    @Override
    public Long createRule(BillDataCleanRuleDTO dto) {
        BillDataCleanRule rule = converter.toEntity(dto);
        billDataCleanRuleMapper.insert(rule);
        log.info("创建清洗规则成功 - ID: {}, 类型：{}", rule.getId(), rule.getRuleType());
        return rule.getId();
    }

    @Override
    public void updateRule(BillDataCleanRuleDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("规则 ID 不能为空");
        }

        BillDataCleanRule existRule = billDataCleanRuleMapper.selectById(dto.getId());
        if (existRule == null) {
            throw new RuntimeException("规则不存在");
        }

        BillDataCleanRule rule = converter.toEntity(dto);
        billDataCleanRuleMapper.updateById(rule);
        log.info("更新清洗规则成功 - ID: {}", dto.getId());
    }

    @Override
    public void deleteRule(Long id) {
        BillDataCleanRule rule = billDataCleanRuleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }

        billDataCleanRuleMapper.deleteById(id);
        log.info("删除清洗规则成功 - ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCleanBills(List<Long> billIds) {
        log.info("开始批量清洗账单，账单 ID 列表：{}", billIds);

        if (billIds == null || billIds.isEmpty()) {
            log.warn("账单 ID 列表为空，跳过批量清洗");
            return 0;
        }

        int successCount = 0;

        for (Long billId : billIds) {
            try {
                // 查询账单
                Bill bill = billMapper.selectById(billId);
                if (bill == null) {
                    log.warn("账单不存在，跳过 - id: {}", billId);
                    continue;
                }

                // 如果是手工记账，则不清洗（只清洗导入的账单）
                if ("1".equals(bill.getManualEntry())) {
                    log.info("账单为手工记账，跳过清洗 - id: {}", billId);
                    continue;
                }

                // 执行清洗
                cleanBill(bill);

                // 保存清洗结果
                billMapper.updateById(bill);
                successCount++;

                log.info("账单清洗成功 - id: {}", billId);
            } catch (Exception e) {
                log.error("账单清洗失败 - id: {}", billId, e);
                // 继续处理下一个，不中断整个流程
            }
        }

        log.info("批量清洗完成，成功清洗 {} 条账单", successCount);
        return successCount;
    }
}
