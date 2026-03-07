package com.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ledger.dto.BillCategoryDTO;
import com.ledger.entity.Bill;
import com.ledger.entity.BillCategory;
import com.ledger.exception.BusinessException;
import com.ledger.mapper.BillCategoryMapper;
import com.ledger.mapper.BillMapper;
import com.ledger.service.BillCategoryService;
import com.ledger.vo.BillCategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单分类服务实现类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@Service
public class BillCategoryServiceImpl implements BillCategoryService {

    @Resource
    private BillCategoryMapper billCategoryMapper;

    @Resource
    private BillMapper billMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillCategoryVO create(BillCategoryDTO dto) {
        // 检查分类名称是否重复
        checkDuplicateName(dto.getCategoryName(), dto.getCategoryType(), null);

        // 检查层级（最多两级）
        if (dto.getParentId() != null) {
            BillCategory parent = billCategoryMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            if (parent.getParentId() != null) {
                throw new BusinessException("不支持创建三级分类");
            }
        }

        BillCategory category = new BillCategory();
        BeanUtils.copyProperties(dto, category);
        category.setEnabled("1"); // 默认启用

        billCategoryMapper.insert(category);

        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillCategoryVO update(BillCategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("分类 ID 不能为空");
        }

        BillCategory exists = billCategoryMapper.selectById(dto.getId());
        if (exists == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查分类名称是否重复
        checkDuplicateName(dto.getCategoryName(), dto.getCategoryType(), dto.getId());

        // 不允许修改分类类型和父分类
        if (!exists.getCategoryType().equals(dto.getCategoryType())) {
            throw new BusinessException("不允许修改分类类型");
        }
        // 检查父分类是否被修改（注意处理 null 值）
        Long existingParentId = exists.getParentId();
        Long dtoParentId = dto.getParentId();
        if ((existingParentId == null && dtoParentId != null) ||
            (existingParentId != null && !existingParentId.equals(dtoParentId))) {
            throw new BusinessException("不允许修改父分类");
        }

        String oldCategoryName = exists.getCategoryName();
        exists.setCategoryName(dto.getCategoryName());
        exists.setIcon(dto.getIcon());
        exists.setSortOrder(dto.getSortOrder());

        billCategoryMapper.updateById(exists);

        // 如果分类名称发生变化，同步更新账单表中的分类名称
        if (!oldCategoryName.equals(dto.getCategoryName())) {
            syncUpdateBillCategoryName(exists.getId(), dto.getCategoryName(), exists.getParentId());
        }

        return convertToVO(exists);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BillCategory category = billCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        LambdaQueryWrapper<BillCategory> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(BillCategory::getParentId, id);
        Long childCount = billCategoryMapper.selectCount(childQuery);
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，请先删除子分类");
        }

        // TODO: 检查是否有账单使用该分类
        // 这里需要查询 bill 表的 category 和 sub_category 字段

        // 逻辑删除
        billCategoryMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, String enabled) {
        BillCategory category = billCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        category.setEnabled(enabled);
        billCategoryMapper.updateById(category);

        // 如果是禁用父分类，子分类也自动禁用
        if ("0".equals(enabled)) {
            LambdaQueryWrapper<BillCategory> childQuery = new LambdaQueryWrapper<>();
            childQuery.eq(BillCategory::getParentId, id);
            List<BillCategory> children = billCategoryMapper.selectList(childQuery);
            for (BillCategory child : children) {
                child.setEnabled("0");
                billCategoryMapper.updateById(child);
            }
        } else {
            // 如果是启用子分类，父分类必须是启用状态
            if (category.getParentId() != null) {
                BillCategory parent = billCategoryMapper.selectById(category.getParentId());
                if (parent != null && "0".equals(parent.getEnabled())) {
                    throw new BusinessException("请先启用父分类");
                }
            }
        }
    }

    @Override
    public List<BillCategoryVO> list(String categoryType, String enabled) {
        LambdaQueryWrapper<BillCategory> query = new LambdaQueryWrapper<>();
        
        if (categoryType != null && !categoryType.isEmpty()) {
            query.eq(BillCategory::getCategoryType, categoryType);
        }
        
        if (enabled != null && !enabled.isEmpty()) {
            query.eq(BillCategory::getEnabled, enabled);
        }
        
        query.orderByAsc(BillCategory::getSortOrder);
        
        List<BillCategory> categories = billCategoryMapper.selectList(query);
        
        // 构建树形结构
        return buildTree(categories);
    }

