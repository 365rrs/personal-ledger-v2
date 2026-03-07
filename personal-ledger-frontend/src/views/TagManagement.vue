<template>
  <div class="tag-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Collection /></el-icon>
            <span>标签管理</span>
          </div>
          <el-button type="primary" @click="handleCreate" :icon="Plus">+ 新建标签</el-button>
        </div>
      </template>

      <!-- 筛选区域 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="标签名称">
          <el-input 
            v-model="queryForm.tagName" 
            placeholder="请输入标签名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="标签分类">
          <el-input 
            v-model="queryForm.tagCategory" 
            placeholder="请输入标签分类"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="queryForm.tagStatus" 
            placeholder="全部状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" value="enable" />
            <el-option label="停用" value="disable" />
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
        <el-table-column prop="tagName" label="标签名称" width="150" />
        <el-table-column prop="tagCategory" label="分类" width="120" />
        <el-table-column prop="tagColor" label="颜色" width="100">
          <template #default="{ row }">
            <span 
              class="color-dot" 
              :style="{ backgroundColor: row.tagColor }"
            ></span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="tagStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.tagStatus === 'enable' ? 'success' : 'info'">
              {{ row.tagStatus === 'enable' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usageCount" label="使用次数" width="100" align="center" />
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.tagStatus === 'enable' ? 'warning' : 'success'" 
              size="small" 
              @click="handleToggleStatus(row)"
            >
              {{ row.tagStatus === 'enable' ? '停用' : '启用' }}
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
      :title="isEditMode ? '编辑标签' : '新建标签'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="formData.tagName" placeholder="请输入标签名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="标签分类" prop="tagCategory">
          <el-select 
            v-model="formData.tagCategory" 
            placeholder="请选择或输入分类"
            allow-create
            filterable
            style="width: 100%"
          >
            <el-option label="时间类" value="时间类" />
            <el-option label="场景类" value="场景类" />
            <el-option label="重要程度" value="重要程度" />
            <el-option label="频率类" value="频率类" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签颜色" prop="tagColor">
          <el-color-picker v-model="formData.tagColor" :predefine="colorPresets" />
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" />
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
import { Collection, Plus, Rank } from '@element-plus/icons-vue'
import { getTagList, getTagDetail, createTag, updateTag, deleteTag, updateTagStatus, updateTagSortOrder } from '@/api/tag'
import Sortable from 'sortablejs'

// 16 种预设颜色
const colorPresets = [
  '#ff6b6b', // 红色
  '#ee5a6f', // 粉红
  '#f06595', // 玫红
  '#cc5de8', // 紫色
  '#845ef7', // 深紫
  '#5c7cfa', // 蓝色
  '#339af0', // 天蓝
  '#22b8cf', // 青色
  '#20c997', // 青绿
  '#51cf66', // 绿色
  '#94d82d', // 黄绿
  '#fcc419', // 黄色
  '#ff922b', // 橙色
  '#fd7e14', // 深橙
  '#e8590c', // 棕橙
  '#a61e4d'  // 暗红
]

// 查询表单
const queryForm = reactive({
  current: 1,
  size: 20,
  tagName: '',
  tagCategory: '',
  tagStatus: ''
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 弹窗控制
const dialogVisible = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)

// 表单数据
const formData = ref({
  id: null,
  tagName: '',
  tagCategory: '',
  tagColor: '#1890ff',
  sortOrder: 0
})

// 表单验证规则
const formRules = {
  tagName: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { max: 50, message: '标签名称不能超过 50 个字符', trigger: 'blur' }
  ]
}

const formRef = ref(null)
const tableRef = ref(null)

// 拖拽排序实例
let sortableInstance = null
let draggedRowData = null

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTagList(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
    
    // 数据加载完成后初始化拖拽排序
    await nextTick()
    initSortable()
  } catch (error) {
    console.error('获取标签列表失败:', error)
    ElMessage.error('获取标签列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化拖拽排序
const initSortable = () => {
  if (!tableRef.value) return
  
  // 获取表格的 tbody 元素
  const tbody = tableRef.value.$el.querySelector('.el-table__body-wrapper tbody')
  if (!tbody) return
  
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
      console.log('拖拽开始:', { 
        oldIndex: evt.oldIndex,
        newIndex: evt.newIndex,
        totalRows: Array.from(tbody.children).length 
      })
    },
    onEnd: async (evt) => {
      console.log('拖拽结束:', { 
        oldIndex: evt.oldIndex, 
        newIndex: evt.newIndex
      })
      await handleDragEnd(evt)
    }
  })
  
  console.log('拖拽排序初始化完成')
}

// 拖拽结束处理
const handleDragEnd = async (evt) => {
  const { oldIndex, newIndex } = evt
  
  console.log('处理拖拽结束:', { oldIndex, newIndex })
  
  if (oldIndex === newIndex) return
  
  try {
    // 创建新数组并重新排列（不修改原数组）
    const newArray = [...tableData.value].map(c => ({...c}))
    const moved = newArray.splice(oldIndex, 1)[0]
    newArray.splice(newIndex, 0, moved)
    
    console.log('移动后的数组:', newArray)
    
    // 批量更新排序（从 1 开始）
    const updates = newArray.map((item, idx) => ({ 
      id: item.id,
      sortOrder: idx + 1
    }))
    
    console.log('需要更新的排序:', updates)
    
    // 批量更新数据库
    if (updates.length > 0) {
      const updatePromises = updates.map(item => updateTagSortOrder(item.id, item.sortOrder))
      await Promise.all(updatePromises)
    }
    
    ElMessage.success('排序已更新')
    
    // 等待一小段时间再重新加载数据，确保后端更新完成
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 重新加载数据以刷新排序显示
    await fetchData()
  } catch (error) {
    console.error('拖拽排序失败:', error)
    ElMessage.error(error.message || '排序失败')
    // 失败时重新加载数据恢复原状
    await fetchData()
  }
}

// 查询
const handleQuery = () => {
  queryForm.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryForm.current = 1
  queryForm.tagName = ''
  queryForm.tagCategory = ''
  queryForm.tagStatus = ''
  fetchData()
}

// 新建
const handleCreate = () => {
  isEditMode.value = false
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEditMode.value = true
  try {
    const res = await getTagDetail(row.id)
    formData.value = {
      id: res.data.id,
      tagName: res.data.tagName,
      tagCategory: res.data.tagCategory || '',
      tagColor: res.data.tagColor || '#1890ff',
      sortOrder: res.data.sortOrder || 0
    }
    dialogVisible.value = true
  } catch (error) {
    console.error('获取标签详情失败:', error)
    ElMessage.error('获取标签详情失败')
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
        await updateTag(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createTag(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('保存标签失败:', error)
      ElMessage.error(error.message || '保存失败')
    } finally {
      submitting.value = false
    }
  })
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = row.tagStatus === 'enable' ? 'disable' : 'enable'
  const actionText = newStatus === 'enable' ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}该标签吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    await updateTagStatus(row.id, newStatus)
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
      `确定要删除标签"${row.tagName}"吗？`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    await deleteTag(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 上移排序
const handleSortUp = async (row) => {
  if (row.sortOrder <= 0) {
    ElMessage.info('已经是最靠前位置')
    return
  }
  
  try {
    // 查询上一个排序位置的标签
    const prevSortOrder = row.sortOrder - 1
    
    // 更新当前标签的排序序号
    await updateTagSortOrder(row.id, prevSortOrder)
    
    // 找到原来排在前面的标签，将它的排序序号 +1
    const currentTagIndex = tableData.value.findIndex(item => item.id === row.id)
    if (currentTagIndex > 0) {
      const prevTag = tableData.value[currentTagIndex - 1]
      await updateTagSortOrder(prevTag.id, row.sortOrder)
    }
    
    ElMessage.success('上移成功')
    fetchData()
  } catch (error) {
    console.error('上移排序失败:', error)
    ElMessage.error(error.message || '上移失败')
  }
}

// 下移排序
const handleSortDown = async (row) => {
  try {
    // 查询下一个排序位置的标签
    const nextSortOrder = row.sortOrder + 1
    
    // 更新当前标签的排序序号
    await updateTagSortOrder(row.id, nextSortOrder)
    
    // 找到原来排在后面的标签，将它的排序序号 -1
    const currentTagIndex = tableData.value.findIndex(item => item.id === row.id)
    if (currentTagIndex < tableData.value.length - 1) {
      const nextTag = tableData.value[currentTagIndex + 1]
      await updateTagSortOrder(nextTag.id, row.sortOrder)
    }
    
    ElMessage.success('下移成功')
    fetchData()
  } catch (error) {
    console.error('下移排序失败:', error)
    ElMessage.error(error.message || '下移失败')
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.value = {
    id: null,
    tagName: '',
    tagCategory: '',
    tagColor: '#1890ff',
    sortOrder: 0
  }
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.tag-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 18px;
}

.query-form {
  margin-bottom: 20px;
}

.table-area {
  margin-top: 20px;
}

.color-dot {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #ddd;
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

.drag-handle {
  cursor: move;
  color: #909399;
}

.drag-handle:hover {
  color: #409eff;
}
</style>
