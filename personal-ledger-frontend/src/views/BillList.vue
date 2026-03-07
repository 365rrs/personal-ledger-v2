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
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="交易类型">
          <el-select v-model="queryForm.transactionType" placeholder="全部" clearable>
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 统计信息 -->
      <el-descriptions :column="3" border class="statistics">
        <el-descriptions-item label="收入">
          <span style="color: #67c23a; font-weight: bold;">¥{{ statistics.totalIncome || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支出">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ statistics.totalExpense || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="结余">
          <span style="color: #409eff; font-weight: bold;">¥{{ statistics.balance || 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>

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
  startDate: '',
  endDate: ''
})

const dateRange = ref([])

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
  queryForm.startDate = ''
  queryForm.endDate = ''
  dateRange.value = []
  handleQuery()
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
}

.statistics {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
