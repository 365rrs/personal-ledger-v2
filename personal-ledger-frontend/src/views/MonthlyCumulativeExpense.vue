<template>
  <div class="monthly-cumulative-expense-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>月度累计支出统计</span>
          <div class="filter-group">
            <el-date-picker
              v-model="selectedYear"
              type="year"
              placeholder="选择年份"
              format="YYYY年"
              value-format="YYYY"
              @change="loadMonthlyCumulativeExpense"
            />
            <el-radio-group v-model="amountType" @change="loadMonthlyCumulativeExpense">
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
            <el-statistic :title="`年度累计${amountType === 'EXPENSE' ? '支出' : '收入'}`" :value="yearlyTotal" :precision="2" suffix="元">
              <template #prefix>
                <el-icon :color="amountType === 'EXPENSE' ? '#F56C6C' : '#67C23A'">
                  <TrendCharts />
                </el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic :title="`月均累计${amountType === 'EXPENSE' ? '支出' : '收入'}`" :value="monthlyAverage" :precision="2" suffix="元" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="累计增长最快月份" :value="maxGrowthAmount" :precision="2" suffix="元">
              <template #suffix>
                <span class="month-suffix">{{ maxGrowthMonth }}</span>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="累计增长最慢月份" :value="minGrowthAmount" :precision="2" suffix="元">
              <template #suffix>
                <span class="month-suffix">{{ minGrowthMonth }}</span>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>

      <!-- 月度累计数据表格 -->
      <div v-if="hasData" class="table-container">
        <el-table :data="monthlyData" stripe>
          <el-table-column prop="month" label="月份" width="100" />
          <el-table-column prop="monthlyAmount" :label="`当月${amountType === 'EXPENSE' ? '支出' : '收入'}`" width="150" sortable>
            <template #default="{ row }">
              <span :style="{ color: '#409EFF' }">
                {{ row.monthlyAmount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="cumulativeAmount" :label="`累计${amountType === 'EXPENSE' ? '支出' : '收入'}`" width="150" sortable>
            <template #default="{ row }">
              <span :style="{ color: amountType === 'EXPENSE' ? '#F56C6C' : '#67C23A', fontWeight: 'bold' }">
                {{ row.cumulativeAmount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="percentage" label="当月占比" width="150">
            <template #default="{ row }">
              <el-progress :percentage="parseFloat(row.percentage.toFixed(2))" />
            </template>
          </el-table-column>
          <el-table-column label="累计进度" width="150">
            <template #default="{ row }">
              <el-progress :percentage="parseFloat(row.cumulativePercentage.toFixed(2))" :color="amountType === 'EXPENSE' ? '#F56C6C' : '#67C23A'" />
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
  return monthlyData.value[monthlyData.value.length - 1].cumulativeAmount
})

const monthlyAverage = computed(() => {
  if (!hasData.value) return 0
  return yearlyTotal.value / monthlyData.value.length
})

const maxGrowthAmount = computed(() => {
  if (!hasData.value) return 0
  return Math.max(...monthlyData.value.map(item => item.monthlyAmount))
})

const maxGrowthMonth = computed(() => {
  if (!hasData.value) return ''
  const maxItem = monthlyData.value.find(item => item.monthlyAmount === maxGrowthAmount.value)
  return maxItem ? maxItem.month : ''
})

const minGrowthAmount = computed(() => {
  if (!hasData.value) return 0
  return Math.min(...monthlyData.value.map(item => item.monthlyAmount))
})

const minGrowthMonth = computed(() => {
  if (!hasData.value) return ''
  const minItem = monthlyData.value.find(item => item.monthlyAmount === minGrowthAmount.value)
  return minItem ? minItem.month : ''
})

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance.value = echarts.init(chartRef.value)
  
  const months = monthlyData.value.map(item => item.month)
  const monthlyAmounts = monthlyData.value.map(item => item.monthlyAmount)
  const cumulativeAmounts = monthlyData.value.map(item => item.cumulativeAmount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const monthlyData = params[0]
        const cumulativeData = params[1]
        const item = monthlyData.value
        const cumulative = cumulativeData.value
        const percentage = cumulative > 0 ? ((item / cumulative) * 100).toFixed(1) : 0
        return `${monthlyData.name}<br/>` +
               `<span style="color:#409EFF">当月${amountType.value === 'EXPENSE' ? '支出' : '收入'}：${item.toFixed(2)} 元</span><br/>` +
               `<span style="color:${amountType.value === 'EXPENSE' ? '#F56C6C' : '#67C23A'}">累计${amountType.value === 'EXPENSE' ? '支出' : '收入'}：${cumulative.toFixed(2)} 元</span><br/>` +
               `<span style="color:#67C23A">当月占比：${percentage}%</span>`
      }
    },
    legend: {
      data: [`当月${amountType.value === 'EXPENSE' ? '支出' : '收入'}`, `累计${amountType.value === 'EXPENSE' ? '支出' : '收入'}`, '累计趋势']
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
        name: `当月${amountType.value === 'EXPENSE' ? '支出' : '收入'}`,
        type: 'bar',
        stack: 'total',
        data: monthlyAmounts,
        itemStyle: {
          color: '#409EFF'
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => {
            return params.value.toFixed(0)
          },
          color: '#fff'
        }
      },
      {
        name: `累计${amountType.value === 'EXPENSE' ? '支出' : '收入'}`,
        type: 'bar',
        stack: 'total',
        data: monthlyData.value.map((item, index) => {
          return item.cumulativeAmount - item.monthlyAmount
        }),
        itemStyle: {
          color: amountType.value === 'EXPENSE' ? '#F56C6C' : '#67C23A'
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params) => {
            const total = cumulativeAmounts[params.dataIndex]
            return total.toFixed(0)
          },
          color: amountType.value === 'EXPENSE' ? '#F56C6C' : '#67C23A'
        }
      },
      {
        name: '累计趋势',
        type: 'line',
        data: cumulativeAmounts,
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
    if (params.componentType === 'series') {
      const month = monthlyData.value[params.dataIndex].month
      loadBillDetail(month)
    }
  })
}

const loadMonthlyCumulativeExpense = async () => {
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
    let cumulativeTotal = 0
    let yearTotal = 0
    
    results.forEach((res, index) => {
      if (res.code === 200) {
        const monthlyAmount = amountType.value === 'EXPENSE' 
          ? res.data.totalExpense 
          : res.data.totalIncome
        cumulativeTotal += monthlyAmount
        yearTotal += monthlyAmount
        
        data.push({
          month: `${index + 1}月`,
          monthlyAmount: monthlyAmount,
          cumulativeAmount: cumulativeTotal,
          percentage: 0,
          cumulativePercentage: 0
        })
      }
    })
    
    // 计算占比
    data.forEach((item) => {
      item.percentage = yearTotal > 0 ? (item.monthlyAmount / yearTotal) * 100 : 0
      item.cumulativePercentage = yearTotal > 0 ? (item.cumulativeAmount / yearTotal) * 100 : 0
    })
    
    monthlyData.value = data
    
    if (hasData.value) {
      setTimeout(() => {
        initChart()
      }, 100)
    }
  } catch (error) {
    console.error('查询月度累计支出失败:', error)
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
  loadMonthlyCumulativeExpense()
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
.monthly-cumulative-expense-container {
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
