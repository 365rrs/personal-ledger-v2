<template>
  <div class="pending-expense-container">
    <!-- 14.2 筛选条件区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" label-width="100px">
        <!-- 快捷日期选择 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="快捷日期">
              <el-radio-group v-model="quickDate" @change="handleQuickDateChange" size="default">
                <el-radio-button label="today">今天</el-radio-button>
                <el-radio-button label="yesterday">昨天</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="lastWeek">上周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="lastMonth">上月</el-radio-button>
                <el-radio-button label="year">本年</el-radio-button>
                <el-radio-button label="lastYear">去年</el-radio-button>
                <el-radio-button label="custom">自定义</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 月份选择 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="选择月份">
              <el-date-picker
                v-model="selectedMonth"
                type="month"
                placeholder="选择月份"
                value-format="YYYY-MM"
                @change="handleMonthChange"
                style="width: 100%;"
                clearable
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="16">
            <el-form-item label="支付日期">
              <el-date-picker
                v-model="paymentDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                @change="handleDateRangeChange"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="项目名称">
              <el-input
                v-model="queryForm.expenseNameKeyword"
                placeholder="请输入项目名称关键词"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="周期">
              <el-select
                v-model="queryForm.periods"
                multiple
                placeholder="请选择周期"
                clearable
                style="width: 100%;"
              >
                <el-option label="每月" value="MONTHLY" />
                <el-option label="每年" value="YEARLY" />
                <el-option label="一次性" value="ONETIME" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="计划类型">
              <el-select
                v-model="queryForm.planTypes"
                multiple
                placeholder="请选择计划类型"
                clearable
                style="width: 100%;"
              >
                <el-option label="刚性支出" value="RIGID" />
                <el-option label="意向计划支出" value="INTENDED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select
                v-model="queryForm.statuses"
                multiple
                placeholder="请选择状态"
                clearable
                style="width: 100%;"
              >
                <el-option label="待支付" value="PENDING" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="分类">
              <el-select
                v-model="queryForm.categoryId"
                placeholder="请选择分类"
                filterable
                clearable
                style="width: 100%;"
              >
                <el-option
                  v-for="cat in categoryList"
                  :key="cat.id"
                  :label="cat.categoryName"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="16">
            <el-form-item label-width="0">
              <el-button type="primary" @click="handleQuery" :icon="Search">
                查询
              </el-button>
              <el-button @click="handleReset" :icon="Refresh">
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
    
    <!-- 待支付金额汇总展示 -->
    <el-card class="summary-card" shadow="never">
      <el-row :gutter="20">
        <el-col :span="24">
          <div class="summary-item">
            <span class="summary-label">待支付金额汇总：</span>
            <span class="summary-value">
              ¥{{ totalPendingAmount.toFixed(2) }}
            </span>
            <span class="summary-tip">
              （根据当前查询条件统计，仅统计"待支付"状态的项目）
            </span>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 14.3 操作按钮区域 -->
    <el-card class="operation-card" shadow="never">
      <el-row :gutter="10">
        <el-col :span="24">
          <el-button type="primary" @click="handleCreate" :icon="Plus">
            新建待支出
          </el-button>
          <el-button type="success" @click="handleRecurringCreate" :icon="DocumentAdd">
            批量创建周期性支出
          </el-button>
          <el-button type="info" @click="handleExport" :icon="Download">
            导出Excel
          </el-button>
          <el-button 
            type="success" 
            @click="handleBatchMarkCompleted" 
            :disabled="selectedIds.length === 0"
            :icon="Select"
          >
            批量标记已完成
          </el-button>
          <el-button 
            type="warning" 
            @click="handleBatchMarkCancelled" 
            :disabled="selectedIds.length === 0"
            :icon="Close"
          >
            批量标记已取消
          </el-button>
          <el-button 
            type="danger" 
            @click="handleBatchDelete" 
            :disabled="selectedIds.length === 0"
            :icon="Delete"
          >
            批量删除
          </el-button>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 14.4 数据表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        style="width: 100%;"
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column prop="expenseName" label="项目名称" min-width="300" show-overflow-tooltip />
        
        <el-table-column prop="amount" label="金额" width="120" align="right" sortable="custom">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: 500;">
              ¥{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="paymentDate" label="支付日期" width="120" align="center" sortable="custom" />
        
        <el-table-column prop="periodName" label="周期" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getPeriodTagType(row.period)" size="small">
              {{ row.periodName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="planTypeName" label="计划类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getPlanTypeTagType(row.planType)" size="small">
              {{ row.planTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="statusName" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.categoryName || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" sortable="custom">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row.id)" :icon="View">
              查看
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row.id)" :icon="Edit">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 14.5 分页区域 -->
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 14.10 集成子组件 -->
    <!-- 创建/编辑对话框 -->
    <PendingExpenseEditDialog
      v-model="editDialogVisible"
      :mode="editMode"
      :edit-id="currentEditId"
      @success="handleDialogSuccess"
    />
    
    <!-- 详情查看对话框 -->
    <PendingExpenseDetailDialog
      v-model="detailDialogVisible"
      :detail-id="currentDetailId"
      @edit="handleEdit"
    />
    
    <!-- 周期性支出批量创建对话框 -->
    <RecurringExpenseDialog
      v-model="recurringDialogVisible"
      @success="handleDialogSuccess"
    />
    
    <!-- 批量操作失败详情对话框 -->
    <el-dialog
      v-model="batchResultDialogVisible"
      title="批量操作结果"
      width="600px"
    >
      <el-result
        :icon="batchResult.failCount === 0 ? 'success' : 'warning'"
        :title="batchResult.failCount === 0 ? '操作成功' : '操作完成（部分失败）'"
      >
        <template #sub-title>
          <div style="margin-bottom: 10px;">
            <span style="color: #67C23A; font-weight: bold; font-size: 16px;">
              成功: {{ batchResult.successCount }} 条
            </span>
            <span v-if="batchResult.failCount > 0" style="margin-left: 20px; color: #F56C6C; font-weight: bold; font-size: 16px;">
              失败: {{ batchResult.failCount }} 条
            </span>
          </div>
        </template>
      </el-result>
      
      <div v-if="batchResult.failureDetails && batchResult.failureDetails.length > 0" style="margin-top: 20px;">
        <el-divider>失败详情</el-divider>
        <el-scrollbar max-height="300px">
          <el-alert
            v-for="(detail, index) in batchResult.failureDetails"
            :key="index"
            :title="`ID: ${detail.id} - ${detail.reason}`"
            type="error"
            :closable="false"
            style="margin-bottom: 10px;"
          />
        </el-scrollbar>
      </div>
      
      <template #footer>
        <el-button type="primary" @click="batchResultDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Plus, 
  DocumentAdd, 
  Download, 
  Select, 
  Close, 
  Delete, 
  Edit,
  View
} from '@element-plus/icons-vue'
import {
  pagePendingExpenses,
  batchMarkAsCompleted,
  batchMarkAsCancelled,
  batchDelete,
  exportToExcel,
  getPendingAmountByQuery
} from '@/api/pendingExpense'
import { getCategoryList } from '@/api/category'
import PendingExpenseEditDialog from './components/PendingExpenseEditDialog.vue'
import PendingExpenseDetailDialog from './components/PendingExpenseDetailDialog.vue'
import RecurringExpenseDialog from './components/RecurringExpenseDialog.vue'

// 14.6 数据状态管理
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const totalPendingAmount = ref(0) // 待支付金额汇总
const selectedIds = ref([])
const categoryList = ref([])
const paymentDateRange = ref([])
const quickDate = ref('custom')
const selectedMonth = ref('')

const queryForm = reactive({
  expenseNameKeyword: '',
  periods: [],
  planTypes: [],
  statuses: [],
  paymentDateStart: '',
  paymentDateEnd: '',
  categoryId: null,
  sortField: 'paymentDate',
  sortOrder: 'asc',
  pageNum: 1,
  pageSize: 20
})

// 对话框状态
const editDialogVisible = ref(false)
const editMode = ref('create')
const currentEditId = ref(null)
const detailDialogVisible = ref(false)
const currentDetailId = ref(null)
const recurringDialogVisible = ref(false)
const batchResultDialogVisible = ref(false)
const batchResult = reactive({
  successCount: 0,
  failCount: 0,
  failureDetails: []
})

// 14.6 加载分类和支付渠道数据
const loadFilterData = async () => {
  try {
    const catRes = await getCategoryList('EXPENSE', '1')
    
    // 扁平化分类列表
    const flattenCategories = (categories) => {
      const result = []
      categories.forEach(cat => {
        if (cat.parentId === null || cat.parentId === undefined) {
          result.push(cat)
        }
        if (cat.children && cat.children.length > 0) {
          result.push(...cat.children)
        }
      })
      return result
    }
    
    categoryList.value = flattenCategories(catRes.data || [])
  } catch (error) {
    console.error('加载筛选数据失败:', error)
  }
}

// 14.6 加载列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await pagePendingExpenses(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    
    // 同时加载待支付金额汇总
    await fetchPendingAmount()
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 14.6 加载待支付金额汇总
const fetchPendingAmount = async () => {
  try {
    const res = await getPendingAmountByQuery(queryForm)
    totalPendingAmount.value = res.data || 0
  } catch (error) {
    console.error('加载待支付金额汇总失败:', error)
    totalPendingAmount.value = 0
  }
}

// 14.6 查询处理
const handleQuery = () => {
  queryForm.pageNum = 1
  fetchData()
}

// 14.6 重置处理
const handleReset = () => {
  Object.assign(queryForm, {
    expenseNameKeyword: '',
    periods: [],
    planTypes: [],
    statuses: [],
    paymentDateStart: '',
    paymentDateEnd: '',
    categoryId: null,
    sortField: 'paymentDate',
    sortOrder: 'asc',
    pageNum: 1,
    pageSize: 20
  })
  paymentDateRange.value = []
  quickDate.value = 'custom'
  selectedMonth.value = ''
  fetchData()
}

// 日期范围变化处理
const handleDateRangeChange = (val) => {
  if (val && val.length === 2) {
    queryForm.paymentDateStart = val[0]
    queryForm.paymentDateEnd = val[1]
  } else {
    queryForm.paymentDateStart = ''
    queryForm.paymentDateEnd = ''
  }
}

// 快捷日期处理
const handleQuickDateChange = (value) => {
  if (value === 'custom') {
    // 自定义模式，不修改日期范围
    return
  }
  
  // 清空月份选择
  selectedMonth.value = ''
  
  const today = new Date()
  const year = today.getFullYear()
  const month = today.getMonth()
  const date = today.getDate()
  const day = today.getDay()
  
  let startDate, endDate
  
  switch (value) {
    case 'today':
      startDate = endDate = new Date(year, month, date)
      break
      
    case 'yesterday':
      startDate = endDate = new Date(year, month, date - 1)
      break
      
    case 'week':
      // 本周（周一到周日）
      const mondayOffset = day === 0 ? -6 : 1 - day
      startDate = new Date(year, month, date + mondayOffset)
      endDate = new Date(year, month, date + mondayOffset + 6)
      break
      
    case 'lastWeek':
      // 上周（周一到周日）
      const lastMondayOffset = day === 0 ? -13 : -6 - day
      startDate = new Date(year, month, date + lastMondayOffset)
      endDate = new Date(year, month, date + lastMondayOffset + 6)
      break
      
    case 'month':
      // 本月
      startDate = new Date(year, month, 1)
      endDate = new Date(year, month + 1, 0)
      break
      
    case 'lastMonth':
      // 上月
      startDate = new Date(year, month - 1, 1)
      endDate = new Date(year, month, 0)
      break
      
    case 'year':
      // 本年
      startDate = new Date(year, 0, 1)
      endDate = new Date(year, 11, 31)
      break
      
    case 'lastYear':
      // 去年
      startDate = new Date(year - 1, 0, 1)
      endDate = new Date(year - 1, 11, 31)
      break
  }
  
  // 格式化日期为 YYYY-MM-DD
  const formatDate = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }
  
  paymentDateRange.value = [formatDate(startDate), formatDate(endDate)]
  queryForm.paymentDateStart = formatDate(startDate)
  queryForm.paymentDateEnd = formatDate(endDate)
}

// 月份选择处理
const handleMonthChange = (value) => {
  if (!value) {
    // 清空月份选择
    return
  }
  
  // 设置为自定义模式
  quickDate.value = 'custom'
  
  // 根据选择的月份设置日期范围
  const [year, month] = value.split('-').map(Number)
  const startDate = new Date(year, month - 1, 1)
  const endDate = new Date(year, month, 0)
  
  const formatDate = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  }
  
  paymentDateRange.value = [formatDate(startDate), formatDate(endDate)]
  queryForm.paymentDateStart = formatDate(startDate)
  queryForm.paymentDateEnd = formatDate(endDate)
}

// 14.6 排序处理
const handleSortChange = ({ prop, order }) => {
  if (!order) {
    queryForm.sortField = 'paymentDate'
    queryForm.sortOrder = 'asc'
  } else {
    queryForm.sortField = prop
    queryForm.sortOrder = order === 'ascending' ? 'asc' : 'desc'
  }
  fetchData()
}

// 14.6 多选处理
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 14.7 新建
const handleCreate = () => {
  editMode.value = 'create'
  currentEditId.value = null
  editDialogVisible.value = true
}

// 14.7 查看详情
const handleView = (id) => {
  currentDetailId.value = id
  detailDialogVisible.value = true
}

// 14.7 编辑（删除、状态标记等操作已收敛到编辑弹窗内）
const handleEdit = (id) => {
  editMode.value = 'edit'
  currentEditId.value = id
  editDialogVisible.value = true
}

// 14.8 批量标记已完成
const handleBatchMarkCompleted = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认要将选中的 ${selectedIds.value.length} 条记录标记为已完成吗？`,
      '确认批量操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await batchMarkAsCompleted(selectedIds.value)
    
    // 显示结果
    batchResult.successCount = res.data.successCount || 0
    batchResult.failCount = res.data.failCount || 0
    batchResult.failureDetails = res.data.failureDetails || []
    
    if (batchResult.failCount === 0) {
      ElMessage.success(`批量标记成功，共处理 ${batchResult.successCount} 条记录`)
    } else {
      batchResultDialogVisible.value = true
    }
    
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 14.8 批量标记已取消
const handleBatchMarkCancelled = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认要将选中的 ${selectedIds.value.length} 条记录标记为已取消吗？`,
      '确认批量操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await batchMarkAsCancelled(selectedIds.value)
    
    // 显示结果
    batchResult.successCount = res.data.successCount || 0
    batchResult.failCount = res.data.failCount || 0
    batchResult.failureDetails = res.data.failureDetails || []
    
    if (batchResult.failCount === 0) {
      ElMessage.success(`批量标记成功，共处理 ${batchResult.successCount} 条记录`)
    } else {
      batchResultDialogVisible.value = true
    }
    
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 14.8 批量删除
const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认要删除选中的 ${selectedIds.value.length} 条记录吗？此操作不可恢复！`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const res = await batchDelete(selectedIds.value)
    
    // 显示结果
    batchResult.successCount = res.data.successCount || 0
    batchResult.failCount = res.data.failCount || 0
    batchResult.failureDetails = res.data.failureDetails || []
    
    if (batchResult.failCount === 0) {
      ElMessage.success(`批量删除成功，共删除 ${batchResult.successCount} 条记录`)
    } else {
      batchResultDialogVisible.value = true
    }
    
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 14.3 批量创建周期性支出
const handleRecurringCreate = () => {
  recurringDialogVisible.value = true
}

// 14.9 导出
const handleExport = async () => {
  try {
    const blob = await exportToExcel(queryForm)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 设置文件名
    const today = new Date()
    const dateStr = today.toISOString().split('T')[0].replace(/-/g, '')
    link.download = `待支出项目_${dateStr}.xlsx`
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 14.10 对话框成功回调
const handleDialogSuccess = () => {
  fetchData()
}

// 状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return typeMap[status] || ''
}

// 周期标签类型
const getPeriodTagType = (period) => {
  const typeMap = {
    MONTHLY: 'primary',
    YEARLY: 'success',
    ONETIME: 'info'
  }
  return typeMap[period] || ''
}

// 计划类型标签类型
const getPlanTypeTagType = (planType) => {
  const typeMap = {
    RIGID: 'danger',
    INTENDED: 'warning'
  }
  return typeMap[planType] || ''
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 19)
}

// 组件挂载时加载数据
onMounted(() => {
  // 默认设置为本年
  quickDate.value = 'year'
  handleQuickDateChange('year')
  
  loadFilterData()
  fetchData()
})
</script>

<style scoped>
.pending-expense-container {
  padding: 20px;
}

.filter-card,
.summary-card,
.operation-card,
.table-card {
  margin-bottom: 20px;
}

.el-card :deep(.el-card__body) {
  padding: 20px;
}

.el-form-item {
  margin-bottom: 18px;
}

.el-pagination {
  display: flex;
}

.el-scrollbar {
  padding: 10px;
  background-color: #F5F7FA;
  border-radius: 4px;
}

/* 待支付金额汇总样式 */
.summary-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.summary-card :deep(.el-card__body) {
  padding: 15px 20px;
}

.summary-item {
  display: flex;
  align-items: center;
  color: #ffffff;
}

.summary-label {
  font-size: 16px;
  font-weight: 500;
  margin-right: 10px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  margin-right: 15px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.summary-tip {
  font-size: 13px;
  opacity: 0.9;
  font-style: italic;
}
</style>
