<template>
  <div class="monthly-expense-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>月度支出统计</span>
          <div class="filter-group">
            <el-date-picker
              v-model="selectedYear"
              type="year"
              placeholder="选择年份"
              format="YYYY年"
              value-format="YYYY"
              @change="loadMonthlyExpense"
            />
            <el-radio-group v-model="amountType" @change="loadMonthlyExpense">
              <el-radio-button label="EXPENSE">支出</el-radio-button>
              <el-radio-button label="INCOME">收入</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="chart-container">
        <div v-if="!hasData" class="no-data">
          <el-empty description="本年暂无数据" />
        </div>
        <div v-else ref="chartRef" class="chart"></div>
      </div>

      <div v-if="hasData" class="summary">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic :title="`年度总${amountType === 'EXPENSE' ? '支出' : '收入'}`" :value="yearlyTotal" :precision="2" suffix="元">
              <template #prefix>
                <el-icon :color="amountType === 'EXPENSE' ? '#F56C6C' : '#67C23A'">
                  <Money />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic :title="`月均${amountType === 'EXPENSE' ? '支出' : '收入'}`" :value="monthlyAverage" :precision="2" suffix="元" />
          </el-col>
          <el-col :span="6">
            <el-statistic :title="`最高月份${amountType === 'EXPENSE' ? '支出' : '收入'}`" :value="maxAmount" :precision="2" suffix="元">
              <template #suffix>
                <span class="month-suffix">{{ maxMonth }}</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic :title="`最低月份${amountType === 'EXPENSE' ? '支出' : '收入'}`" :value="minAmount" :precision="2" suffix="元">
              <template #suffix>
                <span class="month-suffix">{{ minMonth }}</span>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>

      <!-- 月度数据表格 -->
      <div v-if="hasData" class="table-container">
        <el-table :data="monthlyData" stripe>
          <el-table-column prop="month" label="月份" width="100" />
          <el-table-column prop="amount" :label="`${amountType === 'EXPENSE' ? '支出' : '收入'}金额`" width="150" sortable>
            <template #default="{ row }">
              <span :style="{ color: amountType === 'EXPENSE' ? '#F56C6C' : '#67C23A', fontWeight: 'bold' }">
                {{ row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="笔数" width="100" sortable />
          <el-table-column prop="percentage" label="占比" width="150">
            <template #default="{ row }">
              <el-progress :percentage="parseFloat(row.percentage.toFixed(2))" />
            </template>
          </el-table-column>
          <el-table-column label="环比" width="150">
            <template #default="{ row }">
              <span v-if="row.monthOverMonth !== null" :style="{ color: row.monthOverMonth >= 0 ? '#F56C6C' : '#67C23A' }">
                {{ row.monthOverMonth >= 0 ? '+' : '' }}{{ row.monthOverMonth.toFixed(2) }}%
              </span>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="loadBillDetail(row.month)">
                查看明细
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 账单明细 -->
    <el-card v-if="billList.length > 0" class="bill-detail-card">
      <template #header>
        <span>{{ selectedMonth }} 账单明细</span>
      </template>
      <el-table v-loading="billLoading" :data="billList" stripe>
        <el-table-column prop="transactionDate" label="日期" width="120" sortable />
        <el-table-column prop="transactionTime" label="时间" width="100" />
        <el-table-column prop="amountType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.amountType === 'INCOME' ? 'success' : 'danger'" size="small">
              {{ row.amountType === 'INCOME' ? '收入' : '支出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="incomeAmount" label="收入金额" width="120" sortable :sort-method="(a, b) => (a.incomeAmount || 0) - (b.incomeAmount || 0)">
          <template #default="{ row }">
            <span v-if="row.incomeAmount" style="color: #67C23A">
              +{{ row.incomeAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="expenseAmount" label="支出金额" width="120" sortable :sort-method="(a, b) => (a.expenseAmount || 0) - (b.expenseAmount || 0)">
          <template #default="{ row }">
            <span v-if="row.expenseAmount" style="color: #F56C6C">
              -{{ row.expenseAmount }}
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
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <BillEditDialog v-model="showEditDialog" :bill-data="currentBill" @success="handleEditSuccess" />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { pageBills, getStatistics } from '@/api/bill'
import { listTags } from '@/api/tag'
import BillEditDialog from '@/components/BillEditDialog.vue'

const chartRef = ref(null)
const chartInstance = ref(null)
const selectedYear = ref('')
const amountType = ref('EXPENSE')
const loading = ref(false)
const monthlyData = ref([])
const billLoading = ref(false)
const billList = ref([])
const selectedMonth = ref('')
const tagList = ref([])
const showEditDialog = ref(false)
const currentBill = ref(null)

const hasData = computed(() => monthlyData.value.length > 0)

const yearlyTotal = computed(() => {
  if (!hasData.value) return 0
  return monthlyData.value.reduce((sum, item) => sum + item.amount, 0)
})

const monthlyAverage = computed(() => {
  if (!hasData.value) return 0
  return yearlyTotal.value / monthlyData.value.length
})

const maxAmount = computed(() => {
  if (!hasData.value) return 0
  return Math.max(...monthlyData.value.map(item => item.amount))
})

const maxMonth = computed(() => {
  if (!hasData.value) return ''
  const maxItem = monthlyData.value.find(item => item.amount === maxAmount.value)
  return maxItem ? maxItem.month : ''
})

const minAmount = computed(() => {
  if (!hasData.value) return 0
  return Math.min(...monthlyData.value.map(item => item.amount))
})

const minMonth = computed(() => {
  if (!hasData.value) return ''
  const minItem = monthlyData.value.find(item => item.amount === minAmount.value)
  return minItem ? minItem.month : ''
})

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance.value = echarts.init(chartRef.value)
  
  const months = monthlyData.value.map(item => item.month)
  const amounts = monthlyData.value.map(item => item.amount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const data = params[0]
        const item = monthlyData.value[data.dataIndex]
        return `${data.name}<br/>` +
               `金额：${item.amount.toFixed(2)} 元<br/>` +
               `笔数：${item.count} 笔<br/>` +
               `占比：${item.percentage.toFixed(2)}%`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: {
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '金额（元）'
    },
    series: [
      {
        name: amountType.value === 'EXPENSE' ? '支出' : '收入',
        type: 'bar',
        data: amounts,
        itemStyle: {
          color: amountType.value === 'EXPENSE' ? '#F56C6C' : '#67C23A'
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value.toFixed(2)
          }
        }
      },
      {
        name: '趋势线',
        type: 'line',
        data: amounts,
        smooth: true,
        showSymbol: false,
        lineStyle: {
          color: '#409EFF',
          width: 2
        }
      }
    ]
  }
  
  chartInstance.value.setOption(option)
  
  // 添加点击事件
  chartInstance.value.on('click', (params) => {
    if (params.componentType === 'series') {
      const month = monthlyData.value[params.dataIndex].month
      loadBillDetail(month)
    }
  })
}

const loadMonthlyExpense = async () => {
  loading.value = true
  try {
    const year = selectedYear.value ? parseInt(selectedYear.value) : new Date().getFullYear()
    const currentDate = new Date()
    const currentYear = currentDate.getFullYear()
    const currentMonth = currentDate.getMonth() + 1 // 0-11 转为 1-12
    
    // 确定要查询的月份数量
    let maxMonth = 12
    if (year === currentYear) {
      maxMonth = currentMonth // 只查询到当前月份
    } else if (year > currentYear) {
      // 未来年份不展示数据
      monthlyData.value = []
      loading.value = false
      return
    }
    
    // 查询已到达月份的数据
    const promises = []
    for (let month = 1; month <= maxMonth; month++) {
      const startDate = `${year}-${String(month).padStart(2, '0')}-01`
      const lastDay = new Date(year, month, 0).getDate()
      const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`
      
      promises.push(
        getStatistics({
          startDate,
          endDate,
          includeInStatistics: '1'
        })
      )
    }
    
    const results = await Promise.all(promises)
    
    // 处理数据
    const data = []
    let total = 0
    
    results.forEach((res, index) => {
      if (res.code === 200) {
        const amount = amountType.value === 'EXPENSE' 
          ? res.data.totalExpense 
          : res.data.totalIncome
        total += amount
        data.push({
          month: `${index + 1}月`,
          amount: amount,
          count: 0, // 后续可以添加笔数统计
          percentage: 0,
          monthOverMonth: null
        })
      }
    })
    
    // 计算占比和环比
    data.forEach((item, index) => {
      item.percentage = total > 0 ? (item.amount / total) * 100 : 0
      if (index > 0 && data[index - 1].amount > 0) {
        item.monthOverMonth = ((item.amount - data[index - 1].amount) / data[index - 1].amount) * 100
      }
    })
    
    monthlyData.value = data
    
    if (hasData.value) {
      setTimeout(() => {
        initChart()
      }, 100)
    }
  } catch (error) {
    console.error('查询月度支出失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const loadBillDetail = async (month) => {
  selectedMonth.value = month
  billLoading.value = true
  
  try {
    const year = selectedYear.value ? parseInt(selectedYear.value) : new Date().getFullYear()
    const monthNum = parseInt(month)
    const startDate = `${year}-${String(monthNum).padStart(2, '0')}-01`
    const lastDay = new Date(year, monthNum, 0).getDate()
    const endDate = `${year}-${String(monthNum).padStart(2, '0')}-${lastDay}`
    
    const res = await pageBills({
      current: 1,
      size: 1000,
      startDate,
      endDate,
      amountType: amountType.value,
      includeInStatistics: '1'
    })
    
    if (res.code === 200) {
      billList.value = res.data.records || []
    } else {
      ElMessage.error(res.message || '查询账单明细失败')
    }
  } catch (error) {
    console.error('查询账单明细失败:', error)
    ElMessage.error('查询账单明细失败')
  } finally {
    billLoading.value = false
  }
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

const handleEdit = (row) => {
  currentBill.value = row
  showEditDialog.value = true
}

const handleEditSuccess = () => {
  loadBillDetail(selectedMonth.value)
}

const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

onMounted(() => {
  selectedYear.value = String(new Date().getFullYear())
  loadMonthlyExpense()
  loadTags()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.monthly-expense-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.chart-container {
  min-height: 400px;
  position: relative;
}

.chart {
  width: 100%;
  height: 400px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.summary {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.month-suffix {
  font-size: 12px;
  color: #909399;
  margin-left: 5px;
}

.table-container {
  margin-top: 30px;
}

.bill-detail-card {
  margin-top: 20px;
}
</style>
