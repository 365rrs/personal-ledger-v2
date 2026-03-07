package com.ledger.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.dto.BillConvertDTO;
import com.ledger.dto.BillImportDetailQueryDTO;
import com.ledger.dto.BillImportRecordQueryDTO;
import com.ledger.dto.BillSkipDTO;
import com.ledger.converter.BillImportConverter;
import com.ledger.entity.Bill;
import com.ledger.entity.BillImportDetail;
import com.ledger.entity.BillImportRecord;
import com.ledger.dto.BillImportData;
import com.ledger.dto.BillImportInfo;
import com.ledger.entity.BillImportRecordData;
import com.ledger.exception.BusinessException;
import com.ledger.mapper.BillImportDetailMapper;
import com.ledger.mapper.BillImportRecordMapper;
import com.ledger.mapper.BillMapper;
import com.ledger.service.BillDataCleanService;
import com.ledger.service.BillImportService;
import com.ledger.util.DataHashUtil;
import com.ledger.util.FileParseUtil;
import com.ledger.vo.BillImportDetailVO;
import com.ledger.vo.BillImportRecordVO;
import com.ledger.vo.BillImportStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 账单导入服务实现类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@Service
public class BillImportServiceImpl implements BillImportService {

    @Resource
    private BillImportRecordMapper importRecordMapper;

    @Resource
    private BillImportDetailMapper importDetailMapper;

    @Resource
    private BillMapper billMapper;

    @Resource
    private BillImportConverter converter;

    @Resource
    private BillDataCleanService billDataCleanService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadAndImport(MultipartFile file) {
        // 1. 校验文件
        validateFile(file);

        // 2. 创建导入记录
        BillImportRecord record = createImportRecord(file);
        importRecordMapper.insert(record);

        try {
            // 3. 解析文件
            BillImportData billImportData = parseFile(file);
            List<BillImportRecordData> dataList = billImportData.getRecords();
            record.setTotalCount(dataList.size());

            // 4. 批量导入明细
            List<BillImportDetail> details = importDetails(record.getId(), dataList);

            // 5. 重复检测
            detectDuplicates(details);

            // 6. 更新导入记录状态
            updateRecordStatus(record, details);

        } catch (Exception e) {
            log.error("导入失败", e);
            record.setStatus("FAILED");
            record.setErrorMessage(e.getMessage());
            record.setEndTime(LocalDateTime.now());
            importRecordMapper.updateById(record);
            throw new BusinessException("导入失败: " + e.getMessage());
        }

        return record.getId();
    }

    @Override
    public IPage<BillImportRecordVO> pageRecords(BillImportRecordQueryDTO dto) {
        Page<BillImportRecord> page = new Page<>(dto.getCurrent(), dto.getSize());

        LambdaQueryWrapper<BillImportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(dto.getStatus()), BillImportRecord::getStatus, dto.getStatus())
                .like(StringUtils.isNotBlank(dto.getFileName()), BillImportRecord::getFileName, dto.getFileName())
                .orderByDesc(BillImportRecord::getCreateTime);

        IPage<BillImportRecord> recordPage = importRecordMapper.selectPage(page, wrapper);

        // 转换为 VO
        IPage<BillImportRecordVO> voPage = recordPage.convert(this::convertToRecordVO);

        if (recordPage.getRecords().isEmpty()) {
            return voPage;
        }

        // 批量获取统计信息
        List<Long> recordIds = voPage.getRecords().stream()
                .map(BillImportRecordVO::getId)
                .collect(Collectors.toList());

        // 批量查询每个导入记录的明细统计
        Map<Long, BillImportStatisticsVO> statisticsMap = batchGetStatistics(recordIds);

        // 填充统计信息到 VO
        for (BillImportRecordVO vo : voPage.getRecords()) {
            BillImportStatisticsVO stats = statisticsMap.get(vo.getId());
            if (stats != null) {
                vo.setUniqueCount(stats.getUniqueCount());
                vo.setDuplicateCount(stats.getDuplicateCount());
                vo.setPendingCount(stats.getPendingCount());
                vo.setConvertedCount(stats.getConvertedCount());
            }
        }

