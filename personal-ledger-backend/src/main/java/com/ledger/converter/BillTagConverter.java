package com.ledger.converter;

import com.ledger.dto.BillTagDTO;
import com.ledger.entity.BillTag;
import com.ledger.vo.BillTagVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 账单标签转换器
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Component
public class BillTagConverter {
    
    /**
     * DTO 转 Entity
     */
    public BillTag toEntity(BillTagDTO dto) {
        BillTag tag = new BillTag();
        BeanUtils.copyProperties(dto, tag);
        return tag;
    }
    
    /**
     * Entity 转 VO
     */
    public BillTagVO toVO(BillTag entity) {
        BillTagVO vo = new BillTagVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
