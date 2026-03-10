<template>
  <div class="cumulative-expense-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>累计支出统计</span>
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            @change="loadCumulativeExpense"
          />
        </div>
      </template>

      <div v-loading="loading" class="chart-container">
        <div v-if="!hasData" class="no-data">
          <el-empty description="本月暂无账单数据" />
        </div>
        <div v-else ref="chartRef" class="chart"></div>
      </div>

      <div v-if="hasData" class="summary">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="月度累计支出" :value="monthlyTotal" :precision="2" suffix="元">
              <template #prefix>
                <el-icon color="#F56C6C">
                  <TrendCharts />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="平均每日累计" :value="dailyAverage" :precision="2" suffix="元" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="最大累计支出" :value="maxExpense" :precision="2" suffix="元">
              <template #suffix>
                <span class="date-suffix">{{ maxExpenseDate }}</span>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 账单明细 -->
    <el-card v-if="billList.length > 0" class="bill-detail-card">
      <template #header>
        <span>{{ selectedDate }} 账单明细</span>
      </template>
      <el-table v-loading="billLoading" :data="billList" stripe>
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
import { getCumulativeExpense, pageBills } from '@/api/bill'
import { listTags } from '@/api/tag'
import BillEditDialog from '@/components/BillEditDialog.vue'

const chartRef = ref(null)
const chartInstance = ref(null)
const selectedMonth = ref('')
const loading = ref(false)
const expenseData = ref([])
const billLoading = ref(false)
const billList = ref([])
const selectedDate = ref('')
const tagList = ref([])
const showEditDialog = ref(false)
const currentBill = ref(null)

const hasData = computed(() => expenseData.value.length > 0)

const monthlyTotal = computed(() => {
  if (!hasData.value) return 0
  return expenseData.value[expenseData.value.length - 1].cumulativeExpense
})

const dailyAverage = computed(() => {
  if (!hasData.value) return 0
  return monthlyTotal.value / expenseData.value.length
})

const maxExpense = computed(() => {
  if (!hasData.value) return 0
  return Math.max(...expenseData.value.map(item => item.cumulativeExpense))
})

const maxExpenseDate = computed(() => {
  if (!hasData.value) return ''
  const maxItem = expenseData.value.find(item => item.cumulativeExpense === maxExpense.value)
  return maxItem ? maxItem.date.substring(5) : ''
})

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance.value = echarts.init(chartRef.value)
  
  const dates = expenseData.value.map(item => item.date.substring(5))
  const cumulativeExpenses = expenseData.value.map(item => item.cumulativeExpense)
  const dailyExpenses = expenseData.value.map(item => item.dailyExpense)
  const fullDates = expenseData.value.map(item => item.date)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const date = params[0].name
        const cumulative = params[0].value
        const daily = params[1].value
        const percentage = cumulative > 0 ? ((daily / cumulative) * 100).toFixed(1) : 0
        return `${date}<br/>` +
               `<span style="color:#F56C6C">累计支出：${cumulative.toFixed(2)} 元</span><br/>` +
               `<span style="color:#409EFF">当日支出：${daily.toFixed(2)} 元</span><br/>` +
               `<span style="color:#67C23A">当日占比：${percentage}%</span>`
      }
    },
    legend: {
      data: ['当日支出', '之前累计', '累计趋势']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        interval: 0,
        rotate: 0
      },
      triggerEvent: true
    },
    yAxis: {
      type: 'value',
      name: '金额（元）',
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        name: '当日支出',
        type: 'bar',
        stack: 'total',
        data: dailyExpenses,
        itemStyle: {
          color: '#409EFF'
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => {
            return params.value.toFixed(2)
          },
          color: '#fff'
        }
      },
      {
        name: '之前累计',
        type: 'bar',
        stack: 'total',
        data: expenseData.value.map((item, index) => {
          return item.cumulativeExpense - item.dailyExpense
        }),
        itemStyle: {
          color: '#F56C6C'
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            const total = cumulativeExpenses[params.dataIndex]
            return total.toFixed(2)
          },
          color: '#F56C6C'
        }
      },
      {
        name: '累计趋势',
        type: 'line',
        data: cumulativeExpenses,
        smooth: true,
        showSymbol: false,
        lineStyle: {
          color: '#67C23A',
          width: 2
        }
      }
    ]
  }
  
  chartInstance.value.setOption(option)
  
  // 添加点击事件
  chartInstance.value.on('click', (params) => {
    let clickedDate = null
    if (params.componentType === 'series') {
      clickedDate = fullDates[params.dataIndex]
    } else if (params.componentType === 'xAxis') {
      const index = typeof params.value === 'number' ? params.value : dates.indexOf(params.value)
      clickedDate = fullDates[index]
    }
    if (clickedDate) {
      loadBillDetail(clickedDate)
    }
  })
}

const loadCumulativeExpense = async () => {
  loading.value = true
  try {
    const [year, month] = selectedMonth.value ? selectedMonth.value.split('-') : [null, null]
    const res = await getCumulativeExpense({
      year: year ? parseInt(year) : undefined,
      month: month ? parseInt(month) : undefined
    })
    
    if (res.code === 200) {
      expenseData.value = res.data || []
      if (hasData.value) {
        setTimeout(() => {
          initChart()
        }, 100)
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询累计支出失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const loadBillDetail = async (date) => {
  selectedDate.value = date
  billLoading.value = true
  
  try {
    const res = await pageBills({
      current: 1,
      size: 1000,
      startDate: date,
      endDate: date,
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
  loadBillDetail(selectedDate.value)
}

const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

onMounted(() => {
  const now = new Date()
  selectedMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  loadCumulativeExpense()
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
.cumulative-expense-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
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

.date-suffix {
  font-size: 12px;
  color: #909399;
  margin-left: 5px;
}

.bill-detail-card {
  margin-top: 20px;
}
</style>
