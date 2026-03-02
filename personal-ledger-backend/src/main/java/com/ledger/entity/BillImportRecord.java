package com.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ledger.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 导入记录实体类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bill_import_record")
public class BillImportRecord extends BaseEntity {
    
    /**
     * 导入文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 总记录数
     */
    private Integer totalCount;
    
    /**
     * 已处理记录数
     */
    private Integer processedCount;
    
    /**
     * 成功导入数
     */
    private Integer successCount;
    
    /**
     * 失败记录数
     */
    private Integer failCount;
    
    /**
     * 导入状态：PROCESSING-处理中，SUCCESS-成功，FAILED-失败，PARTIAL-部分成功
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 文件存储路径
     */
    private String filePath;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
