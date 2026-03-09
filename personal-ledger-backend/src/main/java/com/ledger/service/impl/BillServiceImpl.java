package com.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.converter.BillConverter;
import com.ledger.dto.BillBatchUpdateDTO;
import com.ledger.dto.BillDailyExpenseQueryDTO;
import com.ledger.dto.BillDTO;
import com.ledger.dto.BillQueryDTO;
import com.ledger.entity.Bill;
import com.ledger.entity.BillTagRelation;
import com.ledger.enums.IncludeInStatisticsEnum;
import com.ledger.exception.BusinessException;
import com.ledger.mapper.BillMapper;
import com.ledger.mapper.BillTagRelationMapper;
import com.ledger.service.BillService;
import com.ledger.util.DataHashUtil;
import com.ledger.vo.BillDailyExpenseVO;
import com.ledger.vo.BillStatisticsVO;
import com.ledger.vo.BillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账单Service实现类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Resource
    private BillMapper billMapper;

    @Resource
    private BillConverter converter;

    @Resource
    private BillTagRelationMapper billTagRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BillDTO dto) {
        Bill bill = converter.toEntity(dto);

        // 设置默认值
        if (bill.getIncludeInStatistics() == null) {
            bill.setIncludeInStatistics("1");
        }
        bill.setManualEntry("1");

        // 计算数据指纹
        String amountType = bill.getAmountType();
        BigDecimal amount = "INCOME".equals(amountType) ? bill.getIncomeAmount() : bill.getExpenseAmount();
        LocalDateTime transactionDateTime = LocalDateTime.of(
                bill.getTransactionDate(),
                bill.getTransactionTime() != null ? bill.getTransactionTime() : java.time.LocalTime.of(0, 0)
        );
        String dataHash = DataHashUtil.calculateHash(amountType, amount, transactionDateTime, bill.getTransactionDesc());
        bill.setDataHash(dataHash);

        billMapper.insert(bill);
        return bill.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BillDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("账单 ID 不能为空");
        }

        Bill bill = billMapper.selectById(dto.getId());
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }

        // 将 DTO 转换为实体类
        Bill updateBill = converter.toEntity(dto);
        
        // 打印日志查看分类字段是否正确传递
        log.info("更新账单 - ID: {}, categoryId: {}, category: {}, subCategoryId: {}, subCategory: {}", 
                dto.getId(), dto.getCategoryId(), dto.getCategory(), dto.getSubCategoryId(), dto.getSubCategory());
        log.info("转换后的实体 - categoryId: {}, category: {}, subCategoryId: {}, subCategory: {}", 
                updateBill.getCategoryId(), updateBill.getCategory(), updateBill.getSubCategoryId(), updateBill.getSubCategory());

        billMapper.updateBill(updateBill);
        
        log.info("账单更新成功 - ID: {}", dto.getId());

        // 处理标签关联关系
        if (dto.getTagIds() != null) {
            updateTagRelations(dto.getId(), dto.getTagIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }

        bill.setDeleted("1");
        billMapper.updateById(bill);
    }

    @Override
    public BillVO getById(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        BillVO vo = converter.toVO(bill);
        // 加载标签 ID 列表
        List<Long> tagIds = loadTagIds(id);
        vo.setTagIds(tagIds);
        return vo;
    }

    @Override
    public IPage<BillVO> page(BillQueryDTO dto) {
        Page<Bill> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<Bill> billPage = billMapper.selectBillPage(page, dto);

        // 转换为 VO 并加载标签信息
        return billPage.convert(bill -> {
            BillVO vo = converter.toVO(bill);
            // 加载标签 ID 列表
            List<Long> tagIds = loadTagIds(bill.getId());
            vo.setTagIds(tagIds);
            return vo;
        });
    }

    @Override
    public BillStatisticsVO getStatistics(BillQueryDTO dto) {
        // 设置默认值：只统计计入收支的数据
        if (dto.getIncludeInStatistics() == null || dto.getIncludeInStatistics().isEmpty()) {
            dto.setIncludeInStatistics(IncludeInStatisticsEnum.YES.getCode());
        }

        // 从数据库查询总收入和总支出
        BillStatisticsVO statistics = billMapper.getStatistics(dto);

        // 在代码中计算结余（收入 - 支出）
        if (statistics != null) {
            BigDecimal balance = statistics.getTotalIncome().subtract(statistics.getTotalExpense());
            statistics.setBalance(balance);
        }

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(BillBatchUpdateDTO dto) {
        log.info("开始批量更新账单 - billIds: {}, size: {}, updateFields: {}", 
                dto.getBillIds(), dto.getBillIds().size(), dto.getUpdateFields());
        
        // 如果没有指定更新字段，则不执行任何操作
        if (dto.getUpdateFields() == null || dto.getUpdateFields().isEmpty()) {
            log.warn("未指定需要更新的字段，跳过批量更新");
            return;
        }
        
        for (Long billId : dto.getBillIds()) {
            try {
                Bill bill = billMapper.selectById(billId);
                if (bill == null) {
                    log.warn("账单不存在，跳过 - id: {}", billId);
                    continue;
                }
                
                // 根据 updateFields 明确更新对应的字段（允许设置为空值）
                if (dto.getUpdateFields().contains("paymentChannel")) {
                    bill.setPaymentChannel(dto.getPaymentChannel());
                }
                
                if (dto.getUpdateFields().contains("categoryId")) {
                    bill.setCategoryId(dto.getCategoryId());
                    bill.setCategory(dto.getCategory());
                    bill.setSubCategoryId(dto.getSubCategoryId());
                    bill.setSubCategory(dto.getSubCategory());
                }
                
                if (dto.getUpdateFields().contains("manualRemark")) {
                    bill.setManualRemark(dto.getManualRemark());
                }
                
                if (dto.getUpdateFields().contains("includeInStatistics")) {
                    bill.setIncludeInStatistics(dto.getIncludeInStatistics());
                }
                
                // 更新账单
                billMapper.updateById(bill);
                
                // 处理标签关联关系（如果提供了标签 ID 列表且在更新字段中）
                if (dto.getUpdateFields().contains("tagIds") && dto.getTagIds() != null) {
                    updateTagRelations(billId, dto.getTagIds());
                }
                
                log.info("账单更新成功 - id: {}", billId);
            } catch (Exception e) {
                log.error("批量更新账单失败 - id: {}", billId, e);
                // 继续处理下一个，不中断整个流程
            }
        }
        
        log.info("批量更新账单完成");
    }

    /**
     * 加载账单的标签 ID 列表
     */
    private List<Long> loadTagIds(Long billId) {
        try {
            List<BillTagRelation> relations = billTagRelationMapper.selectList(
                    new LambdaQueryWrapper<BillTagRelation>()
                            .eq(BillTagRelation::getBillId, billId)
            );
            if (relations == null || relations.isEmpty()) {
                return Collections.emptyList();
            }
            return relations.stream()
                    .map(BillTagRelation::getTagId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("加载标签失败，billId={}", billId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 更新标签关联关系
     *
     * @param billId    账单 ID
     * @param newTagIds 新的标签 ID 列表
     */
    private void updateTagRelations(Long billId, List<Long> newTagIds) {
        try {
            // 查询现有的标签关联关系
            List<BillTagRelation> existingRelations = billTagRelationMapper.selectList(
                    new LambdaQueryWrapper<BillTagRelation>()
                            .eq(BillTagRelation::getBillId, billId)
            );

            Set<Long> existingTagIds = existingRelations != null
                    ? existingRelations.stream().map(BillTagRelation::getTagId).collect(Collectors.toSet())
                    : Collections.emptySet();

            Set<Long> newTagIdSet = newTagIds != null ? new HashSet<>(newTagIds) : Collections.emptySet();

            // 需要删除的标签（在现有中但不在新列表中）
            Set<Long> toRemove = new HashSet<>(existingTagIds);
            toRemove.removeAll(newTagIdSet);

            // 需要新增的标签（在新列表中但不在现有中）
            Set<Long> toAdd = new HashSet<>(newTagIdSet);
            toAdd.removeAll(existingTagIds);

            // 删除不需要的标签关联
            for (Long tagId : toRemove) {
                BillTagRelation relation = existingRelations.stream()
                        .filter(r -> r.getTagId().equals(tagId))
                        .findFirst()
                        .orElse(null);
                if (relation != null) {
                    billTagRelationMapper.deleteById(relation.getId());
                }
            }

            // 添加新的标签关联
            for (Long tagId : toAdd) {
                // 先检查是否已存在，避免重复插入
                BillTagRelation exists = billTagRelationMapper.selectOne(
                        new LambdaQueryWrapper<BillTagRelation>()
                                .eq(BillTagRelation::getBillId, billId)
                                .eq(BillTagRelation::getTagId, tagId)
                );
                if (exists == null) {
                    BillTagRelation relation = new BillTagRelation();
                    relation.setBillId(billId);
                    relation.setTagId(tagId);
                    billTagRelationMapper.insert(relation);
                }
            }

        } catch (Exception e) {
            log.error("更新标签关联关系失败，billId={}", billId, e);
            throw new BusinessException("更新标签失败：" + e.getMessage());
        }
    }

    @Override
    public List<BillDailyExpenseVO> getDailyExpense(BillDailyExpenseQueryDTO dto) {
        // 默认查询当前月份
        YearMonth yearMonth = YearMonth.now();
        if (dto.getYear() != null && dto.getMonth() != null) {
            yearMonth = YearMonth.of(dto.getYear(), dto.getMonth());
        }
        
        return billMapper.selectDailyExpense(yearMonth.getYear(), yearMonth.getMonthValue());
    }
}
