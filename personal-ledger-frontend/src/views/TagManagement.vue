<template>
  <div class="tag-container">
    <!-- 标题栏 -->
    <div class="header">
      <h2>标签管理</h2>
      <el-button type="primary" @click="handleCreate">
        <i class="el-icon-plus"></i> 新建标签
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-area">
      <el-form :inline="true" :model="queryForm" size="default">
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
    </div>

    <!-- 表格区域 -->
    <div class="table-area">
      <el-table 
        :data="tableData" 
        stripe 
        style="width: 100%"
        v-loading="loading"
      >
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
        <el-table-column prop="tagStatus" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.tagStatus === 'enable' ? 'success' : 'info'"
              size="small"
            >
              {{ row.tagStatus === 'enable' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usageCount" label="使用次数" width="100" align="center" />
        <el-table-column label="操作" fixed="right" width="280" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleToggleStatus(row)">
              {{ row.tagStatus === 'enable' ? '停用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button link type="primary" @click="handleSortUp(row)" :disabled="row.sortOrder === 1">↑</el-button>
            <el-button link type="primary" @click="handleSortDown(row)">↓</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>

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
          <el-color-picker v-model="formData.tagColor" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTagList, getTagDetail, createTag, updateTag, deleteTag, updateTagStatus } from '@/api/tag'

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

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTagList(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取标签列表失败:', error)
    ElMessage.error('获取标签列表失败')
  } finally {
    loading.value = false
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
  // TODO: 实现排序逻辑
  ElMessage.info('排序功能待实现')
}

// 下移排序
const handleSortDown = async (row) => {
  // TODO: 实现排序逻辑
  ElMessage.info('排序功能待实现')
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
.tag-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 4px;
}

.header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.filter-area {
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.table-area {
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 4px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.color-dot {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #ddd;
}
</style>
