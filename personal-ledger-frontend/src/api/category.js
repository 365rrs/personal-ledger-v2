import request from '@/utils/request'

/**
 * 创建分类
 */
export function createCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 */
export function updateCategory(data) {
  return request({
    url: '/category/update',
    method: 'post',
    data
  })
}

/**
 * 删除分类
 */
export function deleteCategory(id) {
  return request({
    url: '/category/delete',
    method: 'post',
    params: { id }
  })
}

/**
 * 启用/禁用分类
 */
export function toggleCategoryStatus(id, enabled) {
  return request({
    url: '/category/toggle',
    method: 'post',
    params: { id, enabled }
  })
}

/**
 * 查询分类列表（树形结构）
 */
export function getCategoryList(categoryType, enabled) {
  return request({
    url: '/category/list',
    method: 'post',
    params: { categoryType, enabled }
  })
}

/**
 * 查询分类树
 */
export function getCategoryTree(categoryType, enabled) {
  return request({
    url: '/category/tree',
    method: 'get',
    params: { categoryType, enabled }
  })
}

/**
 * 根据 ID 查询分类
 */
export function getCategoryDetail(id) {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}

/**
 * 更新分类排序
 */
export function updateCategorySortOrder(id, sortOrder) {
  return request({
    url: '/category/sort/update',
    method: 'post',
    params: { id, sortOrder }
  })
}
