package com.ledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.dto.BillTagDTO;
import com.ledger.dto.BillTagQueryDTO;
import com.ledger.vo.BillTagVO;

/**
 * 账单标签 Service
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
public interface BillTagService {
    
    /**
     * 分页查询标签
     */
    IPage<BillTagVO> page(BillTagQueryDTO dto);
    
    /**
     * 根据 ID 查询标签
     */
    BillTagVO getById(Long id);
    
    /**
     * 创建标签
     */
    Long create(BillTagDTO dto);
    
    /**
     * 更新标签
     */
    void update(BillTagDTO dto);
    
    /**
     * 删除标签
     */
    void delete(Long id);
    
    /**
     * 启用/停用标签
     */
    void updateStatus(Long id, String status);
    
    /**
     * 更新标签排序序号
     */
    void updateSortOrder(Long id, Integer newSortOrder);
    
    /**
     * 查询所有标签列表
     */
    java.util.List<BillTagVO> list();
}
