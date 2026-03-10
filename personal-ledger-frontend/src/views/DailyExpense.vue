<template>
  <div class="daily-balance-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>每日支出统计</span>
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            @change="loadDailyExpense"
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
            <el-statistic title="月度总支出" :value="monthlyTotal" :precision="2" suffix="元">
              <template #prefix>
                <el-icon :color="monthlyTotal >= 0 ? '#F56C6C' : '#67C23A'">
                  <component :is="monthlyTotal >= 0 ? 'Bottom' : 'TrendCharts'" />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="平均每日支出" :value="dailyAverage" :precision="2" suffix="元" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="最大单日支出" :value="maxBalance" :precision="2" suffix="元">
              <template #suffix>
                <span class="date-suffix">{{ maxBalanceDate }}</span>
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
import { getDailyExpense, pageBills } from '@/api/bill'
import { listTags } from '@/api/tag'
import BillEditDialog from '@/components/BillEditDialog.vue'

const chartRef = ref(null)
const chartInstance = ref(null)
const selectedMonth = ref('')
const loading = ref(false)
const balanceData = ref([])
const billLoading = ref(false)
const billList = ref([])
const selectedDate = ref('')
const tagList = ref([])
const showEditDialog = ref(false)
const currentBill = ref(null)

const hasData = computed(() => balanceData.value.length > 0)

const monthlyTotal = computed(() => {
  if (!hasData.value) return 0
  return balanceData.value.reduce((sum, item) => sum + item.balance, 0)
})

const dailyAverage = computed(() => {
  if (!hasData.value) return 0
  return monthlyTotal.value / balanceData.value.length
})

const maxBalance = computed(() => {
  if (!hasData.value) return 0
  return Math.max(...balanceData.value.map(item => item.balance))
})

const maxBalanceDate = computed(() => {
  if (!hasData.value) return ''
  const maxItem = balanceData.value.find(item => item.balance === maxBalance.value)
  return maxItem ? maxItem.date.substring(5) : ''
})

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance.value = echarts.init(chartRef.value)
  
  const dates = balanceData.value.map(item => item.date.substring(5))
  const balances = balanceData.value.map(item => item.balance)
  const fullDates = balanceData.value.map(item => item.date)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const data = params[0]
        const value = data.value
        const color = value >= 0 ? '#F56C6C' : '#67C23A'
        const label = value >= 0 ? '支出' : '收入'
        return `${data.name}<br/><span style="color:${color}">${label}：${Math.abs(value).toFixed(2)} 元</span>`
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
      data: dates,
      boundaryGap: false,
      axisLabel: {
        interval: 0,
        rotate: 0
      },
      triggerEvent: true
    },
    yAxis: {
      type: 'value',
      name: '支出（元）',
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        name: '每日支出',
        type: 'bar',
        data: balances,
        itemStyle: {
          color: (params) => {
            return params.value >= 0 ? '#F56C6C' : '#67C23A'
          }
        },
        barWidth: '60%',
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            return params.value.toFixed(2)
          },
          color: (params) => {
            return params.value >= 0 ? '#F56C6C' : '#67C23A'
          }
        }
      },
      {
        name: '趋势线',
        type: 'line',
        data: balances,
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
    let clickedDate = null
    if (params.componentType === 'series') {
      clickedDate = fullDates[params.dataIndex]
    } else if (params.componentType === 'xAxis') {
      // X轴点击时，params.value 是索引
      const index = typeof params.value === 'number' ? params.value : dates.indexOf(params.value)
      clickedDate = fullDates[index]
    }
    if (clickedDate) {
      console.log('Clicked date:', clickedDate)
      loadBillDetail(clickedDate)
    }
  })
}

const loadDailyExpense = async () => {
  loading.value = true
  try {
    const [year, month] = selectedMonth.value ? selectedMonth.value.split('-') : [null, null]
    const res = await getDailyExpense({
      year: year ? parseInt(year) : undefined,
      month: month ? parseInt(month) : undefined
    })
    
    if (res.code === 200) {
      balanceData.value = res.data || []
      if (hasData.value) {
        setTimeout(() => {
          initChart()
        }, 100)
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询每日支出失败:', error)
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
  loadDailyExpense()
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
.daily-balance-container {
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
