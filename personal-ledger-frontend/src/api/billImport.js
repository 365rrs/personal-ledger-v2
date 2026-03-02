import request from '@/utils/request'

/**
 * 上传文件并导入
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/bill-import/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 查询导入记录列表
 */
export function pageRecords(data) {
  return request({
    url: '/bill-import/record/page',
    method: 'post',
    data
  })
}

/**
 * 查询导入记录详情
 */
export function getRecord(id) {
  return request({
    url: '/bill-import/record',
    method: 'get',
    params: { id }
  })
}

/**
 * 查询导入明细列表
 */
export function pageDetails(data) {
  return request({
    url: '/bill-import/detail/page',
    method: 'post',
    data
  })
}

/**
 * 查询导入统计
 */
export function getStatistics(importRecordId) {
  return request({
    url: '/bill-import/statistics',
    method: 'get',
    params: { importRecordId }
  })
}

/**
 * 转换为账单
 */
export function convertToBill(data) {
  return request({
    url: '/bill-import/convert',
    method: 'post',
    data
  })
}

/**
 * 跳过记录
 */
export function skipRecords(data) {
  return request({
    url: '/bill-import/skip',
    method: 'post',
    data
  })
}

/**
 * 删除导入记录
 */
export function deleteRecord(id) {
  return request({
    url: '/bill-import/record/delete',
    method: 'post',
    params: { id }
  })
}
