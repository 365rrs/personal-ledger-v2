import request from '@/utils/request'

/**
 * 创建支付渠道
 */
export function createPaymentChannel(data) {
  return request({
    url: '/payment-channel',
    method: 'post',
    data
  })
}

/**
 * 更新支付渠道
 */
export function updatePaymentChannel(data) {
  return request({
    url: '/payment-channel',
    method: 'put',
    data
  })
}

/**
 * 删除支付渠道
 */
export function deletePaymentChannel(id) {
  return request({
    url: `/payment-channel?id=${id}`,
    method: 'delete'
  })
}

/**
 * 查询支付渠道详情
 */
export function getPaymentChannel(id) {
  return request({
    url: `/payment-channel?id=${id}`,
    method: 'get'
  })
}

/**
 * 分页查询支付渠道
 */
export function pagePaymentChannels(data) {
  return request({
    url: '/payment-channel/list',
    method: 'post',
    data
  })
}

/**
 * 查询支付渠道列表（不分页）
 */
export function listPaymentChannels(data) {
  return request({
    url: '/payment-channel/list-all',
    method: 'post',
    data
  })
}

/**
 * 启用/禁用支付渠道
 */
export function togglePaymentChannel(id, enabled) {
  return request({
    url: `/payment-channel/toggle?id=${id}&enabled=${enabled}`,
    method: 'post'
  })
}

/**
 * 更新支付渠道排序
 */
export function updatePaymentChannelSort(id, sortOrder) {
  return request({
    url: `/payment-channel/sort?id=${id}&sortOrder=${sortOrder}`,
    method: 'post'
  })
}
