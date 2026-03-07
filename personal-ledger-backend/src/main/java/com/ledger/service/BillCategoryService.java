package com.ledger.service;

import com.ledger.dto.BillCategoryDTO;
import com.ledger.vo.BillCategoryVO;

import java.util.List;

/**
 * 账单分类服务接口
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
public interface BillCategoryService {

    /**
     * 创建分类
     *
     * @param dto 分类 DTO
     * @return 创建的分类 VO
     */
    BillCategoryVO create(BillCategoryDTO dto);

    /**
     * 更新分类
     *
     * @param dto 分类 DTO
     * @return 更新的分类 VO
     */
    BillCategoryVO update(BillCategoryDTO dto);

    /**
     * 删除分类
     *
     * @param id 分类 ID
     */
    void delete(Long id);

    /**
     * 启用/禁用分类
     *
     * @param id      分类 ID
     * @param enabled 是否启用
     */
    void toggleStatus(Long id, String enabled);

    /**
     * 查询分类列表（树形结构）
     *
     * @param categoryType 分类类型
     * @param enabled      是否启用
     * @return 分类树
     */
    List<BillCategoryVO> list(String categoryType, String enabled);

    /**
     * 查询所有分类（不分层）
     *
     * @param categoryType 分类类型
     * @param enabled      是否启用
     * @return 分类列表
     */
    List<BillCategoryVO> listAll(String categoryType, String enabled);

    /**
     * 根据 ID 查询分类
     *
     * @param id 分类 ID
     * @return 分类 VO
     */
    BillCategoryVO getById(Long id);

    /**
     * 更新分类排序
     *
     * @param id         分类 ID
     * @param sortOrder  新的排序序号
     */
    void updateSortOrder(Long id, Integer sortOrder);
}
