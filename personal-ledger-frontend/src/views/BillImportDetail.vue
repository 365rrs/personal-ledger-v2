<template>
  <div class="bill-import-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>导入明细 - {{ recordInfo.fileName }}</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <!-- 统计信息 -->
      <el-descriptions :column="4" border class="statistics">
        <el-descriptions-item label="总数">{{ statistics.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="成功">{{ statistics.successCount }}</el-descriptions-item>
        <el-descriptions-item label="失败">{{ statistics.failCount }}</el-descriptions-item>
        <el-descriptions-item label="唯一">{{ statistics.uniqueCount }}</el-descriptions-item>
        <el-descriptions-item label="重复">{{ statistics.duplicateCount }}</el-descriptions-item>
        <el-descriptions-item label="待转换">{{ statistics.pendingCount }}</el-descriptions-item>
        <el-descriptions-item label="已转换">{{ statistics.convertedCount }}</el-descriptions-item>
      </el-descriptions>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="导入状态">
          <el-select v-model="queryForm.importStatus" placeholder="全部" clearable>
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="重复状态">
          <el-select v-model="queryForm.duplicateStatus" placeholder="全部" clearable>
            <el-option label="唯一" value="UNIQUE" />
            <el-option label="重复" value="DUPLICATE" />
          </el-select>
        </el-form-item>
        <el-form-item label="转换状态">
          <el-select v-model="queryForm.convertStatus" placeholder="全部" clearable>
            <el-option label="待转换" value="PENDING" />
            <el-option label="已转换" value="CONVERTED" />
            <el-option label="已跳过" value="SKIPPED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button type="primary" :disabled="selectedIds.length === 0" @click="handleConvert">
          批量转换
        </el-button>
        <el-button :disabled="selectedIds.length === 0" @click="handleSkip">
          批量跳过
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="amountType" label="金额类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.amountType === 'INCOME'" type="success">收入</el-tag>
            <el-tag v-else-if="row.amountType === 'EXPENSE'" type="warning">支出</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="transactionType" label="交易类型" width="120" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="transactionTime" label="交易时间" width="180" />
        <el-table-column prop="importStatus" label="导入状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.importStatus === 'SUCCESS'" type="success">成功</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duplicateStatus" label="重复状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.duplicateStatus === 'UNIQUE'" type="primary">唯一</el-tag>
            <el-tag v-else-if="row.duplicateStatus === 'DUPLICATE'" type="warning">重复</el-tag>
            <el-tag v-else type="info">未检查</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="convertStatus" label="转换状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.convertStatus === 'PENDING'" type="info">待转换</el-tag>
            <el-tag v-else-if="row.convertStatus === 'CONVERTED'" type="success">已转换</el-tag>
            <el-tag v-else-if="row.convertStatus === 'SKIPPED'" type="warning">已跳过</el-tag>
            <el-tag v-else-if="row.convertStatus === 'DUPLICATE'" type="warning">重复跳过</el-tag>
            <el-tag v-else-if="row.convertStatus === 'CONVERT_FAILED'" type="danger">转换失败</el-tag>
            <el-tag v-else type="info">{{ row.convertStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="150" show-overflow-tooltip />
        <el-table-column prop="convertErrorMessage" label="转换错误" min-width="150" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRecord, pageDetails, getStatistics, convertToBill, skipRecords } from '@/api/billImport'

const route = useRoute()
const router = useRouter()

const importRecordId = ref(route.params.id)

// 记录信息
const recordInfo = ref({})

// 统计信息
const statistics = ref({
  totalCount: 0,
  successCount: 0,
  failCount: 0,
  uniqueCount: 0,
  duplicateCount: 0,
  pendingCount: 0,
  convertedCount: 0
})

// 查询表单
const queryForm = reactive({
  importRecordId: importRecordId.value,
  current: 1,
  size: 20,
  importStatus: '',
  duplicateStatus: '',
  convertStatus: ''
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])

// 加载记录信息
const loadRecordInfo = async () => {
  try {
    const res = await getRecord(importRecordId.value)
    recordInfo.value = res.data
  } catch (error) {
    ElMessage.error('加载记录信息失败')
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const res = await getStatistics(importRecordId.value)
    statistics.value = res.data
  } catch (error) {
    ElMessage.error('加载统计信息失败')
  }
}

// 加载明细数据
const loadData = async () => {
  try {
    const res = await pageDetails(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

// 查询
const handleQuery = () => {
  queryForm.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.importStatus = ''
  queryForm.duplicateStatus = ''
  queryForm.convertStatus = ''
  handleQuery()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 批量转换
const handleConvert = async () => {
  try {
    await ElMessageBox.confirm('确定转换选中的记录为账单吗？', '提示', {
      type: 'warning'
    })
    
    await convertToBill({
      importRecordId: importRecordId.value,
      detailIds: selectedIds.value
    })
    
    ElMessage.success('转换成功')
    loadData()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '转换失败')
    }
  }
}

// 批量跳过
const handleSkip = async () => {
  try {
    await ElMessageBox.confirm('确定跳过选中的记录吗？', '提示', {
      type: 'warning'
    })
    
    await skipRecords({
      detailIds: selectedIds.value
    })
    
    ElMessage.success('操作成功')
    loadData()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadRecordInfo()
  loadStatistics()
  loadData()
})
</script>

<style scoped>
.bill-import-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics {
  margin-bottom: 20px;
}

.query-form {
  margin-bottom: 20px;
}

.action-bar {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