        return voPage;
    }

    @Override
    public BillImportRecordVO getRecordById(Long id) {
        BillImportRecord record = importRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("导入记录不存在");
        }
        return convertToRecordVO(record);
    }

    @Override
    public IPage<BillImportDetailVO> pageDetails(BillImportDetailQueryDTO dto) {
        Page<BillImportDetail> page = new Page<>(dto.getCurrent(), dto.getSize());

        LambdaQueryWrapper<BillImportDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillImportDetail::getImportRecordId, dto.getImportRecordId())
                .eq(StringUtils.isNotBlank(dto.getImportStatus()), BillImportDetail::getImportStatus, dto.getImportStatus())
                .eq(StringUtils.isNotBlank(dto.getDuplicateStatus()), BillImportDetail::getDuplicateStatus, dto.getDuplicateStatus())
                .eq(StringUtils.isNotBlank(dto.getConvertStatus()), BillImportDetail::getConvertStatus, dto.getConvertStatus())
                .orderByAsc(BillImportDetail::getId);

        IPage<BillImportDetail> detailPage = importDetailMapper.selectPage(page, wrapper);

        return detailPage.convert(this::convertToDetailVO);
    }

    @Override
    public BillImportStatisticsVO getStatistics(Long importRecordId) {
        LambdaQueryWrapper<BillImportDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillImportDetail::getImportRecordId, importRecordId);

        List<BillImportDetail> details = importDetailMapper.selectList(wrapper);

        BillImportStatisticsVO statistics = new BillImportStatisticsVO();
        statistics.setTotalCount(details.size());
        statistics.setSuccessCount((int) details.stream().filter(d -> "SUCCESS".equals(d.getImportStatus())).count());
        statistics.setFailCount((int) details.stream().filter(d -> "FAILED".equals(d.getImportStatus())).count());
        statistics.setUniqueCount((int) details.stream().filter(d -> "UNIQUE".equals(d.getDuplicateStatus())).count());
        statistics.setDuplicateCount((int) details.stream().filter(d -> "DUPLICATE".equals(d.getDuplicateStatus())).count());
        statistics.setPendingCount((int) details.stream().filter(d -> "PENDING".equals(d.getConvertStatus())).count());
        statistics.setConvertedCount((int) details.stream().filter(d -> "CONVERTED".equals(d.getConvertStatus())).count());

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void convertToBill(BillConvertDTO dto) {
        // 1. 查询明细记录
        List<BillImportDetail> details = importDetailMapper.selectBatchIds(dto.getDetailIds());
        if (details.isEmpty()) {
            throw new BusinessException("未找到导入明细记录");
        }

        // 2. 校验状态
        for (BillImportDetail detail : details) {
            if (!"SUCCESS".equals(detail.getImportStatus())) {
                throw new BusinessException("只能转换导入成功的记录");
            }
            if (!"UNIQUE".equals(detail.getDuplicateStatus())) {
                throw new BusinessException("只能转换唯一记录");
            }
            if (!"PENDING".equals(detail.getConvertStatus())) {
                throw new BusinessException("只能转换待转换状态的记录");
            }
        }

        // 3. 转换前再次检查重复（防止导入后有新账单创建）
        detectDuplicatesBeforeConvert(details);

        // 4. 批量创建账单
        for (BillImportDetail detail : details) {
            // 跳过重复记录
            if ("DUPLICATE".equals(detail.getConvertStatus())) {
                continue;
            }

            try {
                log.info("开始转换明细ID: {}", detail.getId());
                Bill bill = convertDetailToBill(detail);
                log.info("转换后的账单: {}", bill);

                billMapper.insert(bill);
                log.info("账单插入成功, ID: {}", bill.getId());

                // 更新明细状态
                detail.setConvertStatus("CONVERTED");
                detail.setLedgerId(bill.getId());
                importDetailMapper.updateById(detail);
                log.info("明细状态更新成功");

            } catch (Exception e) {
                log.error("转换账单失败, 明细ID: {}", detail.getId(), e);
                detail.setConvertStatus("CONVERT_FAILED");
                detail.setConvertErrorMessage(e.getMessage());
                importDetailMapper.updateById(detail);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void skipRecords(BillSkipDTO dto) {
        List<BillImportDetail> details = importDetailMapper.selectBatchIds(dto.getDetailIds());

        details.forEach(detail -> {
            detail.setConvertStatus("SKIPPED");
        });

        details.forEach(importDetailMapper::updateById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        BillImportRecord record = importRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("导入记录不存在");
        }

        record.setDeleted("1");
        importRecordMapper.updateById(record);
    }

    /**
     * 批量获取统计信息
     */
    private Map<Long, BillImportStatisticsVO> batchGetStatistics(List<Long> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            return new HashMap<>();
        }

        // 批量查询所有明细
        LambdaQueryWrapper<BillImportDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BillImportDetail::getImportRecordId, recordIds);
        List<BillImportDetail> allDetails = importDetailMapper.selectList(wrapper);

        // 按导入记录 ID 分组并计算统计
        return allDetails.stream()
                .collect(Collectors.groupingBy(
                        BillImportDetail::getImportRecordId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                details -> {
                                    BillImportStatisticsVO stats = new BillImportStatisticsVO();
                                    stats.setTotalCount(details.size());
                                    stats.setSuccessCount((int) details.stream()
                                            .filter(d -> "SUCCESS".equals(d.getImportStatus())).count());
                                    stats.setFailCount((int) details.stream()
                                            .filter(d -> "FAILED".equals(d.getImportStatus())).count());
                                    stats.setUniqueCount((int) details.stream()
                                            .filter(d -> "UNIQUE".equals(d.getDuplicateStatus())).count());
                                    stats.setDuplicateCount((int) details.stream()
                                            .filter(d -> "DUPLICATE".equals(d.getDuplicateStatus())).count());
                                    stats.setPendingCount((int) details.stream()
                                            .filter(d -> "PENDING".equals(d.getConvertStatus())).count());
                                    stats.setConvertedCount((int) details.stream()
                                            .filter(d -> "CONVERTED".equals(d.getConvertStatus())).count());
                                    return stats;
                                }
                        )
                ));
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new BusinessException("文件名不能为空");
        }

        // 校验文件格式
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls") && !fileName.endsWith(".csv")) {
            throw new BusinessException("仅支持 Excel 和 CSV 格式文件");
        }

        // 校验文件大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过 10MB");
        }
    }

    /**
     * 创建导入记录
     */
    private BillImportRecord createImportRecord(MultipartFile file) {
        BillImportRecord record = new BillImportRecord();
        record.setFileName(file.getOriginalFilename());
        record.setFileSize(file.getSize());
        record.setTotalCount(0);
        record.setProcessedCount(0);
        record.setSuccessCount(0);
        record.setFailCount(0);
        record.setStatus("PROCESSING");
        record.setStartTime(LocalDateTime.now());
        return record;
    }

    /**
     * 转换为记录VO
     */
    private BillImportRecordVO convertToRecordVO(BillImportRecord record) {
        return converter.toRecordVO(record);
    }

    /**
     * 转换为明细VO
     */
    private BillImportDetailVO convertToDetailVO(BillImportDetail detail) {
        return converter.toDetailVO(detail);
    }

    /**
     * 解析文件（使用 EasyExcel）
     */
    private BillImportData parseFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            BillImportData billImportData = new BillImportData();

            // 读取前6行的导出信息
            String[] headerLines = new String[6];
            for (int i = 0; i < 6; i++) {
                headerLines[i] = reader.readLine();
            }

            // 解析导入信息
            BillImportInfo importInfo = parseImportInfo(headerLines);
            billImportData.setImportInfo(importInfo);

            // 使用 EasyExcel 读取交易记录
            List<BillImportRecordData> importRecords = EasyExcel.read(file.getInputStream())
                    .head(BillImportRecordData.class)
                    .headRowNumber(8)  // 跳过前8行，第9行作为表头
                    .sheet()
                    .doReadSync();

            // 移除最后3行非交易数据
            if (importRecords.size() >= 3) {
                importRecords = importRecords.subList(0, importRecords.size() - 3);
            }

            billImportData.setRecords(importRecords);
            return billImportData;
        } catch (Exception e) {
            log.error("解析文件失败", e);
            throw new BusinessException("解析文件失败: " + e.getMessage());
        }
    }

    /**
     * 解析导入信息
     */
    private BillImportInfo parseImportInfo(String[] headerLines) {
        BillImportInfo info = new BillImportInfo();

        for (String line : headerLines) {
            if (line == null) {
                continue;
            }

            if (line.contains("导出时间:")) {
                info.setExportTime(extractValue(line));
            } else if (line.contains("账    号:")) {
                info.setAccountNumber(extractValue(line));
            } else if (line.contains("币    种:")) {
                info.setCurrency(extractValue(line));
            } else if (line.contains("起始日期:")) {
                String[] parts = line.split("终止日期:");
                if (parts.length >= 1) {
                    info.setStartDate(extractValue(parts[0]));
                }
                if (parts.length >= 2) {
                    info.setEndDate(extractValue(parts[1]));
                }
            }
        }

        return info;
    }

    /**
     * 提取方括号中的值
     */
    private String extractValue(String line) {
        int start = line.indexOf('[');
        int end = line.indexOf(']');
        if (start != -1 && end != -1 && end > start) {
            return line.substring(start + 1, end).trim();
        }
        return "";
    }

    /**
     * 导入明细
     */
    private List<BillImportDetail> importDetails(Long importRecordId, List<BillImportRecordData> dataList) {
        List<BillImportDetail> details = new ArrayList<>();

        for (BillImportRecordData record : dataList) {
            BillImportDetail detail = new BillImportDetail();
            detail.setImportRecordId(importRecordId);
            detail.setOriginalData(JSON.toJSONString(record));
            detail.setDuplicateStatus("UNCHECKED");
            detail.setConvertStatus("PENDING");

            try {
                // 解析字段
                LocalDate transactionDate = FileParseUtil.toLocalDate(record.getTransactionDate());
                LocalTime transactionTime = FileParseUtil.toLocalTime(record.getTransactionTime());
                BigDecimal income = FileParseUtil.toBigDecimal(record.getIncomeAmount());
                BigDecimal expense = FileParseUtil.toBigDecimal(record.getExpenseAmount());
                String description = record.getTransactionDesc();

                // 校验必填字段
                if (transactionDate == null) {
                    throw new BusinessException("交易日期不能为空");
                }
                if (transactionTime == null) {
                    transactionTime = LocalTime.of(0, 0);
                }

                // 判断类型和金额
                String type;
                BigDecimal amount;
                if (income != null && income.compareTo(BigDecimal.ZERO) > 0) {
                    type = "INCOME";
                    amount = income;
                } else if (expense != null && expense.compareTo(BigDecimal.ZERO) > 0) {
                    type = "EXPENSE";
                    amount = expense;
                } else {
                    throw new BusinessException("收入和支出不能同时为空");
                }

                LocalDateTime transactionDateTime = LocalDateTime.of(transactionDate, transactionTime);

                detail.setAmountType(type);
                detail.setAmount(amount);
                detail.setTransactionType(record.getTransactionType()); // 保存原始交易类型
                detail.setDescription(description);
                detail.setTransactionTime(transactionDateTime);

                // 计算数据指纹
                String dataHash = DataHashUtil.calculateHash(type, amount, transactionDateTime, description);
                detail.setDataHash(dataHash);

                detail.setImportStatus("SUCCESS");

            } catch (Exception e) {
                log.error("解析数据失败", e);
                detail.setImportStatus("FAILED");
                detail.setErrorMessage(e.getMessage());
            }

            importDetailMapper.insert(detail);
            details.add(detail);
        }

        return details;
    }

    /**
     * 转换前重复检测（防止导入后有新账单创建）
     */
    private void detectDuplicatesBeforeConvert(List<BillImportDetail> details) {
        // 提取所有数据指纹
        List<String> dataHashes = details.stream()
                .map(BillImportDetail::getDataHash)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (dataHashes.isEmpty()) {
            return;
        }

        // 批量查询已存在的账单
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Bill::getDataHash, dataHashes)
                .eq(Bill::getDeleted, "0");
        List<Bill> existingBills = billMapper.selectList(wrapper);

        // 构建数据指纹到账单ID的映射
        Map<String, Long> hashToBillId = existingBills.stream()
                .collect(Collectors.toMap(Bill::getDataHash, Bill::getId, (a, b) -> a));

        // 更新明细的重复状态
        for (BillImportDetail detail : details) {
            Long duplicateBillId = hashToBillId.get(detail.getDataHash());
            if (duplicateBillId != null) {
                log.warn("明细ID: {} 在转换前检测到重复，重复账单ID: {}", detail.getId(), duplicateBillId);
                detail.setDuplicateStatus("DUPLICATE");
                detail.setDuplicateLedgerId(duplicateBillId);
                detail.setConvertStatus("DUPLICATE");
                importDetailMapper.updateById(detail);
            }
        }
    }

    /**
     * 重复检测
     */
    private void detectDuplicates(List<BillImportDetail> details) {
        // 提取所有数据指纹
        List<String> dataHashes = details.stream()
                .filter(d -> "SUCCESS".equals(d.getImportStatus()))
                .map(BillImportDetail::getDataHash)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (dataHashes.isEmpty()) {
            return;
        }

        // 批量查询已存在的账单
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Bill::getDataHash, dataHashes)
                .eq(Bill::getDeleted, "0");
        List<Bill> existingBills = billMapper.selectList(wrapper);

        // 构建数据指纹到账单ID的映射
        Map<String, Long> hashToBillId = existingBills.stream()
                .collect(Collectors.toMap(Bill::getDataHash, Bill::getId, (a, b) -> a));

        // 更新明细的重复状态
        for (BillImportDetail detail : details) {
            if (!"SUCCESS".equals(detail.getImportStatus())) {
                continue;
            }

            Long duplicateBillId = hashToBillId.get(detail.getDataHash());
            if (duplicateBillId != null) {
                detail.setDuplicateStatus("DUPLICATE");
                detail.setDuplicateLedgerId(duplicateBillId);
                detail.setConvertStatus("DUPLICATE");
            } else {
                detail.setDuplicateStatus("UNIQUE");
            }

            importDetailMapper.updateById(detail);
        }
    }

    /**
     * 更新导入记录状态
     */
    private void updateRecordStatus(BillImportRecord record, List<BillImportDetail> details) {
        int successCount = (int) details.stream().filter(d -> "SUCCESS".equals(d.getImportStatus())).count();
        int failCount = (int) details.stream().filter(d -> "FAILED".equals(d.getImportStatus())).count();

        record.setProcessedCount(details.size());
        record.setSuccessCount(successCount);
        record.setFailCount(failCount);
        record.setEndTime(LocalDateTime.now());

        if (failCount == 0) {
            record.setStatus("SUCCESS");
        } else if (successCount == 0) {
            record.setStatus("FAILED");
        } else {
            record.setStatus("PARTIAL");
        }

        importRecordMapper.updateById(record);
    }

    /**
     * 转换明细为账单
     */
    private Bill convertDetailToBill(BillImportDetail detail) {
        Bill bill = new Bill();

        // 设置交易日期和时间
        bill.setTransactionDate(detail.getTransactionTime().toLocalDate());
        bill.setTransactionTime(detail.getTransactionTime().toLocalTime());

        // 设置金额和类型
        if ("INCOME".equals(detail.getAmountType())) {
            bill.setIncomeAmount(detail.getAmount());
            bill.setAmountType("INCOME");
        } else if ("EXPENSE".equals(detail.getAmountType())) {
            bill.setExpenseAmount(detail.getAmount());
            bill.setAmountType("EXPENSE");
        } else {
            throw new BusinessException("交易类型错误: " + detail.getAmountType());
        }

        // 设置原始交易类型
        bill.setTransactionType(detail.getTransactionType());

        // 应用数据清洗规则
        Map<String, String> billFields = new HashMap<>();
        billFields.put("transaction_type", bill.getAmountType());

        // 清洗分类（必填）
        String category = billDataCleanService.cleanField("CATEGORY", billFields);
        if (category == null) {
            // 如果没有匹配到规则，使用默认分类
            category = "INCOME".equals(bill.getAmountType()) ? "其他收入" : "其他支出";
        }
        bill.setCategory(category);

        // 交易描述
        bill.setTransactionDesc(detail.getDescription());
        // 数据指纹（用于重复检测）
        bill.setDataHash(detail.getDataHash());
        // 是否手工记账：0-否，1-是
        bill.setManualEntry("0");
        // 是否计入收支统计：0-不计入，1-计入
        bill.setIncludeInStatistics("1");

        return bill;
    }
}