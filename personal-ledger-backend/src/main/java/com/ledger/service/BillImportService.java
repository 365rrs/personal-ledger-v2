package com.ledger.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ledger.dto.BillConvertDTO;
import com.ledger.dto.BillImportDetailQueryDTO;
import com.ledger.dto.BillImportRecordQueryDTO;
import com.ledger.dto.BillSkipDTO;
import com.ledger.vo.BillImportDetailVO;
import com.ledger.vo.BillImportRecordVO;
import com.ledger.vo.BillImportStatisticsVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 账单导入服务接口
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
public interface BillImportService {
    
    /**
     * 上传文件并导入
     */
    Long uploadAndImport(MultipartFile file);
    
    /**
     * 查询导入记录列表
     */
    IPage<BillImportRecordVO> pageRecords(BillImportRecordQueryDTO dto);
    
    /**
     * 查询导入记录详情
     */
    BillImportRecordVO getRecordById(Long id);
    
    /**
     * 查询导入明细列表
     */
    IPage<BillImportDetailVO> pageDetails(BillImportDetailQueryDTO dto);
    
    /**
     * 查询导入统计
     */
    BillImportStatisticsVO getStatistics(Long importRecordId);
    
    /**
     * 转换为账单
     */
    void convertToBill(BillConvertDTO dto);
    
    /**
     * 跳过记录
     */
    void skipRecords(BillSkipDTO dto);
    
    /**
     * 删除导入记录
     */
    void deleteRecord(Long id);
}
