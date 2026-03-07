import request from '@/utils/request'

/**
 * 分页查询标签列表
 */
export function getTagList(data) {
  return request({
    url: '/tag/page',
    method: 'post',
    data
  })
}

/**
 * 根据 ID 查询标签详情
 */
export function getTagDetail(id) {
  return request({
    url: `/tag/${id}`,
    method: 'get'
  })
}

/**
 * 创建标签
 */
export function createTag(data) {
  return request({
    url: '/tag/create',
    method: 'post',
    data
  })
}

/**
 * 更新标签
 */
export function updateTag(data) {
  return request({
    url: '/tag/update',
    method: 'post',
    data
  })
}

/**
 * 删除标签
 */
export function deleteTag(id) {
  return request({
    url: '/tag/delete',
    method: 'post',
    data: id
  })
}

/**
 * 启用/停用标签
 */
export function updateTagStatus(id, status) {
  return request({
    url: '/tag/status/update',
    method: 'post',
    params: { id, status }
  })
}

/**
 * 更新标签排序序号
 */
export function updateTagSortOrder(id, sortOrder) {
  return request({
    url: '/tag/sort/update',
    method: 'post',
    params: { id, sortOrder }
  })
}
