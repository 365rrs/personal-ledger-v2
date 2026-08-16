import request from '@/utils/request'

// ==================== CRUD 操作 ====================

// 创建待支出项目
export function createPendingExpense(data) {
  return request({
    url: '/pending-expense',
    method: 'post',
    data
  })
}

// 更新待支出项目
export function updatePendingExpense(id, data) {
  return request({
    url: `/pending-expense/${id}`,
    method: 'put',
    data
  })
}

// 删除待支出项目
export function deletePendingExpense(id) {
  return request({
    url: `/pending-expense/${id}`,
    method: 'delete'
  })
}

// 获取待支出项目详情
export function getPendingExpense(id) {
  return request({
    url: `/pending-expense/${id}`,
    method: 'get'
  })
}

// ==================== 周期性支出批量创建 ====================

// 批量创建周期性支出
export function createRecurringExpense(data) {
  return request({
    url: '/pending-expense/recurring',
    method: 'post',
    data
  })
}

// ==================== 状态管理 ====================

// 标记为已完成
export function markAsCompleted(id) {
  return request({
    url: `/pending-expense/${id}/complete`,
    method: 'put'
  })
}

// 标记为已取消
export function markAsCancelled(id) {
  return request({
    url: `/pending-expense/${id}/cancel`,
    method: 'put'
  })
}

// 标记为待支付
export function markAsPending(id) {
  return request({
    url: `/pending-expense/${id}/pending`,
    method: 'put'
  })
}

// ==================== 批量操作 ====================

// 批量标记为已完成
export function batchMarkAsCompleted(ids) {
  return request({
    url: '/pending-expense/batch/complete',
    method: 'post',
    data: ids
  })
}

// 批量标记为已取消
export function batchMarkAsCancelled(ids) {
  return request({
    url: '/pending-expense/batch/cancel',
    method: 'post',
    data: ids
  })
}

// 批量删除
export function batchDelete(ids) {
  return request({
    url: '/pending-expense/batch/delete',
    method: 'post',
    data: ids
  })
}

// ==================== 查询分页 ====================

// 分页查询待支出项目
export function pagePendingExpenses(data) {
  return request({
    url: '/pending-expense/page',
    method: 'post',
    data
  })
}

// ==================== 统计分析 ====================

// 获取综合统计信息
export function getStatistics(data) {
  return request({
    url: '/pending-expense/statistics',
    method: 'post',
    data
  })
}

// 获取待支付总金额
export function getTotalPendingAmount(year) {
  return request({
    url: '/pending-expense/statistics/total-pending',
    method: 'get',
    params: { year }
  })
}

// 根据查询条件计算待支付金额总和
export function getPendingAmountByQuery(data) {
  return request({
    url: '/pending-expense/statistics/pending-amount',
    method: 'post',
    data
  })
}

// 按月份统计
export function getMonthlyStatistics(year) {
  return request({
    url: '/pending-expense/statistics/monthly',
    method: 'get',
    params: { year }
  })
}

// 按分类统计
export function getCategoryStatistics(year) {
  return request({
    url: '/pending-expense/statistics/category',
    method: 'get',
    params: { year }
  })
}

// 按周期统计
export function getPeriodStatistics(year) {
  return request({
    url: '/pending-expense/statistics/period',
    method: 'get',
    params: { year }
  })
}

// 按计划类型统计
export function getPlanTypeStatistics(year) {
  return request({
    url: '/pending-expense/statistics/plan-type',
    method: 'get',
    params: { year }
  })
}

// ==================== 导入导出 ====================

// 从 Excel 导入
export function importFromExcel(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/pending-expense/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 导出为 Excel
export function exportToExcel(queryParams) {
  return request({
    url: '/pending-expense/export',
    method: 'post',
    data: queryParams,
    responseType: 'blob'
  })
}
