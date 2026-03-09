import request from '@/utils/request'

// 创建账单
export function createBill(data) {
  return request({
    url: '/bill',
    method: 'post',
    data
  })
}

// 更新账单
export function updateBill(data) {
  return request({
    url: '/bill/update',
    method: 'post',
    data
  })
}

// 删除账单
export function deleteBill(id) {
  return request({
    url: '/bill/delete',
    method: 'post',
    params: { id }
  })
}

// 查询账单详情
export function getBill(id) {
  return request({
    url: '/bill',
    method: 'get',
    params: { id }
  })
}

// 分页查询账单
export function pageBills(data) {
  return request({
    url: '/bill/page',
    method: 'post',
    data
  })
}

// 查询统计数据
export function getStatistics(data) {
  return request({
    url: '/bill/statistics',
    method: 'post',
    data
  })
}

// 批量更新账单
export function batchUpdateBills(data) {
  return request({
    url: '/bill/batchUpdate',
    method: 'post',
    data
  })
}

// 查询每日支出
export function getDailyExpense(data) {
  return request({
    url: '/bill/dailyExpense',
    method: 'post',
    data
  })
}

// 查询累计支出
export function getCumulativeExpense(data) {
  return request({
    url: '/bill/cumulativeExpense',
    method: 'post',
    data
  })
}

// 按分类统计
export function getCategoryStatistics(data) {
  return request({
    url: '/bill/categoryStatistics',
    method: 'post',
    data
  })
}
