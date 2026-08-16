package com.ledger.converter;

import com.ledger.dto.PendingExpenseDTO;
import com.ledger.dto.PendingExpenseImportDTO;
import com.ledger.entity.PendingExpense;
import com.ledger.vo.PendingExpenseVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 待支出转换器
 *
 * @author personal-ledger
 * @date 2025-01-14
 */
@Mapper(componentModel = "spring")
public interface PendingExpenseConverter {
    
    /**
     * DTO 转 Entity
     *
     * @param dto 待支出DTO
     * @return 待支出实体
     */
    PendingExpense toEntity(PendingExpenseDTO dto);
    
    /**
     * Entity 转 VO
     *
     * @param entity 待支出实体
     * @return 待支出VO
     */
    PendingExpenseVO toVO(PendingExpense entity);
    
    /**
     * Entity 列表转 VO 列表
     *
     * @param entities 待支出实体列表
     * @return 待支出VO列表
     */
    List<PendingExpenseVO> toVOList(List<PendingExpense> entities);
    
    /**
     * 导入 DTO 转 Entity
     *
     * @param importDTO 导入DTO
     * @return 待支出实体
     */
    PendingExpense fromImportDTO(PendingExpenseImportDTO importDTO);
}
