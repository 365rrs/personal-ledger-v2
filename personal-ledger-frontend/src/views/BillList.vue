<template>
  <div class="bill-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账单列表</span>
          <el-button type="primary" @click="handleCreate">+ 记一笔</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryForm" class="query-form">
        <!-- 第一行：快捷日期 -->
        <div style="margin-bottom: 12px;">
          <span style="margin-right: 12px;">快捷日期：</span>
          <el-radio-group v-model="quickDate" @change="handleQuickDateChange">
            <el-radio-button label="今天" value="today" />
            <el-radio-button label="昨天" value="yesterday" />
            <el-radio-button label="本周" value="week" />
            <el-radio-button label="上周" value="lastWeek" />
            <el-radio-button label="本月" value="month" />
            <el-radio-button label="上月" value="lastMonth" />
            <el-radio-button label="本年" value="year" />
            <el-radio-button label="去年" value="lastYear" />
          </el-radio-group>
        </div>
        
        <!-- 第二行：日期范围 -->
        <div style="margin-bottom: 12px;">
          <span style="margin-right: 12px;">日期范围：</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </div>
        
        <!-- 第三行：收支类型 + 是否计入统计 + 分类 + 二级分类 -->
        <div style="margin-bottom: 12px;">
          <span style="margin-right: 12px;">收支类型：</span>
          <el-select v-model="queryForm.amountType" placeholder="全部" clearable style="width: 120px; margin-right: 12px;">
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
          </el-select>
          
          <span style="margin-right: 12px;">是否计入统计：</span>
          <el-select v-model="queryForm.includeInStatistics" placeholder="全部" clearable style="width: 100px; margin-right: 12px;">
            <el-option label="是" value="1" />
            <el-option label="否" value="0" />
          </el-select>
          
          <span style="margin-right: 12px;">分类：</span>
          <el-input v-model="queryForm.category" placeholder="请输入分类" clearable style="width: 150px; margin-right: 12px;" />
          
          <span style="margin-right: 12px;">二级分类：</span>
          <el-input v-model="queryForm.subCategory" placeholder="请输入二级分类" clearable style="width: 150px;" />
        </div>
        
        <!-- 折叠区域：更多查询条件 -->
        <el-collapse-transition>
          <div v-show="showAdvancedSearch">
            <!-- 第四行：支付渠道 + 交易类型 + 标签 -->
            <div style="margin-bottom: 12px;">
              <span style="margin-right: 12px;">支付渠道：</span>
              <el-input v-model="queryForm.paymentChannel" placeholder="请输入支付渠道" clearable style="width: 150px; margin-right: 12px;" />
              
              <span style="margin-right: 12px;">交易类型：</span>
              <el-input v-model="queryForm.transactionType" placeholder="请输入交易类型" clearable style="width: 150px; margin-right: 12px;" />
              
              <span style="margin-right: 12px;">标签：</span>
              <el-input v-model="queryForm.tags" placeholder="请输入标签" clearable style="width: 150px;" />
            </div>
            
            <!-- 第五行：关键词搜索 + 金额范围 -->
            <div style="margin-bottom: 12px;">
              <span style="margin-right: 12px;">关键词：</span>
              <el-input 
                v-model="queryForm.keywords" 
                placeholder="搜索交易描述或用户备注" 
                clearable 
                style="width: 300px; margin-right: 12px;"
              />
              
              <span style="margin-right: 12px;">金额范围：</span>
              <el-input-number v-model="queryForm.minAmount" placeholder="最小金额" :min="0" :precision="2" style="width: 120px; margin-right: 8px;" />
              <span style="margin: 0 8px;">至</span>
              <el-input-number v-model="queryForm.maxAmount" placeholder="最大金额" :min="0" :precision="2" style="width: 120px;" />
            </div>
          </div>
        </el-collapse-transition>
        
        <!-- 第六行：展开/收起按钮 -->
        <div style="margin-bottom: 12px;">
          <el-button link type="primary" @click="toggleAdvancedSearch">
            {{ showAdvancedSearch ? '收起高级搜索' : '展开高级搜索' }}
          </el-button>
        </div>
        
        <!-- 第七行：查询和重置按钮 -->
        <div>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>

      <!-- 统计信息 -->
      <div class="statistics-cards">
        <div class="stat-card stat-card-balance">
          <div class="stat-label">结余</div>
          <div class="stat-value">{{ statistics.balance || 0 }}</div>
        </div>
        <div class="stat-operator">=</div>
        <div class="stat-card stat-card-income">
          <div class="stat-label">收入</div>
          <div class="stat-value">{{ statistics.totalIncome || 0 }}</div>
        </div>
        <div class="stat-operator">-</div>
        <div class="stat-card stat-card-expense">
          <div class="stat-label">支出</div>
          <div class="stat-value">{{ statistics.totalExpense || 0 }}</div>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="transactionDate" label="交易日期" width="120" />
        <el-table-column prop="transactionTime" label="交易时间" width="100" />
        <el-table-column label="收入" width="80" align="right">
          <template #default="{ row }">
            <span v-if="row.amountType === 'INCOME'" style="color: #67c23a;">
              {{ row.incomeAmount }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="支出" width="80" align="right">
          <template #default="{ row }">
            <span v-if="row.amountType === 'EXPENSE'" style="color: #f56c6c;">
              {{ row.expenseAmount }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="transactionType" label="交易类型" width="120" show-overflow-tooltip />
        <el-table-column prop="paymentChannel" label="支付渠道" width="120" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" show-overflow-tooltip />
        <el-table-column prop="subCategory" label="二级分类" width="120" show-overflow-tooltip />
        <el-table-column prop="transactionDesc" label="交易描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="manualRemark" label="用户备注" min-width="120" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" width="120" show-overflow-tooltip />
        <el-table-column label="计入统计" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.includeInStatistics === '1'" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手工记账" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.manualEntry === '1'" type="primary">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="金额类型" prop="amountType">
          <el-radio-group v-model="form.amountType" :disabled="isViewMode">
            <el-radio label="INCOME">收入</el-radio>
            <el-radio label="EXPENSE">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="1" :disabled="isViewMode" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="请输入分类" :disabled="isViewMode" />
        </el-form-item>
        <el-form-item label="交易日期" prop="transactionDate">
          <el-date-picker
            v-model="form.transactionDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            :disabled="isViewMode"
          />
        </el-form-item>
        <el-form-item label="交易时间">
          <el-time-picker
            v-model="form.transactionTime"
            placeholder="选择时间"
            value-format="HH:mm:ss"
            :disabled="isViewMode"
          />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-input v-model="form.paymentChannel" placeholder="请输入支付渠道" :disabled="isViewMode" />
        </el-form-item>
        <el-form-item label="交易描述">
          <el-input v-model="form.transactionDesc" type="textarea" :rows="3" :disabled="isViewMode" />
        </el-form-item>
        <el-form-item label="手工备注">
          <el-input v-model="form.manualRemark" type="textarea" :rows="2" :disabled="isViewMode" />
        </el-form-item>
        <el-form-item label="计入统计">
          <el-switch v-model="form.includeInStatistics" active-value="1" inactive-value="0" :disabled="isViewMode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isViewMode" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageBills, getStatistics, createBill, updateBill, deleteBill } from '@/api/bill'

// 查询表单
const queryForm = reactive({
  current: 1,
  size: 20,
  transactionType: '',
  category: '',
  subCategory: '',
  paymentChannel: '',
  amountType: '',
  includeInStatistics: '',
  tags: '',
  keywords: '',
  minAmount: null,
  maxAmount: null,
  startDate: '',
  endDate: ''
})

const dateRange = ref([])
const quickDate = ref('month') // 快捷日期选择，默认为本月
const showAdvancedSearch = ref(false) // 是否显示高级搜索

// 表格数据
const tableData = ref([])
const total = ref(0)

// 统计数据
const statistics = ref({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('记一笔')
const formRef = ref()
const isViewMode = ref(false) // 是否为查看模式
const form = reactive({
  id: null,
  amountType: 'EXPENSE',
  transactionType: '',
  amount: null,
  category: '',
  transactionDate: '',
  transactionTime: '',
  paymentChannel: '',
  transactionDesc: '',
  manualRemark: '',
  includeInStatistics: '1'
})

const rules = {
  amountType: [{ required: true, message: '请选择金额类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  transactionDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  try {
    const res = await pageBills(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

// 加载统计
const loadStatistics = async () => {
  try {
    const res = await getStatistics(queryForm)
    statistics.value = res.data
  } catch (error) {
    ElMessage.error('加载统计失败')
  }
}

// 日期范围变化
const handleDateChange = (val) => {
  if (val) {
    queryForm.startDate = val[0]
    queryForm.endDate = val[1]
  } else {
    queryForm.startDate = ''
    queryForm.endDate = ''
  }
}

// 快捷日期选择
const handleQuickDateChange = (value) => {
  if (!value) {
    // 清空快捷日期时，同时清空日期范围
    queryForm.startDate = ''
    queryForm.endDate = ''
    dateRange.value = []
    return
  }
  
  const today = new Date()
  let startDate = ''
  let endDate = ''
  
  switch (value) {
    case 'today':
      startDate = formatDate(today)
      endDate = formatDate(today)
      break
    case 'yesterday':
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
      startDate = formatDate(yesterday)
      endDate = formatDate(yesterday)
      break
    case 'week':
      // 本周一
      const currentDay = today.getDay() || 7 // 周日为 0，转为 7
      const mondayOffset = (currentDay - 1) * 24 * 60 * 60 * 1000
      const monday = new Date(today.getTime() - mondayOffset)
      startDate = formatDate(monday)
      endDate = formatDate(today)
      break
    case 'lastWeek':
      // 上周一到上周日
      const currentDay2 = today.getDay() || 7
      const lastMondayOffset = (currentDay2 + 6) * 24 * 60 * 60 * 1000
      const lastSundayOffset = (currentDay2 - 1) * 24 * 60 * 60 * 1000
      const lastMonday = new Date(today.getTime() - lastMondayOffset)
      const lastSunday = new Date(today.getTime() - lastSundayOffset)
      startDate = formatDate(lastMonday)
      endDate = formatDate(lastSunday)
      break
    case 'month':
      // 本月 1 号到今天
      const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
      startDate = formatDate(firstDayOfMonth)
      endDate = formatDate(today)
      break
    case 'lastMonth':
      // 上月 1 号到上月最后一天
      const firstDayOfLastMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1)
      const lastDayOfLastMonth = new Date(today.getFullYear(), today.getMonth(), 0)
      startDate = formatDate(firstDayOfLastMonth)
      endDate = formatDate(lastDayOfLastMonth)
      break
    case 'year':
      // 本年 1 月 1 号到今天
      const firstDayOfYear = new Date(today.getFullYear(), 0, 1)
      startDate = formatDate(firstDayOfYear)
      endDate = formatDate(today)
      break
    case 'lastYear':
      // 去年 1 月 1 号到去年 12 月 31 号
      const firstDayOfLastYear = new Date(today.getFullYear() - 1, 0, 1)
      const lastDayOfLastYear = new Date(today.getFullYear(), 0, 0)
      startDate = formatDate(firstDayOfLastYear)
      endDate = formatDate(lastDayOfLastYear)
      break
  }
  
  queryForm.startDate = startDate
  queryForm.endDate = endDate
  dateRange.value = [startDate, endDate]
}

// 格式化日期 YYYY-MM-DD
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 分页变化处理
const handleSizeChange = (size) => {
  queryForm.size = size
  queryForm.current = 1
  loadData()
  loadStatistics()
}

const handleCurrentChange = (current) => {
  queryForm.current = current
  loadData()
  loadStatistics()
}

// 查询
const handleQuery = () => {
  queryForm.current = 1
  loadData()
  loadStatistics()
}

// 重置
const handleReset = () => {
  queryForm.transactionType = ''
  queryForm.category = ''
  queryForm.subCategory = ''
  queryForm.paymentChannel = ''
  queryForm.amountType = ''
  queryForm.includeInStatistics = ''
  queryForm.tags = ''
  queryForm.keywords = ''
  queryForm.minAmount = null
  queryForm.maxAmount = null
  queryForm.startDate = ''
  queryForm.endDate = ''
  dateRange.value = []
  quickDate.value = 'month' // 重置时恢复默认值为本月
  handleQuickDateChange('month') // 同时更新查询条件
  showAdvancedSearch.value = false // 收起高级搜索
  handleQuery()
}

// 切换高级搜索显示/隐藏
const toggleAdvancedSearch = () => {
  showAdvancedSearch.value = !showAdvancedSearch.value
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '记一笔'
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    amountType: 'EXPENSE',
    transactionType: '',
    amount: null,
    category: '',
    transactionDate: new Date().toISOString().split('T')[0],
    transactionTime: '',
    paymentChannel: '',
    transactionDesc: '',
    manualRemark: '',
    includeInStatistics: '1'
  })
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑账单'
  dialogVisible.value = true
  Object.assign(form, {
    id: row.id,
    amountType: row.amountType,
    transactionType: row.transactionType,
    amount: row.amountType === 'INCOME' ? row.incomeAmount : row.expenseAmount,
    category: row.category,
    transactionDate: row.transactionDate,
    transactionTime: row.transactionTime,
    paymentChannel: row.paymentChannel,
    transactionDesc: row.transactionDesc,
    manualRemark: row.manualRemark,
    includeInStatistics: row.includeInStatistics
  })
}

// 查看
const handleView = (row) => {
  dialogTitle.value = '账单详情'
  isViewMode.value = true // 设置为查看模式
  dialogVisible.value = true
  Object.assign(form, {
    id: row.id,
    amountType: row.amountType,
    transactionType: row.transactionType,
    amount: row.amountType === 'INCOME' ? row.incomeAmount : row.expenseAmount,
    category: row.category,
    subCategory: row.subCategory,
    transactionDate: row.transactionDate,
    transactionTime: row.transactionTime,
    paymentChannel: row.paymentChannel,
    transactionDesc: row.transactionDesc,
    manualRemark: row.manualRemark,
    tags: row.tags,
    includeInStatistics: row.includeInStatistics,
    manualEntry: row.manualEntry
  })
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该账单吗？', '提示', { type: 'warning' })
    await deleteBill(row.id)
    ElMessage.success('删除成功')
    loadData()
    loadStatistics()
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
    
    const data = {
      id: form.id,
      amountType: form.amountType,
      transactionType: form.transactionType,
      transactionDate: form.transactionDate,
      transactionTime: form.transactionTime,
      category: form.category,
      paymentChannel: form.paymentChannel,
      transactionDesc: form.transactionDesc,
      manualRemark: form.manualRemark,
      includeInStatistics: form.includeInStatistics
    }
    
    if (form.amountType === 'INCOME') {
      data.incomeAmount = form.amount
    } else {
      data.expenseAmount = form.amount
    }
    
    if (form.id) {
      await updateBill(data)
      ElMessage.success('更新成功')
    } else {
      await createBill(data)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  formRef.value?.resetFields()
  isViewMode.value = false // 重置查看模式
}

onMounted(() => {
  // 初始化快捷日期为本月
  if (quickDate.value === 'month') {
    handleQuickDateChange('month')
  }
  loadData()
  loadStatistics()
})
</script>

<style scoped>
.bill-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
  font-size: 14px; /* 查询表单字体 */
}

/* 查询表单中的标签文字 */
.query-form span {
  font-size: 14px;
  color: #606266;
}

/* 查询表单中的输入框和选择器 */
.query-form .el-input,
.query-form .el-select,
.query-form .el-radio-group {
  font-size: 14px;
}

/* 日期选择器字体 */
.query-form .el-date-editor {
  font-size: 14px;
}

/* 数字输入框字体 */
.query-form .el-input-number {
  font-size: 14px;
}

.statistics-cards {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.stat-card {
  flex: 0 0 auto;
  width: 240px;
  height: 90px;
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-card-balance {
  /* 移除左侧彩色边框 */
}

.stat-card-income {
  /* 移除左侧彩色边框 */
}

.stat-card-expense {
  /* 移除左侧彩色边框 */
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: 500;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.stat-card-balance .stat-value {
  color: #409eff;
}

.stat-card-income .stat-value {
  color: #67c23a;
}

.stat-card-expense .stat-value {
  color: #f56c6c;
}

.stat-operator {
  font-size: 20px;
  font-weight: bold;
  color: #909399;
  margin: 0 4px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
