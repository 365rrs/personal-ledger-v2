<template>
  <div class="category">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button type="primary" @click="handleCreate">+ 新建分类</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="分类类型">
          <el-select v-model="queryForm.categoryType" placeholder="全部" clearable style="width: 120px">
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.enabled" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <div class="table-area">
        <el-table 
          ref="tableRef"
          :data="tableData" 
          stripe 
          style="width: 100%"
          v-loading="loading"
          row-key="id"
        >
          <el-table-column label="拖拽" width="60" align="center">
            <template #default>
              <el-icon class="drag-handle" style="cursor: move;"><Rank /></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类名称" width="150" />
          <el-table-column prop="icon" label="图标" width="80" align="center">
            <template #default="{ row }">
              <span v-if="row.icon" style="font-size: 18px;">{{ row.icon }}</span>
              <span v-else style="color: #999;">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="categoryType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.categoryType === 'INCOME' ? 'success' : 'warning'">
                {{ row.categoryType === 'INCOME' ? '收入' : '支出' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="enabled" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled === '1' ? 'success' : 'info'">
                {{ row.enabled === '1' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button 
                :type="row.enabled === '1' ? 'warning' : 'success'" 
                size="small" 
                @click="handleToggleStatus(row)"
              >
                {{ row.enabled === '1' ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑分类' : '新建分类'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="formData.categoryName" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="分类类型" prop="categoryType">
          <el-radio-group v-model="formData.categoryType" :disabled="isEditMode">
            <el-radio value="INCOME">收入</el-radio>
            <el-radio value="EXPENSE">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select 
            v-model="formData.parentId" 
            placeholder="选择父分类（可选）"
            clearable
            :disabled="isEditMode"
            style="width: 100%"
          >
            <el-option
              v-for="parent in parentOptions"
              :key="parent.id"
              :label="parent.categoryName"
              :value="parent.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <div class="icon-selector">
            <div class="icon-options">
              <span 
                v-for="icon in iconOptions" 
                :key="icon"
                :class="['icon-option', { selected: formData.icon === icon }]"
                @click="formData.icon = icon"
              >
                {{ icon }}
              </span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import {
  getCategoryList,
  createCategory,
  updateCategory,
  deleteCategory,
  toggleCategoryStatus
} from '@/api/category'
import Sortable from 'sortablejs'
import request from '@/utils/request'

// 更新分类排序
function updateCategorySortOrder(id, sortOrder) {
  return request({
    url: '/category/sort/update',
    method: 'post',
    params: { id, sortOrder }
  })
}

// 查询表单
const queryForm = reactive({
  categoryType: '',
  enabled: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 弹窗控制
const dialogVisible = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)

// 表单数据
const formData = ref({
  id: null,
  categoryName: '',
  categoryType: 'EXPENSE',
  parentId: null,
  icon: '',
  sortOrder: 0
})

// 表单验证规则
const formRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '分类名称不能超过 50 个字符', trigger: 'blur' }
  ],
  categoryType: [
    { required: true, message: '请选择分类类型', trigger: 'change' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序序号', trigger: 'blur' }
  ]
}

const formRef = ref(null)
const tableRef = ref(null)

// 拖拽排序实例
let sortableInstance = null
let draggedRowData = null

// 初始化拖拽排序
const initSortable = () => {
  if (!tableRef.value) return
  
  // 等待 DOM 更新
  nextTick(() => {
    // 获取表格的 tbody 元素
    const tbody = tableRef.value.$el.querySelector('.el-table__body-wrapper tbody')
    if (!tbody) {
      console.log('未找到 tbody 元素')
      return
    }
    
    // 如果已经初始化过，先销毁
    if (sortableInstance) {
      sortableInstance.destroy()
    }
    
    // 创建 Sortable 实例，使用 handle 选项只对拖拽手柄响应
    sortableInstance = new Sortable(tbody, {
      animation: 150,
      handle: '.drag-handle',  // 只对拖拽图标响应
      ghostClass: 'sortable-ghost',
      dragClass: 'sortable-drag',
      onStart: (evt) => {
        const allRows = Array.from(tbody.children)
        const rowIndex = evt.oldIndex
        // 使用 el-table__row--level-1 判断是否是二级分类
        const isChild = evt.item.classList.contains('el-table__row--level-1')
        
        console.log('拖拽开始:', { 
          oldIndex: evt.oldIndex,
          newIndex: evt.newIndex,
          isChild,
          totalRows: allRows.length 
        })
        
        if (isChild) {
          // 二级分类：找到其父分类
          let parentIdx = rowIndex - 1
          while (parentIdx >= 0 && allRows[parentIdx].classList.contains('el-table__row--level-1')) {
            parentIdx--
          }
          if (parentIdx >= 0) {
            // 计算父分类在一级分类中的索引
            const parentRowIndices = allRows.filter((r, i) => i <= parentIdx && !r.classList.contains('el-table__row--level-1'))
            const parentIdxInParents = parentRowIndices.length - 1
            const parents = tableData.value.filter(c => !c.parentId)
            const parent = parents[parentIdxInParents]
            
            if (parent && parent.children) {
              // 计算子分类在子分类数组中的索引
              const childRowIndices = []
              let idx = parentIdx + 1
              while (idx < allRows.length && allRows[idx].classList.contains('el-table__row--level-1')) {
                childRowIndices.push(idx)
                idx++
              }
              const childIdxInSiblings = rowIndex - parentIdx - 1
              draggedRowData = { 
                category: parent.children[childIdxInSiblings], 
                isChild: true, 
                parent,
                childIdxInSiblings 
              }
            }
          }
        } else {
          // 一级分类：计算在一级分类数组中的索引
          const parentRowIndices = allRows.filter((r, i) => i <= rowIndex && !r.classList.contains('el-table__row--level-1'))
          const parents = tableData.value.filter(c => !c.parentId)
          const parentIdxInParents = parentRowIndices.length - 1
          draggedRowData = { 
            category: parents[parentIdxInParents], 
            isChild: false,
            parentIdxInParents 
          }
        }
      },
      onEnd: async (evt) => {
        console.log('拖拽结束:', { 
          oldIndex: evt.oldIndex, 
          newIndex: evt.newIndex,
          draggedRowData 
        })
        
        if (evt.oldIndex === evt.newIndex || !draggedRowData) {
          draggedRowData = null
          return
        }
        
        // 根据是一级还是二级分类分别处理
        if (draggedRowData.isChild) {
          await handleChildDrag(draggedRowData, evt.oldIndex, evt.newIndex)
        } else {
          await handleParentDrag(draggedRowData, evt.oldIndex, evt.newIndex)
        }
        
        draggedRowData = null
      }
    })
    
    console.log('拖拽排序初始化完成')
  })
}

// 处理一级分类拖拽
const handleParentDrag = async (draggedData, oldDomIndex, newDomIndex) => {
  console.log('处理一级分类拖拽:', draggedData, 'DOM 索引:', oldDomIndex, '->', newDomIndex)
  
  // 获取 tbody 中的所有行
  const tbody = tableRef.value.$el.querySelector('.el-table__body-wrapper tbody')
  const allRows = Array.from(tbody.children)
  
  // 找出所有一级分类的行
  const parentRows = allRows.filter(row => !row.classList.contains('el-table__row--level-1'))
  
  // 计算新的一级分类索引
  const newParentIndex = parentRows.indexOf(allRows[newDomIndex])
  
  console.log('一级分类行数量:', parentRows.length)
  console.log('新的一级分类索引:', newParentIndex)
  
  if (newParentIndex === -1) {
    console.error('无效的新一级分类索引')
    ElMessage.error('拖拽排序失败：无法确定目标位置')
    return
  }
  
  // 获取所有一级分类（从原始数据中获取，保持完整结构）
  const parents = tableData.value.filter(c => !c.parentId)
  const oldRealIndex = parents.findIndex(p => p.id === draggedData.category.id)
  
  console.log('旧的一级分类索引:', oldRealIndex)
  
  if (oldRealIndex === -1) {
    console.error('无效的一级分类 ID')
    ElMessage.error('拖拽排序失败：找不到对应的分类')
    return
  }
  
  // 在新数组中移动（不修改原数组，创建新数组）
  const newParents = [...parents]
  const moved = newParents.splice(oldRealIndex, 1)[0]
  newParents.splice(newParentIndex, 0, moved)
  
  console.log('移动后的一级分类数组:', newParents)
  
  // 批量更新排序（从 1 开始），传递完整的分类对象
  const updates = newParents.map((item, idx) => ({ 
    id: item.id,
    categoryName: item.categoryName,
    categoryType: item.categoryType,
    parentId: item.parentId,
    icon: item.icon,
    sortOrder: idx + 1,
    enabled: item.enabled
  }))
  await batchUpdateSort(updates)
}

// 处理二级分类拖拽
const handleChildDrag = async (draggedData, oldDomIndex, newDomIndex) => {
  console.log('处理二级分类拖拽:', draggedData, 'DOM 索引:', oldDomIndex, '->', newDomIndex)
  
  // 获取 tbody 中的所有行
  const tbody = tableRef.value.$el.querySelector('.el-table__body-wrapper tbody')
  const allRows = Array.from(tbody.children)
  
  // 找到新的父分类行
  let parentRow = null
  for (let i = newDomIndex - 1; i >= 0; i--) {
    if (!allRows[i].classList.contains('el-table__row--level-1')) {
      parentRow = allRows[i]
      break
    }
  }
  
  if (!parentRow) {
    console.error('未找到父分类行')
    ElMessage.error('拖拽排序失败：无法确定父分类')
    return
  }
  
  // 获取该父分类下的所有子分类行
  const childRows = []
  let idx = allRows.indexOf(parentRow) + 1
  while (idx < allRows.length && allRows[idx].classList.contains('el-table__row--level-1')) {
    childRows.push(allRows[idx])
    idx++
  }
  
  // 计算在新的子分类中的索引
  const newChildIndex = childRows.indexOf(allRows[newDomIndex])
  
  console.log('新的子分类索引:', newChildIndex)
  
  if (newChildIndex === -1) {
    console.error('无效的子分类索引')
    ElMessage.error('拖拽排序失败：无法确定目标位置')
    return
  }
  
  // 获取父分类及其子分类
  const parent = draggedData.parent
  if (!parent || !parent.children) {
    console.error('父分类或子分类不存在')
    ElMessage.error('拖拽排序失败：父分类数据异常')
    return
  }
  
  const oldRealIndex = parent.children.findIndex(c => c.id === draggedData.category.id)
  if (oldRealIndex === -1) {
    console.error('无效的子分类 ID')
    ElMessage.error('拖拽排序失败：找不到对应的子分类')
    return
  }
  
  // 在子分类数组中移动
  const moved = parent.children[oldRealIndex]
  parent.children.splice(oldRealIndex, 1)
  parent.children.splice(newChildIndex, 0, moved)
  
  // 批量更新子分类排序
  const updates = parent.children.map((item, idx) => ({ ...item, sortOrder: idx + 1 }))
  await batchUpdateSort(updates)
}

// 批量更新排序
const batchUpdateSort = async (categories) => {
  try {
    console.log('开始批量更新排序，categories:', categories)
    
    if (!categories || categories.length === 0) {
      throw new Error('分类列表为空')
    }
    
    const updatePromises = categories.map((cat, idx) => {
      if (!cat || !cat.id) {
        console.error('无效的分类对象:', cat, '索引:', idx)
        throw new Error(`索引 ${idx} 的分类对象无效`)
      }
      const newSortOrder = idx + 1
      console.log(`更新 ${cat.categoryName} (ID:${cat.id}) sortOrder: ${cat.sortOrder} -> ${newSortOrder}`)
      return updateCategorySortOrder(cat.id, newSortOrder)
    })
    
    await Promise.all(updatePromises)
    ElMessage.success('排序已更新')
    
    // 等待一小段时间再重新加载数据，确保后端更新完成
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 重新加载数据
    await fetchData()
  } catch (error) {
    console.error('批量更新排序失败:', error)
    ElMessage.error(error.message || '排序更新失败')
    await fetchData()
  }
}

// 拖拽结束处理
const handleDragEnd = async (evt) => {
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCategoryList(queryForm.categoryType, queryForm.enabled)
    tableData.value = res.data || []
    // 数据加载完成后，等待 DOM 更新再初始化拖拽
    await nextTick()
    initSortable()
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 加载父分类选项
const loadParentOptions = async () => {
  try {
    // 只加载一级分类作为父分类选项
    const res = await getCategoryList('', '')
    parentOptions.value = (res.data || []).filter(item => !item.parentId)
  } catch (error) {
    console.error('加载父分类选项失败:', error)
  }
}

// 图标选项
const iconOptions = [
  '🍔', '🍜', '☕', '🍰', '🚗', '🚌', '🚕', '✈️',
  '🛒', '🎁', '💰', '💳', '🏠', '💊', '📚', '🎮',
  '🎬', '🎵', '⚽', '🏊', '💄', '👗', '📱', '💻'
]

// 父分类选项（用于选择器）
const parentOptions = ref([])

// 查询
const handleQuery = () => {
  fetchData()
}

// 重置
const handleReset = () => {
  queryForm.categoryType = ''
  queryForm.enabled = ''
  fetchData()
}

// 新建
const handleCreate = () => {
  isEditMode.value = false
  dialogVisible.value = true
  formData.value = {
    id: null,
    categoryName: '',
    categoryType: 'EXPENSE',
    parentId: null,
    icon: '',
    sortOrder: 0
  }
}

// 编辑
const handleEdit = async (row) => {
  isEditMode.value = true
  dialogVisible.value = true
  formData.value = {
    id: row.id,
    categoryName: row.categoryName,
    categoryType: row.categoryType,
    parentId: row.parentId,
    icon: row.icon || '',
    sortOrder: row.sortOrder
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEditMode.value) {
        await updateCategory(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createCategory(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('保存分类失败:', error)
      ElMessage.error(error.message || '保存失败')
    } finally {
      submitting.value = false
    }
  })
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = row.enabled === '1' ? '0' : '1'
  const actionText = newStatus === '1' ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}该分类吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    await toggleCategoryStatus(row.id, newStatus)
    ElMessage.success(`${actionText}成功`)
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换状态失败:', error)
      ElMessage.error(error.message || '切换状态失败')
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.categoryName}"吗？`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  fetchData()
  loadParentOptions()
})
</script>

<style scoped>
.category {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

.table-area {
  margin-top: 20px;
}

.icon-selector {
  width: 100%;
}

.icon-options {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 12px;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.icon-option {
  font-size: 24px;
  cursor: pointer;
  text-align: center;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.icon-option:hover {
  background-color: #f5f7fa;
  transform: scale(1.1);
}

.icon-option.selected {
  background-color: #ecf5ff;
  border: 2px solid #409eff;
}

/* 拖拽排序样式 */
.sortable-ghost {
  opacity: 0.4;
  background-color: #f5f7fa;
}

.sortable-drag {
  opacity: 0.8;
}

.el-table__row {
  cursor: move;
}
</style>

