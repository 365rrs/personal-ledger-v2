<template>
  <div class="payment-channel">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付渠道管理</span>
          <el-button type="primary" @click="handleCreate">+ 新建渠道</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="渠道名称">
          <el-input v-model="queryForm.channelName" placeholder="请输入渠道名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="渠道类型">
          <el-select v-model="queryForm.channelType" placeholder="全部" clearable style="width: 120px">
            <el-option label="现金" value="CASH" />
            <el-option label="银行卡" value="BANK_CARD" />
            <el-option label="信用卡" value="CREDIT_CARD" />
            <el-option label="电子钱包" value="E_WALLET" />
            <el-option label="其他" value="OTHER" />
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
          style="width: 100%"
          v-loading="loading"
          row-key="id"
        >
          <el-table-column label="拖拽" width="60" align="center">
            <template #default>
              <el-icon class="drag-handle" style="cursor: move;"><Rank /></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="channelName" label="渠道名称" width="150" />
          <el-table-column prop="channelType" label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getChannelTypeTagType(row.channelType)">
                {{ getChannelTypeLabel(row.channelType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="icon" label="图标" width="80" align="center">
            <template #default="{ row }">
              <span v-if="row.icon" style="font-size: 18px;">{{ row.icon }}</span>
              <span v-else style="color: #999;">-</span>
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
      :title="isEditMode ? '编辑支付渠道' : '新建支付渠道'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="渠道名称" prop="channelName">
          <el-input v-model="formData.channelName" placeholder="请输入渠道名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="渠道类型" prop="channelType">
          <el-select v-model="formData.channelType" placeholder="请选择渠道类型" clearable style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="银行卡" value="BANK_CARD" />
            <el-option label="信用卡" value="CREDIT_CARD" />
            <el-option label="电子钱包" value="E_WALLET" />
            <el-option label="其他" value="OTHER" />
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
import Sortable from 'sortablejs'
import {
  pagePaymentChannels,
  createPaymentChannel,
  updatePaymentChannel,
  deletePaymentChannel,
  togglePaymentChannel,
  updatePaymentChannelSort
} from '@/api/paymentChannel'

// 渠道类型映射
const channelTypeMap = {
  'CASH': '现金',
  'BANK_CARD': '银行卡',
  'CREDIT_CARD': '信用卡',
  'E_WALLET': '电子钱包',
  'OTHER': '其他'
}

// 渠道类型标签颜色映射
const channelTypeTagMap = {
  'CASH': '',
  'BANK_CARD': 'warning',
  'CREDIT_CARD': 'danger',
  'E_WALLET': 'success',
  'OTHER': 'info'
}

// 获取渠道类型标签
const getChannelTypeLabel = (type) => {
  return channelTypeMap[type] || type
}

// 获取渠道类型标签颜色
const getChannelTypeTagType = (type) => {
  return channelTypeTagMap[type] || ''
}

// 图标选项
const iconOptions = ['💵', '🏦', '💳', '💚', '💙', '🟡', '⚫', '⚪', '💰', '💸']

// 查询表单
const queryForm = reactive({
  channelName: '',
  channelType: '',
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
  channelName: '',
  channelType: '',
  icon: '',
  sortOrder: 0,
  enabled: '1'
})

// 表单验证规则
const formRules = {
  channelName: [
    { required: true, message: '请输入渠道名称', trigger: 'blur' },
    { max: 50, message: '渠道名称长度不能超过 50 个字符', trigger: 'blur' }
  ]
}

// 表格引用
const tableRef = ref()

// 表单引用
const formRef = ref()

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await pagePaymentChannels({
      current: 1,
      size: 100,
      ...queryForm
    })
    tableData.value = res.data.records || []
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.channelName = ''
  queryForm.channelType = ''
  queryForm.enabled = ''
  loadData()
}

// 新建
const handleCreate = () => {
  isEditMode.value = false
  dialogVisible.value = true
  formData.value = {
    id: null,
    channelName: '',
    channelType: '',
    icon: '',
    sortOrder: 0,
    enabled: '1'
  }
}

// 编辑
const handleEdit = (row) => {
  isEditMode.value = true
  dialogVisible.value = true
  formData.value = {
    id: row.id,
    channelName: row.channelName,
    channelType: row.channelType,
    icon: row.icon,
    sortOrder: row.sortOrder,
    enabled: row.enabled
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  const newStatus = row.enabled === '1' ? '0' : '1'
  const action = newStatus === '1' ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${action}该渠道吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await togglePaymentChannel(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该渠道吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePaymentChannel(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEditMode.value) {
      await updatePaymentChannel(formData.value)
      ElMessage.success('更新成功')
    } else {
      await createPaymentChannel(formData.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 初始化拖拽排序
const initSortable = () => {
  const tbody = tableRef.value.$el.querySelector('.el-table__body-wrapper tbody')
  Sortable.create(tbody, {
    animation: 150,
    handle: '.drag-handle',
    onEnd: async (evt) => {
      try {
        // 创建新数组并重新排列（不修改原数组）
        const newArray = [...tableData.value].map(c => ({...c}))
        const moved = newArray.splice(evt.oldIndex, 1)[0]
        newArray.splice(evt.newIndex, 0, moved)
        
        console.log('移动后的数组:', newArray)
        
        // 批量更新排序（从 1 开始）
        const updates = newArray.map((item, idx) => ({ 
          id: item.id,
          sortOrder: idx + 1
        }))
        
        console.log('需要更新的排序:', updates)
        
        // 批量更新数据库
        if (updates.length > 0) {
          const updatePromises = updates.map(item => updatePaymentChannelSort(item.id, item.sortOrder))
          await Promise.all(updatePromises)
        }
        
        ElMessage.success('排序已更新')
        
        // 等待一小段时间再重新加载数据，确保后端更新完成
        await new Promise(resolve => setTimeout(resolve, 300))
        
        // 重新加载数据以刷新排序显示
        await loadData()
      } catch (error) {
        console.error('拖拽排序失败:', error)
        ElMessage.error(error.message || '排序失败')
        // 失败时重新加载数据恢复原状
        await loadData()
      }
    }
  })
}

onMounted(() => {
  loadData()
  nextTick(() => {
    initSortable()
  })
})
</script>

<style scoped>
.payment-channel {
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
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.icon-option {
  font-size: 24px;
  cursor: pointer;
  padding: 8px;
  text-align: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.icon-option:hover {
  background-color: #e6e8eb;
  transform: scale(1.1);
}

.icon-option.selected {
  background-color: #409eff;
  color: white;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
}

.drag-handle {
  font-size: 18px;
  color: #909399;
  cursor: move;
}

.drag-handle:hover {
  color: #409eff;
}
</style>
