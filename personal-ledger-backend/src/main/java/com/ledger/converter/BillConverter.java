package com.ledger.converter;

import com.ledger.dto.BillDTO;
import com.ledger.entity.Bill;
import com.ledger.vo.BillVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 账单转换器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Component
public class BillConverter {
    
    public Bill toEntity(BillDTO dto) {
        Bill bill = new Bill();
        BeanUtils.copyProperties(dto, bill);
        return bill;
    }
    
    public BillVO toVO(Bill bill) {
        BillVO vo = new BillVO();
        BeanUtils.copyProperties(bill, vo);
        return vo;
    }
}
