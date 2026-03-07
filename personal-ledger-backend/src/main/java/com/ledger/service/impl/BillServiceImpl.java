package com.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.converter.BillConverter;
import com.ledger.dto.BillDTO;
import com.ledger.dto.BillQueryDTO;
import com.ledger.entity.Bill;
import com.ledger.exception.BusinessException;
import com.ledger.mapper.BillMapper;
import com.ledger.service.BillService;
import com.ledger.util.DataHashUtil;
import com.ledger.vo.BillStatisticsVO;
import com.ledger.vo.BillVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
        String type = bill.getAmountType();
        BigDecimal amount = "INCOME".equals(type) ? bill.getIncomeAmount() : bill.getExpenseAmount();
        LocalDateTime transactionDateTime = LocalDateTime.of(
            bill.getTransactionDate(), 
            bill.getTransactionTime() != null ? bill.getTransactionTime() : java.time.LocalTime.of(0, 0)
        );
        String dataHash = DataHashUtil.calculateHash(type, amount, transactionDateTime, bill.getTransactionDesc());
        bill.setDataHash(dataHash);
        
        billMapper.insert(bill);
        return bill.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BillDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("账单ID不能为空");
        }
        
        Bill bill = billMapper.selectById(dto.getId());
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        
        Bill updateBill = converter.toEntity(dto);
        
        // 重新计算数据指纹
        String type = updateBill.getAmountType();
        BigDecimal amount = "INCOME".equals(type) ? updateBill.getIncomeAmount() : updateBill.getExpenseAmount();
        LocalDateTime transactionDateTime = LocalDateTime.of(
            updateBill.getTransactionDate(), 
            updateBill.getTransactionTime() != null ? updateBill.getTransactionTime() : java.time.LocalTime.of(0, 0)
        );
        String dataHash = DataHashUtil.calculateHash(type, amount, transactionDateTime, updateBill.getTransactionDesc());
        updateBill.setDataHash(dataHash);
        
        billMapper.updateById(updateBill);
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
        return converter.toVO(bill);
    }
    
    @Override
    public IPage<BillVO> page(BillQueryDTO dto) {
        Page<Bill> page = new Page<>(dto.getCurrent(), dto.getSize());
        
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(dto.getTransactionType()), Bill::getTransactionType, dto.getTransactionType())
                .eq(StringUtils.isNotBlank(dto.getCategory()), Bill::getCategory, dto.getCategory())
                .eq(StringUtils.isNotBlank(dto.getPaymentChannel()), Bill::getPaymentChannel, dto.getPaymentChannel())
                .eq(StringUtils.isNotBlank(dto.getManualEntry()), Bill::getManualEntry, dto.getManualEntry())
                .ge(dto.getStartDate() != null, Bill::getTransactionDate, dto.getStartDate())
                .le(dto.getEndDate() != null, Bill::getTransactionDate, dto.getEndDate())
                .orderByDesc(Bill::getTransactionDate)
                .orderByDesc(Bill::getTransactionTime);
        
        IPage<Bill> billPage = billMapper.selectPage(page, wrapper);
        return billPage.convert(converter::toVO);
    }
    
    @Override
    public BillStatisticsVO getStatistics(BillQueryDTO dto) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(dto.getTransactionType()), Bill::getTransactionType, dto.getTransactionType())
                .eq(StringUtils.isNotBlank(dto.getCategory()), Bill::getCategory, dto.getCategory())
                .eq(StringUtils.isNotBlank(dto.getPaymentChannel()), Bill::getPaymentChannel, dto.getPaymentChannel())
                .ge(dto.getStartDate() != null, Bill::getTransactionDate, dto.getStartDate())
                .le(dto.getEndDate() != null, Bill::getTransactionDate, dto.getEndDate())
                .eq(Bill::getIncludeInStatistics, "1");
        
        List<Bill> bills = billMapper.selectList(wrapper);
        
        BigDecimal totalIncome = bills.stream()
                .filter(b -> "INCOME".equals(b.getAmountType()))
                .map(Bill::getIncomeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpense = bills.stream()
                .filter(b -> "EXPENSE".equals(b.getAmountType()))
                .map(Bill::getExpenseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BillStatisticsVO statistics = new BillStatisticsVO();
        statistics.setTotalIncome(totalIncome);
        statistics.setTotalExpense(totalExpense);
        statistics.setBalance(totalIncome.subtract(totalExpense));
        
        return statistics;
    }
}
