package com.ledger.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.converter.PendingExpenseConverter;
import com.ledger.dto.PendingExpenseDTO;
import com.ledger.dto.PendingExpenseImportDTO;
import com.ledger.dto.PendingExpenseQueryDTO;
import com.ledger.dto.RecurringExpenseDTO;
import com.ledger.entity.BillCategory;
import com.ledger.entity.BillPaymentChannel;
import com.ledger.entity.PendingExpense;
import com.ledger.enums.ExpenseStatusEnum;
import com.ledger.enums.PeriodEnum;
import com.ledger.enums.PlanTypeEnum;
import com.ledger.exception.BusinessException;
import com.ledger.mapper.BillCategoryMapper;
import com.ledger.mapper.BillPaymentChannelMapper;
import com.ledger.mapper.PendingExpenseMapper;
import com.ledger.service.PendingExpenseService;
import com.ledger.vo.BatchOperationResult;
import com.ledger.vo.PendingExpenseStatisticsVO;
import com.ledger.vo.PendingExpenseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待支出Service实现类
 *
 * @author personal-ledger
 * @date 2025-01-15
 */
@Slf4j
@Service
public class PendingExpenseServiceImpl implements PendingExpenseService {
    
    @Resource
    private PendingExpenseMapper pendingExpenseMapper;
    
    @Resource
    private PendingExpenseConverter converter;
    
    @Resource
    private BillCategoryMapper billCategoryMapper;
    
    @Resource
    private BillPaymentChannelMapper billPaymentChannelMapper;
    
    // ================== 基础 CRUD ==================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PendingExpenseDTO dto) {
        log.info("开始创建待支出项目 - expenseName: {}, amount: {}", dto.getExpenseName(), dto.getAmount());
        
        // 验证分类ID存在性（如果提供了分类ID）
        if (dto.getCategoryId() != null) {
            BillCategory category = billCategoryMapper.selectById(dto.getCategoryId());
            if (category == null) {
                log.error("分类不存在 - categoryId: {}", dto.getCategoryId());
                throw new BusinessException("分类不存在");
            }
        }
        
        // DTO 转 Entity
        PendingExpense pendingExpense = converter.toEntity(dto);
        
        // 设置默认状态为 PENDING
        pendingExpense.setStatus(ExpenseStatusEnum.PENDING.getCode());
        
        // 保存到数据库
        int result = pendingExpenseMapper.insert(pendingExpense);
        
