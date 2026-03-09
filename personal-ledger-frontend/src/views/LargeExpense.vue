<template>
  <div class="large-expense-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>大额支出列表</span>
        </div>
      </template>

      <div class="filter-section">
        <el-form :inline="true">
          <el-form-item label="金额阈值">
            <el-input-number 
              v-model="minAmount" 
              :min="0" 
              :step="100"
              placeholder="最小金额"
            />
          </el-form-item>
          <el-form-item label="时间范围">
            <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
              <el-radio-button label="week">本周</el-radio-button>
              <el-radio-button label="month">本月</el-radio-button>
              <el-radio-button label="year">本年</el-radio-button>
              <el-radio-button label="custom">自定义</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="timeRange === 'custom'" label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="收支类型">
            <el-radio-group v-model="amountType">
              <el-radio-button label="EXPENSE">支出</el-radio-button>
              <el-radio-button label="INCOME">收入</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadList">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-loading="loading" :data="billList" stripe>
        <el-table-column prop="transactionDate" label="日期" width="120" sortable />
        <el-table-column prop="transactionTime" label="时间" width="100" />
        <el-table-column label="金额" width="150" sortable :sort-method="sortByAmount">
          <template #default="{ row }">
            <span :style="{ color: row.amountType === 'EXPENSE' ? '#F56C6C' : '#67C23A', fontWeight: 'bold', fontSize: '16px' }">
              {{ row.amountType === 'EXPENSE' ? '-' : '+' }}{{ getAmount(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="paymentChannel" label="支付渠道" width="120" />
        <el-table-column prop="transactionDesc" label="交易描述" show-overflow-tooltip />
        <el-table-column prop="manualRemark" label="备注" show-overflow-tooltip />
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <el-tag v-for="tagId in row.tagIds" :key="tagId" size="small" style="margin-right: 5px">
              {{ getTagName(tagId) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="summary">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="大额支出总额" :value="totalAmount" :precision="2" suffix="元">
              <template #prefix>
                <el-icon color="#F56C6C">
                  <Money />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="大额支出笔数" :value="billList.length" suffix="笔">
              <template #prefix>
                <el-icon color="#409EFF">
                  <Document />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="占总支出比例" :value="percentage" :precision="2" suffix="%">
              <template #prefix>
                <el-icon color="#67C23A">
                  <PieChart />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { pageBills, getStatistics } from '@/api/bill'
import { listTags } from '@/api/tag'

const minAmount = ref(500)
const timeRange = ref('month')
const dateRange = ref([])
const amountType = ref('EXPENSE')
const loading = ref(false)
const billList = ref([])
const tagList = ref([])
const totalStatistics = ref({ totalExpense: 0, totalIncome: 0 })

const totalAmount = computed(() => {
  return billList.value.reduce((sum, item) => {
    return sum + parseFloat(getAmount(item))
  }, 0)
})

const percentage = computed(() => {
  if (amountType.value === 'EXPENSE' && totalStatistics.value.totalExpense > 0) {
    return (totalAmount.value / totalStatistics.value.totalExpense) * 100
  } else if (amountType.value === 'INCOME' && totalStatistics.value.totalIncome > 0) {
    return (totalAmount.value / totalStatistics.value.totalIncome) * 100
  }
  return 0
})

const getAmount = (row) => {
  return row.amountType === 'EXPENSE' ? row.expenseAmount : row.incomeAmount
}

const sortByAmount = (a, b) => {
  return parseFloat(getAmount(a)) - parseFloat(getAmount(b))
}

const getDateRange = () => {
  const now = new Date()
  let startDate, endDate
  
  if (timeRange.value === 'week') {
    const day = now.getDay()
    const diff = day === 0 ? 6 : day - 1
    startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() - diff)
    endDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() + (6 - diff))
  } else if (timeRange.value === 'month') {
    startDate = new Date(now.getFullYear(), now.getMonth(), 1)
    endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  } else if (timeRange.value === 'year') {
    startDate = new Date(now.getFullYear(), 0, 1)
    endDate = new Date(now.getFullYear(), 11, 31)
  } else if (timeRange.value === 'custom' && dateRange.value && dateRange.value.length === 2) {
    return { startDate: dateRange.value[0], endDate: dateRange.value[1] }
  }
  
  return {
    startDate: startDate.toISOString().split('T')[0],
    endDate: endDate.toISOString().split('T')[0]
  }
}

const loadList = async () => {
  loading.value = true
  try {
    const { startDate, endDate } = getDateRange()
    
    const res = await pageBills({
      current: 1,
      size: 1000,
      startDate,
      endDate,
      minAmount: minAmount.value,
      amountType: amountType.value,
      includeInStatistics: '1'
    })
    
    if (res.code === 200) {
      billList.value = res.data.records || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
    
    // 查询总统计
    const statsRes = await getStatistics({
      startDate,
      endDate,
      includeInStatistics: '1'
    })
    
    if (statsRes.code === 200) {
      totalStatistics.value = statsRes.data
    }
  } catch (error) {
    console.error('查询大额支出失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleTimeRangeChange = () => {
  if (timeRange.value !== 'custom') {
    loadList()
  }
}

const resetFilter = () => {
  minAmount.value = 500
  timeRange.value = 'month'
  dateRange.value = []
  amountType.value = 'EXPENSE'
  loadList()
}

const loadTags = async () => {
  try {
    const res = await listTags()
    if (res.code === 200) {
      tagList.value = res.data || []
    }
  } catch (error) {
    console.error('查询标签失败:', error)
  }
}

const getTagName = (tagId) => {
  const tag = tagList.value.find(t => t.id === tagId)
  return tag ? tag.tagName : ''
}

onMounted(() => {
  loadList()
  loadTags()
})
</script>

<style scoped>
.large-expense-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.summary {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