    @Override
    public List<BillCategoryVO> listAll(String categoryType, String enabled) {
        LambdaQueryWrapper<BillCategory> query = new LambdaQueryWrapper<>();
        
        if (categoryType != null && !categoryType.isEmpty()) {
            query.eq(BillCategory::getCategoryType, categoryType);
        }
        
        if (enabled != null && !enabled.isEmpty()) {
            query.eq(BillCategory::getEnabled, enabled);
        }
        
        query.orderByAsc(BillCategory::getSortOrder, BillCategory::getId);
        
        List<BillCategory> categories = billCategoryMapper.selectList(query);
        
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public BillCategoryVO getById(Long id) {
        BillCategory category = billCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(Long id, Integer newSortOrder) {
        BillCategory category = billCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        category.setSortOrder(newSortOrder);
        billCategoryMapper.updateById(category);
    }

    /**
     * 检查分类名称是否重复
     */
    private void checkDuplicateName(String categoryName, String categoryType, Long excludeId) {
        LambdaQueryWrapper<BillCategory> query = new LambdaQueryWrapper<>();
        query.eq(BillCategory::getCategoryName, categoryName)
             .eq(BillCategory::getCategoryType, categoryType);
        
        if (excludeId != null) {
            query.ne(BillCategory::getId, excludeId);
        }
        
        Long count = billCategoryMapper.selectCount(query);
        if (count > 0) {
            throw new BusinessException("该分类名称已存在");
        }
    }

    /**
     * 同步更新账单表中的分类名称
     * @param categoryId 分类 ID
     * @param newCategoryName 新的分类名称
     * @param parentId 父分类 ID（null 表示一级分类）
     */
    private void syncUpdateBillCategoryName(Long categoryId, String newCategoryName, Long parentId) {
        try {
            // 如果是一级分类，更新 category 字段
            if (parentId == null) {
                // 使用 LambdaUpdateWrapper 进行批量更新（Lambda 方式，避免魔法字符串）
                LambdaUpdateWrapper<Bill> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Bill::getCategoryId, categoryId)
                            .set(Bill::getCategory, newCategoryName);
                
                int affectedRows = billMapper.update(null, updateWrapper);
                
                log.info("同步更新账单表分类名称 - categoryId: {}, newCategoryName: {}, affected rows: {}", 
                        categoryId, newCategoryName, affectedRows);
            } else {
                // 如果是二级分类，更新 sub_category 字段
                LambdaUpdateWrapper<Bill> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Bill::getSubCategoryId, categoryId)
                            .set(Bill::getSubCategory, newCategoryName);
                
                int affectedRows = billMapper.update(null, updateWrapper);
                
                log.info("同步更新账单表二级分类名称 - categoryId: {}, newCategoryName: {}, affected rows: {}", 
                        categoryId, newCategoryName, affectedRows);
            }
        } catch (Exception e) {
            log.error("同步更新账单表分类名称失败 - categoryId: {}, newCategoryName: {}", 
                    categoryId, newCategoryName, e);
            throw new BusinessException("同步更新账单表分类名称失败：" + e.getMessage());
        }
    }

    /**
     * 构建树形结构
     */
    private List<BillCategoryVO> buildTree(List<BillCategory> categories) {
        List<BillCategoryVO> tree = new ArrayList<>();
        List<BillCategoryVO> allNodes = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 先添加所有一级分类（parentId 为 null）
        for (BillCategoryVO node : allNodes) {
            if (node.getParentId() == null) {
                tree.add(node);
            }
        }

        // 为每个一级分类添加子分类
        for (BillCategoryVO parentNode : tree) {
            List<BillCategoryVO> children = new ArrayList<>();
            for (BillCategoryVO node : allNodes) {
                if (parentNode.getId().equals(node.getParentId())) {
                    children.add(node);
                }
            }
            if (!children.isEmpty()) {
                parentNode.setChildren(children);
            }
        }

        return tree;
    }

    /**
     * Entity 转 VO
     */
    private BillCategoryVO convertToVO(BillCategory category) {
        BillCategoryVO vo = new BillCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
