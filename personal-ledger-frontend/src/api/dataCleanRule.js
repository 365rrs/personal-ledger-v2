import request from '@/utils/request'

/**
 * 分页查询清洗规则
 */
export function pageRules(data) {
  return request({
    url: '/bill-data-clean-rule/page',
    method: 'post',
    data
  })
}

/**
 * 查询清洗规则详情
 */
export function getRuleDetail(id) {
  return request({
    url: '/bill-data-clean-rule/detail',
    method: 'get',
    params: { id }
  })
}

/**
 * 创建清洗规则
 */
export function createRule(data) {
  console.log('[API createRule] 发送的数据:', data)
  return request({
    url: '/bill-data-clean-rule/create',
    method: 'post',
    data
  })
}

/**
 * 更新清洗规则
 */
export function updateRule(data) {
  console.log('[API updateRule] 发送的数据:', data)
  return request({
    url: '/bill-data-clean-rule/update',
    method: 'post',
    data
  })
}

/**
 * 删除清洗规则
 */
export function deleteRule(id) {
  return request({
    url: '/bill-data-clean-rule/delete',
    method: 'post',
    params: { id }
  })
}

/**
 * 批量清洗账单
 */
export function batchCleanBills(data) {
  return request({
    url: '/bill-data-clean-rule/batch-clean',
    method: 'post',
    data
  })
}
