package com.ledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.dto.BillPaymentChannelDTO;
import com.ledger.vo.BillPaymentChannelVO;

import java.util.List;

/**
 * 支付渠道 Service
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
public interface BillPaymentChannelService {
    
    /**
     * 创建支付渠道
     */
    BillPaymentChannelVO create(BillPaymentChannelDTO dto);
    
    /**
     * 更新支付渠道
     */
    BillPaymentChannelVO update(BillPaymentChannelDTO dto);
    
    /**
     * 删除支付渠道
     */
    void delete(Long id);
    
    /**
     * 获取支付渠道详情
     */
    BillPaymentChannelVO getById(Long id);
    
    /**
     * 分页查询支付渠道
     */
    IPage<BillPaymentChannelVO> page(BillPaymentChannelDTO dto);
    
    /**
     * 查询支付渠道列表（不分页）
     */
    List<BillPaymentChannelVO> list(BillPaymentChannelDTO dto);
    
    /**
     * 启用/禁用支付渠道
     */
    void toggleStatus(Long id, String enabled);
    
    /**
     * 更新排序
     */
    void updateSortOrder(Long id, Integer newSortOrder);
}