        if (result > 0) {
            log.info("创建待支出项目成功 - id: {}", pendingExpense.getId());
            return pendingExpense.getId();
        } else {
            log.error("创建待支出项目失败");
            throw new BusinessException("创建待支出项目失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PendingExpenseDTO dto) {
        log.info("开始更新待支出项目 - id: {}, expenseName: {}", id, dto.getExpenseName());
        
        // 验证记录存在
        PendingExpense existing = pendingExpenseMapper.selectById(id);
        if (existing == null) {
            log.error("待支出项目不存在 - id: {}", id);
            throw new BusinessException("待支出项目不存在");
        }
        
        // 验证分类ID存在性（如果提供了分类ID）
        if (dto.getCategoryId() != null) {
            BillCategory category = billCategoryMapper.selectById(dto.getCategoryId());
            if (category == null) {
                log.error("分类不存在 - categoryId: {}", dto.getCategoryId());
                throw new BusinessException("分类不存在");
            }
        }
        
        // DTO 转 Entity
        PendingExpense pendingExpense = converter.toEntity(dto);
        pendingExpense.setId(id);
        
        // 更新数据库（MyBatis Plus会自动记录更新时间和更新人）
        int result = pendingExpenseMapper.updateById(pendingExpense);
        
        if (result > 0) {
            log.info("更新待支出项目成功 - id: {}", id);
        } else {
            log.error("更新待支出项目失败 - id: {}", id);
            throw new BusinessException("更新待支出项目失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info("开始删除待支出项目 - id: {}", id);
        
        // 验证记录存在
        PendingExpense existing = pendingExpenseMapper.selectById(id);
        if (existing == null) {
            log.error("待支出项目不存在 - id: {}", id);
            throw new BusinessException("待支出项目不存在");
        }
        
        // 逻辑删除（MyBatis Plus 的 deleteById 会自动处理 @TableLogic 注解）
        int result = pendingExpenseMapper.deleteById(id);
        
        if (result > 0) {
            log.info("删除待支出项目成功 - id: {}", id);
        } else {
            log.error("删除待支出项目失败 - id: {}", id);
            throw new BusinessException("删除待支出项目失败");
        }
    }
    
    @Override
    public PendingExpenseVO getById(Long id) {
        log.info("查询待支出项目详情 - id: {}", id);
        
        // 查询 Entity
        PendingExpense pendingExpense = pendingExpenseMapper.selectById(id);
        if (pendingExpense == null) {
            log.error("待支出项目不存在 - id: {}", id);
            throw new BusinessException("待支出项目不存在");
        }
        
        // Entity 转 VO
        PendingExpenseVO vo = converter.toVO(pendingExpense);
        
        // 关联查询分类名称
        if (pendingExpense.getCategoryId() != null) {
            BillCategory category = billCategoryMapper.selectById(pendingExpense.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        
        // 设置中文名称（使用枚举类）
        PeriodEnum periodEnum = PeriodEnum.getByCode(pendingExpense.getPeriod());
        if (periodEnum != null) {
            vo.setPeriodName(periodEnum.getDesc());
        }
        
        PlanTypeEnum planTypeEnum = PlanTypeEnum.getByCode(pendingExpense.getPlanType());
        if (planTypeEnum != null) {
            vo.setPlanTypeName(planTypeEnum.getDesc());
        }
        
        ExpenseStatusEnum statusEnum = ExpenseStatusEnum.getByCode(pendingExpense.getStatus());
        if (statusEnum != null) {
            vo.setStatusName(statusEnum.getDesc());
        }
        
        log.info("查询待支出项目详情成功 - id: {}", id);
        return vo;
    }
    
    // ================== 周期性支出批量创建 ==================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createRecurring(RecurringExpenseDTO dto) {
        log.info("开始批量创建周期性支出 - expenseName: {}, year: {}, months: {}, days: {}", 
                 dto.getExpenseName(), dto.getYear(), dto.getMonths(), dto.getDays());
        
        // 验证分类ID存在性（如果提供了分类ID）
        if (dto.getCategoryId() != null) {
            BillCategory category = billCategoryMapper.selectById(dto.getCategoryId());
            if (category == null) {
                log.error("分类不存在 - categoryId: {}", dto.getCategoryId());
                throw new BusinessException("分类不存在");
            }
        }
        
        // 生成月份和日期的笛卡尔积，创建待支出记录列表
        List<PendingExpense> expenseList = new ArrayList<>();
        
        for (Integer month : dto.getMonths()) {
            for (Integer day : dto.getDays()) {
                // 验证日期有效性（避免2月30日、4月31日等无效日期）
                LocalDate paymentDate;
                try {
                    paymentDate = LocalDate.of(dto.getYear(), month, day);
                } catch (Exception e) {
                    // 跳过无效日期（如2月30日、4月31日等）
                    log.warn("跳过无效日期 - year: {}, month: {}, day: {}", dto.getYear(), month, day);
                    continue;
                }
                
                // 创建待支出记录
                PendingExpense expense = new PendingExpense();
                expense.setExpenseName(dto.getExpenseName());
                expense.setAmount(dto.getAmount());
                expense.setPaymentDate(paymentDate);
                expense.setPeriod(dto.getPeriod());
                expense.setPlanType(dto.getPlanType());
                expense.setStatus(ExpenseStatusEnum.PENDING.getCode());
                expense.setCategoryId(dto.getCategoryId());
                expense.setRemark(dto.getRemark());
                
                expenseList.add(expense);
            }
        }
        
        if (expenseList.isEmpty()) {
            log.error("没有生成有效的待支出记录 - 所有日期组合都无效");
            throw new BusinessException("没有生成有效的待支出记录，请检查日期选择");
        }
        
        // 使用 MyBatis Plus 批量插入（性能优化，每批次1000条）
        int batchSize = 1000;
        int totalInserted = 0;
        List<Long> ids = new ArrayList<>();
        
        for (int i = 0; i < expenseList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, expenseList.size());
            List<PendingExpense> batch = expenseList.subList(i, end);
            
            // 批量插入当前批次
            for (PendingExpense expense : batch) {
                int result = pendingExpenseMapper.insert(expense);
                if (result > 0) {
                    ids.add(expense.getId());
                    totalInserted++;
                } else {
                    log.error("插入待支出记录失败 - expenseName: {}, paymentDate: {}", 
                             expense.getExpenseName(), expense.getPaymentDate());
                }
            }
        }
        
        if (ids.isEmpty()) {
            log.error("批量创建周期性支出失败 - 所有记录插入失败");
            throw new BusinessException("批量创建周期性支出失败");
        }
        
        log.info("批量创建周期性支出成功 - 总数: {}, 成功: {}, IDs: {}", expenseList.size(), totalInserted, ids);
        return ids;
    }
    
    // ================== 状态管理 ==================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsCompleted(Long id) {
        log.info("开始标记待支出项目为已完成 - id: {}", id);
        
        // 验证记录存在
        PendingExpense existing = pendingExpenseMapper.selectById(id);
        if (existing == null) {
            log.error("待支出项目不存在 - id: {}", id);
            throw new BusinessException("待支出项目不存在");
        }
        
        // 更新状态为 COMPLETED，记录完成时间
        PendingExpense update = new PendingExpense();
        update.setId(id);
        update.setStatus(ExpenseStatusEnum.COMPLETED.getCode());
        update.setCompletedTime(LocalDateTime.now());
        
        int result = pendingExpenseMapper.updateById(update);
        
        if (result > 0) {
            log.info("标记待支出项目为已完成成功 - id: {}", id);
        } else {
            log.error("标记待支出项目为已完成失败 - id: {}", id);
            throw new BusinessException("标记待支出项目为已完成失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsCancelled(Long id) {
        log.info("开始标记待支出项目为已取消 - id: {}", id);
        
        // 验证记录存在
        PendingExpense existing = pendingExpenseMapper.selectById(id);
        if (existing == null) {
            log.error("待支出项目不存在 - id: {}", id);
            throw new BusinessException("待支出项目不存在");
        }
        
        // 更新状态为 CANCELLED，记录取消时间
        PendingExpense update = new PendingExpense();
        update.setId(id);
        update.setStatus(ExpenseStatusEnum.CANCELLED.getCode());
        update.setCancelledTime(LocalDateTime.now());
        
        int result = pendingExpenseMapper.updateById(update);
        
        if (result > 0) {
            log.info("标记待支出项目为已取消成功 - id: {}", id);
        } else {
            log.error("标记待支出项目为已取消失败 - id: {}", id);
            throw new BusinessException("标记待支出项目为已取消失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsPending(Long id) {
        log.info("开始标记待支出项目为待支付 - id: {}", id);
        
        // 验证记录存在
        PendingExpense existing = pendingExpenseMapper.selectById(id);
        if (existing == null) {
            log.error("待支出项目不存在 - id: {}", id);
            throw new BusinessException("待支出项目不存在");
        }
        
        // 更新状态为 PENDING，清空完成时间和取消时间
        PendingExpense update = new PendingExpense();
        update.setId(id);
        update.setStatus(ExpenseStatusEnum.PENDING.getCode());
        update.setCompletedTime(null);
        update.setCancelledTime(null);
        
        int result = pendingExpenseMapper.updateById(update);
        
        if (result > 0) {
            log.info("标记待支出项目为待支付成功 - id: {}", id);
        } else {
            log.error("标记待支出项目为待支付失败 - id: {}", id);
            throw new BusinessException("标记待支出项目为待支付失败");
        }
    }
    
    // ================== 批量操作 ==================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResult batchMarkAsCompleted(List<Long> ids) {
        log.info("开始批量标记待支出项目为已完成 - ids: {}", ids);
        
        BatchOperationResult result = new BatchOperationResult();
        result.setTotalCount(ids.size());
        result.setFailureCount(0);
        
        LocalDateTime completedTime = LocalDateTime.now();
        
        for (Long id : ids) {
            try {
                // 验证记录存在
                PendingExpense existing = pendingExpenseMapper.selectById(id);
                if (existing == null) {
                    result.addFailure(id, null, "待支出项目不存在");
                    continue;
                }
                
                // 更新状态为 COMPLETED，记录完成时间
                PendingExpense update = new PendingExpense();
                update.setId(id);
                update.setStatus(ExpenseStatusEnum.COMPLETED.getCode());
                update.setCompletedTime(completedTime);
                
                int updateResult = pendingExpenseMapper.updateById(update);
                if (updateResult <= 0) {
                    result.addFailure(id, existing.getExpenseName(), "更新失败");
                }
            } catch (Exception e) {
                log.error("批量标记为已完成时发生异常 - id: {}, error: {}", id, e.getMessage());
                result.addFailure(id, null, e.getMessage());
            }
        }
        
        result.calculateSuccessCount();
        log.info("批量标记待支出项目为已完成完成 - 总数: {}, 成功: {}, 失败: {}", 
                 result.getTotalCount(), result.getSuccessCount(), result.getFailureCount());
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResult batchMarkAsCancelled(List<Long> ids) {
        log.info("开始批量标记待支出项目为已取消 - ids: {}", ids);
        
        BatchOperationResult result = new BatchOperationResult();
        result.setTotalCount(ids.size());
        result.setFailureCount(0);
        
        LocalDateTime cancelledTime = LocalDateTime.now();
        
        for (Long id : ids) {
            try {
                // 验证记录存在
                PendingExpense existing = pendingExpenseMapper.selectById(id);
                if (existing == null) {
                    result.addFailure(id, null, "待支出项目不存在");
                    continue;
                }
                
                // 更新状态为 CANCELLED，记录取消时间
                PendingExpense update = new PendingExpense();
                update.setId(id);
                update.setStatus(ExpenseStatusEnum.CANCELLED.getCode());
                update.setCancelledTime(cancelledTime);
                
                int updateResult = pendingExpenseMapper.updateById(update);
                if (updateResult <= 0) {
                    result.addFailure(id, existing.getExpenseName(), "更新失败");
                }
            } catch (Exception e) {
                log.error("批量标记为已取消时发生异常 - id: {}, error: {}", id, e.getMessage());
                result.addFailure(id, null, e.getMessage());
            }
        }
        
        result.calculateSuccessCount();
        log.info("批量标记待支出项目为已取消完成 - 总数: {}, 成功: {}, 失败: {}", 
                 result.getTotalCount(), result.getSuccessCount(), result.getFailureCount());
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResult batchDelete(List<Long> ids) {
        log.info("开始批量删除待支出项目 - ids: {}", ids);
        
        BatchOperationResult result = new BatchOperationResult();
        result.setTotalCount(ids.size());
        result.setFailureCount(0);
        
        for (Long id : ids) {
            try {
                // 验证记录存在
                PendingExpense existing = pendingExpenseMapper.selectById(id);
                if (existing == null) {
                    result.addFailure(id, null, "待支出项目不存在");
                    continue;
                }
                
                // 逻辑删除
                int deleteResult = pendingExpenseMapper.deleteById(id);
                if (deleteResult <= 0) {
                    result.addFailure(id, existing.getExpenseName(), "删除失败");
                }
            } catch (Exception e) {
                log.error("批量删除时发生异常 - id: {}, error: {}", id, e.getMessage());
                result.addFailure(id, null, e.getMessage());
            }
        }
        
        result.calculateSuccessCount();
        log.info("批量删除待支出项目完成 - 总数: {}, 成功: {}, 失败: {}", 
                 result.getTotalCount(), result.getSuccessCount(), result.getFailureCount());
        return result;
    }
    
    // ================== 查询与分页 ==================
    
    @Override
    public Page<PendingExpenseVO> page(PendingExpenseQueryDTO queryDTO) {
        log.info("开始分页查询待支出项目 - pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 创建分页对象
        Page<PendingExpense> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 构建动态查询条件
        QueryWrapper<PendingExpense> queryWrapper = new QueryWrapper<>();
        
        // 项目名称关键词搜索 (LIKE)
        if (queryDTO.getExpenseNameKeyword() != null && !queryDTO.getExpenseNameKeyword().trim().isEmpty()) {
            queryWrapper.like("expense_name", queryDTO.getExpenseNameKeyword().trim());
        }
        
        // 周期多选筛选
        if (queryDTO.getPeriods() != null && !queryDTO.getPeriods().isEmpty()) {
            queryWrapper.in("period", queryDTO.getPeriods());
        }
        
        // 计划类型多选筛选
        if (queryDTO.getPlanTypes() != null && !queryDTO.getPlanTypes().isEmpty()) {
            queryWrapper.in("plan_type", queryDTO.getPlanTypes());
        }
        
        // 状态多选筛选
        if (queryDTO.getStatuses() != null && !queryDTO.getStatuses().isEmpty()) {
            queryWrapper.in("status", queryDTO.getStatuses());
        }
        
        // 分类筛选
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq("category_id", queryDTO.getCategoryId());
        }
        
        // 支付日期范围筛选
        if (queryDTO.getPaymentDateStart() != null) {
            queryWrapper.ge("payment_date", queryDTO.getPaymentDateStart());
        }
        if (queryDTO.getPaymentDateEnd() != null) {
            queryWrapper.le("payment_date", queryDTO.getPaymentDateEnd());
        }
        
        // 处理排序
        String sortField = queryDTO.getSortField();
        String sortOrder = queryDTO.getSortOrder();
        
        if (sortField != null && !sortField.trim().isEmpty()) {
            // 转换驼峰命名为下划线命名
            String columnName = camelToUnderscore(sortField);
            
            if ("asc".equalsIgnoreCase(sortOrder)) {
                queryWrapper.orderByAsc(columnName);
            } else {
                queryWrapper.orderByDesc(columnName);
            }
        } else {
            // 默认按支付日期升序排序
            queryWrapper.orderByAsc("payment_date");
        }
        
        // 执行分页查询
        Page<PendingExpense> entityPage = pendingExpenseMapper.selectPage(page, queryWrapper);
        
        // Entity 列表转 VO 列表
        List<PendingExpenseVO> voList = new ArrayList<>();
        
        // 预加载分类和支付渠道名称（批量查询优化）
        Map<Long, String> categoryNameMap = new HashMap<>();
        Map<Long, String> paymentChannelNameMap = new HashMap<>();
        
        for (PendingExpense entity : entityPage.getRecords()) {
            PendingExpenseVO vo = converter.toVO(entity);
            
            // 填充分类名称
            if (entity.getCategoryId() != null) {
                if (!categoryNameMap.containsKey(entity.getCategoryId())) {
                    BillCategory category = billCategoryMapper.selectById(entity.getCategoryId());
                    if (category != null) {
                        categoryNameMap.put(entity.getCategoryId(), category.getCategoryName());
                    }
                }
                vo.setCategoryName(categoryNameMap.get(entity.getCategoryId()));
            }
            
            // 填充枚举中文名称
            PeriodEnum periodEnum = PeriodEnum.getByCode(entity.getPeriod());
            if (periodEnum != null) {
                vo.setPeriodName(periodEnum.getDesc());
            }
            
            PlanTypeEnum planTypeEnum = PlanTypeEnum.getByCode(entity.getPlanType());
            if (planTypeEnum != null) {
                vo.setPlanTypeName(planTypeEnum.getDesc());
            }
            
            ExpenseStatusEnum statusEnum = ExpenseStatusEnum.getByCode(entity.getStatus());
            if (statusEnum != null) {
                vo.setStatusName(statusEnum.getDesc());
            }
            
            voList.add(vo);
        }
        
        // 构建 VO 分页对象
        Page<PendingExpenseVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        
        log.info("分页查询待支出项目完成 - 总数: {}, 当前页: {}", entityPage.getTotal(), entityPage.getCurrent());
        return voPage;
    }
    
    /**
     * 驼峰命名转下划线命名
     *
     * @param camelCase 驼峰命名字符串
     * @return 下划线命名字符串
     */
    private String camelToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camelCase.charAt(0)));
        for (int i = 1; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    // ================== 统计分析 ==================
    
    @Override
    public PendingExpenseStatisticsVO getStatistics(PendingExpenseQueryDTO queryDTO) {
        log.info("开始获取待支出综合统计信息");
        
        PendingExpenseStatisticsVO statisticsVO = new PendingExpenseStatisticsVO();
        
        // 获取年份参数
        Integer year = queryDTO != null && queryDTO.getPaymentDateStart() != null 
                       ? queryDTO.getPaymentDateStart().getYear() 
                       : null;
        
        // 获取待支付总金额和数量
        BigDecimal totalAmount = getTotalPendingAmount(year);
        statisticsVO.setTotalAmount(totalAmount);
        
        // 获取待支付项目总数
        QueryWrapper<PendingExpense> countWrapper = new QueryWrapper<>();
        countWrapper.eq("status", ExpenseStatusEnum.PENDING.getCode());
        if (year != null) {
            countWrapper.apply("YEAR(payment_date) = {0}", year);
        }
        Long totalCount = pendingExpenseMapper.selectCount(countWrapper);
        statisticsVO.setTotalCount(totalCount.intValue());
        
        // 获取月度统计
        statisticsVO.setMonthlyStatistics(getMonthlyStatistics(year));
        
        // 获取分类统计
        statisticsVO.setCategoryStatistics(getCategoryStatistics(year));
        
        // 获取周期统计
        statisticsVO.setPeriodStatistics(getPeriodStatistics(year));
        
        // 获取计划类型统计
        statisticsVO.setPlanTypeStatistics(getPlanTypeStatistics(year));
        
        // 获取状态统计
        statisticsVO.setStatusStatistics(pendingExpenseMapper.getStatusStatistics());
        
        log.info("获取待支出综合统计信息完成 - year: {}", year);
        return statisticsVO;
    }
    
    @Override
    public BigDecimal getTotalPendingAmount(Integer year) {
        log.info("开始计算待支付总金额 - year: {}", year);
        
        // 查询所有 PENDING 状态的项目
        QueryWrapper<PendingExpense> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", ExpenseStatusEnum.PENDING.getCode());
        
        // 添加年份过滤
        if (year != null) {
            queryWrapper.apply("YEAR(payment_date) = {0}", year);
        }
        
        queryWrapper.select("IFNULL(SUM(amount), 0) as amount");
        
        List<Map<String, Object>> result = pendingExpenseMapper.selectMaps(queryWrapper);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (result != null && !result.isEmpty()) {
            Object amountObj = result.get(0).get("amount");
            if (amountObj != null) {
                totalAmount = new BigDecimal(amountObj.toString());
            }
        }
        
        log.info("计算待支付总金额完成 - year: {}, totalAmount: {}", year, totalAmount);
        return totalAmount;
    }
    
    @Override
    public BigDecimal getPendingAmountByQuery(PendingExpenseQueryDTO queryDTO) {
        log.info("开始根据查询条件计算待支付金额总和");
        
        // 构建查询条件（复用 buildQueryWrapper）
        QueryWrapper<PendingExpense> queryWrapper = buildQueryWrapper(queryDTO);
        
        // 如果查询条件中没有指定状态，默认只统计 PENDING 状态
        if (queryDTO == null || queryDTO.getStatuses() == null || queryDTO.getStatuses().isEmpty()) {
            queryWrapper.eq("status", ExpenseStatusEnum.PENDING.getCode());
        }
        
        // 选择 SUM 聚合
        queryWrapper.select("IFNULL(SUM(amount), 0) as amount");
        
        List<Map<String, Object>> result = pendingExpenseMapper.selectMaps(queryWrapper);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (result != null && !result.isEmpty()) {
            Object amountObj = result.get(0).get("amount");
            if (amountObj != null) {
                totalAmount = new BigDecimal(amountObj.toString());
            }
        }
        
        log.info("根据查询条件计算待支付金额总和完成 - totalAmount: {}", totalAmount);
        return totalAmount;
    }
    
    @Override
    public List<PendingExpenseStatisticsVO.MonthlyStatistics> getMonthlyStatistics(Integer year) {
        log.info("开始按月份统计待支出 - year: {}", year);
        
        List<PendingExpenseStatisticsVO.MonthlyStatistics> monthlyStatistics = 
                pendingExpenseMapper.getMonthlyStatistics(year);
        
        // 填充枚举值对应的中文名称（如果需要）
        // MonthlyStatistics 不需要填充枚举名称
        
        log.info("按月份统计待支出完成 - 统计记录数: {}", monthlyStatistics.size());
        return monthlyStatistics;
    }
    
    @Override
    public List<PendingExpenseStatisticsVO.CategoryStatistics> getCategoryStatistics(Integer year) {
        log.info("开始按分类统计待支出 - year: {}", year);
        
        List<PendingExpenseStatisticsVO.CategoryStatistics> categoryStatistics = 
                pendingExpenseMapper.getCategoryStatistics(year);
        
        log.info("按分类统计待支出完成 - year: {}, 统计记录数: {}", year, categoryStatistics.size());
        return categoryStatistics;
    }
    
    @Override
    public List<PendingExpenseStatisticsVO.PeriodStatistics> getPeriodStatistics(Integer year) {
        log.info("开始按周期统计待支出 - year: {}", year);
        
        List<PendingExpenseStatisticsVO.PeriodStatistics> periodStatistics = 
                pendingExpenseMapper.getPeriodStatistics(year);
        
        // 填充周期枚举的中文名称
        for (PendingExpenseStatisticsVO.PeriodStatistics stat : periodStatistics) {
            PeriodEnum periodEnum = PeriodEnum.getByCode(stat.getPeriod());
            if (periodEnum != null) {
                stat.setPeriodName(periodEnum.getDesc());
            }
        }
        
        log.info("按周期统计待支出完成 - year: {}, 统计记录数: {}", year, periodStatistics.size());
        return periodStatistics;
    }
    
    @Override
    public List<PendingExpenseStatisticsVO.PlanTypeStatistics> getPlanTypeStatistics(Integer year) {
        log.info("开始按计划类型统计待支出 - year: {}", year);
        
        List<PendingExpenseStatisticsVO.PlanTypeStatistics> planTypeStatistics = 
                pendingExpenseMapper.getPlanTypeStatistics(year);
        
        // 计算总金额用于计算百分比
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PendingExpenseStatisticsVO.PlanTypeStatistics stat : planTypeStatistics) {
            if (stat.getAmount() != null) {
                totalAmount = totalAmount.add(stat.getAmount());
            }
        }
        
        // 填充计划类型枚举的中文名称和计算百分比
        for (PendingExpenseStatisticsVO.PlanTypeStatistics stat : planTypeStatistics) {
            PlanTypeEnum planTypeEnum = PlanTypeEnum.getByCode(stat.getPlanType());
            if (planTypeEnum != null) {
                stat.setPlanTypeName(planTypeEnum.getDesc());
            }
            
            // 计算百分比
            if (stat.getAmount() != null && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = stat.getAmount()
                        .divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                stat.setPercentage(percentage);
            } else {
                stat.setPercentage(BigDecimal.ZERO);
            }
        }
        
        log.info("按计划类型统计待支出完成 - year: {}, 统计记录数: {}", year, planTypeStatistics.size());
        return planTypeStatistics;
    }
    
    // ================== 导入导出 ==================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> importFromExcel(MultipartFile file) {
        log.info("开始从 Excel 导入待支出项目 - 文件名: {}", file.getOriginalFilename());
        
        List<String> errorMessages = new ArrayList<>();
        
        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || 
            (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            errorMessages.add("文件格式不正确，仅支持 .xlsx 和 .xls 格式");
            return errorMessages;
        }
        
        try {
            // 使用 EasyExcel 解析文件
            List<PendingExpenseImportDTO> importList = EasyExcel.read(file.getInputStream())
                    .head(PendingExpenseImportDTO.class)
                    .sheet()
                    .doReadSync();
            
            if (importList == null || importList.isEmpty()) {
                errorMessages.add("Excel 文件为空或没有数据行");
                return errorMessages;
            }
            
            log.info("Excel 解析完成 - 共 {} 行数据", importList.size());
            
            // 构建分类名称到ID的映射
            Map<String, Long> categoryNameToIdMap = new HashMap<>();
            QueryWrapper<BillCategory> categoryQueryWrapper = new QueryWrapper<>();
            categoryQueryWrapper.select("id", "category_name");
            List<BillCategory> allCategories = billCategoryMapper.selectList(categoryQueryWrapper);
            for (BillCategory category : allCategories) {
                categoryNameToIdMap.put(category.getCategoryName(), category.getId());
            }
            
            // 构建支付渠道名称到ID的映射
            Map<String, Long> channelNameToIdMap = new HashMap<>();
            QueryWrapper<BillPaymentChannel> channelQueryWrapper = new QueryWrapper<>();
            channelQueryWrapper.select("id", "channel_name");
            List<BillPaymentChannel> allChannels = billPaymentChannelMapper.selectList(channelQueryWrapper);
            for (BillPaymentChannel channel : allChannels) {
                channelNameToIdMap.put(channel.getChannelName(), channel.getId());
            }
            
            // 逐行验证并转换数据
            List<PendingExpense> validExpenses = new ArrayList<>();
            
            for (int i = 0; i < importList.size(); i++) {
                int rowNum = i + 2; // Excel 行号从 1 开始，且有标题行
                PendingExpenseImportDTO importDTO = importList.get(i);
                StringBuilder rowErrors = new StringBuilder();
                
                // 验证必填字段
                if (importDTO.getExpenseName() == null || importDTO.getExpenseName().trim().isEmpty()) {
                    rowErrors.append("项目名称不能为空; ");
                }
                if (importDTO.getAmount() == null) {
                    rowErrors.append("金额不能为空; ");
                } else if (importDTO.getAmount().compareTo(new BigDecimal("0.01")) < 0 || 
                           importDTO.getAmount().compareTo(new BigDecimal("999999.99")) > 0) {
                    rowErrors.append("金额必须在 0.01 到 999999.99 之间; ");
                }
                if (importDTO.getPaymentDate() == null) {
                    rowErrors.append("支付日期不能为空; ");
                } else if (importDTO.getPaymentDate().isBefore(LocalDate.of(1900, 1, 1))) {
                    rowErrors.append("支付日期不能早于 1900-01-01; ");
                }
                if (importDTO.getPeriod() == null || importDTO.getPeriod().trim().isEmpty()) {
                    rowErrors.append("周期不能为空; ");
                } else if (!importDTO.getPeriod().matches("MONTHLY|YEARLY|ONETIME")) {
                    rowErrors.append("周期必须是 MONTHLY、YEARLY 或 ONETIME; ");
                }
                if (importDTO.getPlanType() == null || importDTO.getPlanType().trim().isEmpty()) {
                    rowErrors.append("计划类型不能为空; ");
                } else if (!importDTO.getPlanType().matches("RIGID|INTENDED")) {
                    rowErrors.append("计划类型必须是 RIGID 或 INTENDED; ");
                }
                
                // 验证状态枚举值（如果提供了）
                if (importDTO.getStatus() != null && !importDTO.getStatus().trim().isEmpty()) {
                    if (!importDTO.getStatus().matches("PENDING|COMPLETED|CANCELLED")) {
                        rowErrors.append("状态必须是 PENDING、COMPLETED 或 CANCELLED; ");
                    }
                }
                
                // 查询分类ID
                Long categoryId = null;
                if (importDTO.getCategoryName() != null && !importDTO.getCategoryName().trim().isEmpty()) {
                    categoryId = categoryNameToIdMap.get(importDTO.getCategoryName().trim());
                    if (categoryId == null) {
                        rowErrors.append("分类名称不存在: ").append(importDTO.getCategoryName()).append("; ");
                    }
                }
                
                // 如果有错误，记录错误信息并跳过该行
                if (rowErrors.length() > 0) {
                    errorMessages.add("第 " + rowNum + " 行: " + rowErrors.toString());
                    continue;
                }
                
                // 转换为 Entity
                PendingExpense expense = new PendingExpense();
                expense.setExpenseName(importDTO.getExpenseName().trim());
                expense.setAmount(importDTO.getAmount());
                expense.setPaymentDate(importDTO.getPaymentDate());
                expense.setPeriod(importDTO.getPeriod().trim());
                expense.setPlanType(importDTO.getPlanType().trim());
                expense.setStatus(importDTO.getStatus() != null && !importDTO.getStatus().trim().isEmpty() 
                                  ? importDTO.getStatus().trim() 
                                  : ExpenseStatusEnum.PENDING.getCode());
                expense.setCategoryId(categoryId);
                expense.setRemark(importDTO.getRemark());
                
                validExpenses.add(expense);
            }
            
            // 批量保存有效数据
            if (!validExpenses.isEmpty()) {
                for (PendingExpense expense : validExpenses) {
                    pendingExpenseMapper.insert(expense);
                }
                log.info("成功导入 {} 条待支出项目", validExpenses.size());
            }
            
            if (errorMessages.isEmpty()) {
                log.info("Excel 导入完成 - 全部成功");
            } else {
                log.warn("Excel 导入完成 - 成功: {}, 失败: {}", validExpenses.size(), errorMessages.size());
            }
            
        } catch (IOException e) {
            log.error("读取 Excel 文件时发生异常", e);
            errorMessages.add("读取 Excel 文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("导入 Excel 时发生异常", e);
            errorMessages.add("导入失败: " + e.getMessage());
        }
        
        return errorMessages;
    }
    
    @Override
    public void exportToExcel(HttpServletResponse response, PendingExpenseQueryDTO queryDTO) {
        log.info("开始导出待支出项目为 Excel");
        
        try {
            // 根据查询条件获取数据（不分页，获取全部数据）
            QueryWrapper<PendingExpense> queryWrapper = buildQueryWrapper(queryDTO);
            List<PendingExpense> expenseList = pendingExpenseMapper.selectList(queryWrapper);
            
            // 转换为导出 DTO
            List<PendingExpenseImportDTO> exportList = new ArrayList<>();
            
            for (PendingExpense expense : expenseList) {
                PendingExpenseImportDTO exportDTO = new PendingExpenseImportDTO();
                exportDTO.setExpenseName(expense.getExpenseName());
                exportDTO.setAmount(expense.getAmount());
                exportDTO.setPaymentDate(expense.getPaymentDate());
                exportDTO.setPeriod(expense.getPeriod());
                exportDTO.setPlanType(expense.getPlanType());
                exportDTO.setStatus(expense.getStatus());
                
                // 填充分类名称
                if (expense.getCategoryId() != null) {
                    BillCategory category = billCategoryMapper.selectById(expense.getCategoryId());
                    if (category != null) {
                        exportDTO.setCategoryName(category.getCategoryName());
                    }
                }
                
                exportDTO.setRemark(expense.getRemark());
                
                exportList.add(exportDTO);
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            
            // 生成文件名（包含当前日期）
            String fileName = "待支出项目_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            
            // 使用 EasyExcel 写入响应流
            EasyExcel.write(response.getOutputStream(), PendingExpenseImportDTO.class)
                    .sheet("待支出项目")
                    .doWrite(exportList);
            
            log.info("导出待支出项目为 Excel 完成 - 共导出 {} 条记录", exportList.size());
            
        } catch (IOException e) {
            log.error("导出 Excel 时发生 IO 异常", e);
            throw new BusinessException("导出 Excel 失败");
        } catch (Exception e) {
            log.error("导出 Excel 时发生异常", e);
            throw new BusinessException("导出 Excel 失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建查询条件（用于导出功能）
     *
     * @param queryDTO 查询条件
     * @return QueryWrapper
     */
    private QueryWrapper<PendingExpense> buildQueryWrapper(PendingExpenseQueryDTO queryDTO) {
        QueryWrapper<PendingExpense> queryWrapper = new QueryWrapper<>();
        
        if (queryDTO == null) {
            return queryWrapper;
        }
        
        // 项目名称关键词搜索
        if (queryDTO.getExpenseNameKeyword() != null && !queryDTO.getExpenseNameKeyword().trim().isEmpty()) {
            queryWrapper.like("expense_name", queryDTO.getExpenseNameKeyword().trim());
        }
        
        // 周期多选筛选
        if (queryDTO.getPeriods() != null && !queryDTO.getPeriods().isEmpty()) {
            queryWrapper.in("period", queryDTO.getPeriods());
        }
        
        // 计划类型多选筛选
        if (queryDTO.getPlanTypes() != null && !queryDTO.getPlanTypes().isEmpty()) {
            queryWrapper.in("plan_type", queryDTO.getPlanTypes());
        }
        
        // 状态多选筛选
        if (queryDTO.getStatuses() != null && !queryDTO.getStatuses().isEmpty()) {
            queryWrapper.in("status", queryDTO.getStatuses());
        }
        
        // 分类筛选
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq("category_id", queryDTO.getCategoryId());
        }
        
        // 支付日期范围筛选
        if (queryDTO.getPaymentDateStart() != null) {
            queryWrapper.ge("payment_date", queryDTO.getPaymentDateStart());
        }
        if (queryDTO.getPaymentDateEnd() != null) {
            queryWrapper.le("payment_date", queryDTO.getPaymentDateEnd());
        }
        
        // 默认按支付日期升序排序
        queryWrapper.orderByAsc("payment_date");
        
        return queryWrapper;
    }
}
