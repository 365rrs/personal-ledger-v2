package com.ledger.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作结果VO
 *
 * @author personal-ledger
 * @date 2025-01-14
 */
@Data
public class BatchOperationResult {
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failureCount;
    
    /**
     * 失败详情列表
     */
    private List<FailureDetail> failureDetails = new ArrayList<>();
    
    /**
     * 失败详情内部类
     */
    @Data
    public static class FailureDetail {
        
        /**
         * 项目ID
         */
        private Long id;
        
        /**
         * 项目名称
         */
        private String expenseName;
        
        /**
         * 失败原因
         */
        private String errorMessage;
        
        /**
         * 构造函数
         */
        public FailureDetail() {
        }
        
        /**
         * 带参数的构造函数
         *
         * @param id 项目ID
         * @param expenseName 项目名称
         * @param errorMessage 失败原因
         */
        public FailureDetail(Long id, String expenseName, String errorMessage) {
            this.id = id;
            this.expenseName = expenseName;
            this.errorMessage = errorMessage;
        }
    }
    
    /**
     * 添加失败详情
     *
     * @param id 项目ID
     * @param expenseName 项目名称
     * @param errorMessage 失败原因
     */
    public void addFailure(Long id, String expenseName, String errorMessage) {
        this.failureDetails.add(new FailureDetail(id, expenseName, errorMessage));
        this.failureCount = this.failureDetails.size();
    }
    
    /**
     * 计算成功数量
     */
    public void calculateSuccessCount() {
        if (this.totalCount != null && this.failureCount != null) {
            this.successCount = this.totalCount - this.failureCount;
        }
    }
}
