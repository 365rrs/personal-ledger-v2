package com.ledger.converter;

import com.ledger.dto.BillDTO;
import com.ledger.entity.Bill;
import com.ledger.entity.BillPaymentChannel;
import com.ledger.mapper.BillPaymentChannelMapper;
import com.ledger.vo.BillVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 账单转换器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Component
public class BillConverter {
    
    @Resource
    private BillPaymentChannelMapper paymentChannelMapper;
    
    public Bill toEntity(BillDTO dto) {
        Bill bill = new Bill();
        BeanUtils.copyProperties(dto, bill);
        return bill;
    }
    
    public BillVO toVO(Bill bill) {
        BillVO vo = new BillVO();
        BeanUtils.copyProperties(bill, vo);
        
        // 填充支付渠道的类型和图标
        if (bill.getPaymentChannel() != null && !bill.getPaymentChannel().isEmpty()) {
            BillPaymentChannel channel = paymentChannelMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BillPaymentChannel>()
                    .eq(BillPaymentChannel::getChannelName, bill.getPaymentChannel())
            );
            if (channel != null) {
                vo.setPaymentChannelType(channel.getChannelType());
                vo.setPaymentChannelIcon(channel.getIcon());
            }
        }
        
        return vo;
    }
}
