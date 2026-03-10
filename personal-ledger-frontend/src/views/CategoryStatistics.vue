<template>
  <div class="category-statistics-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类统计</span>
          <div class="filter-group">
            <el-radio-group v-model="timeType" @change="handleTimeTypeChange">
              <el-radio-button label="month">按月</el-radio-button>
              <el-radio-button label="year">按年</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-if="timeType === 'month'"
              v-model="selectedMonth"
              type="month"
              placeholder="选择月份"
              format="YYYY年MM月"
              value-format="YYYY-MM"
              @change="loadStatistics"
            />
            <el-date-picker
              v-else
              v-model="selectedYear"
              type="year"
              placeholder="选择年份"
              format="YYYY年"
              value-format="YYYY"
              @change="loadStatistics"
            />
            <el-radio-group v-model="amountType" @change="loadStatistics">
              <el-radio-button label="EXPENSE">支出</el-radio-button>
              <el-radio-button label="INCOME">收入</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="chart-container">
        <div v-if="!hasData" class="no-data">
          <el-empty description="暂无数据" />
        </div>
        <div v-else ref="chartRef" class="chart"></div>
      </div>

      <div v-if="hasData" class="summary">
        <el-table :data="statisticsData" stripe>
          <el-table-column prop="category" label="分类" width="150" />
          <el-table-column prop="amount" label="金额" width="150">
            <template #default="{ row }">
              <span :style="{ color: amountType === 'EXPENSE' ? '#F56C6C' : '#67C23A' }">
                {{ row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="笔数" width="100" />
          <el-table-column prop="percentage" label="占比" width="150">
            <template #default="{ row }">
              <el-progress :percentage="parseFloat(row.percentage.toFixed(2))" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="loadBillDetail(row.category)">
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
        <span>{{ selectedCategory }} 账单明细</span>
      </template>
      <el-table v-loading="billLoading" :data="billList" stripe>
        <el-table-column prop="transactionDate" label="日期" width="120" />
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getCategoryStatistics, pageBills } from '@/api/bill'
import { listTags } from '@/api/tag'

const chartRef = ref(null)
const chartInstance = ref(null)
const timeType = ref('month')
const selectedMonth = ref('')
const selectedYear = ref('')
const amountType = ref('EXPENSE')
const loading = ref(false)
const statisticsData = ref([])
const billLoading = ref(false)
const billList = ref([])
const selectedCategory = ref('')
const tagList = ref([])

const hasData = computed(() => statisticsData.value.length > 0)

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance.value = echarts.init(chartRef.value)
  
  const categories = statisticsData.value.map(item => item.category)
  const amounts = statisticsData.value.map(item => item.amount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const data = params[0]
        const item = statisticsData.value[data.dataIndex]
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
      data: categories,
      axisLabel: {
        interval: 0,
        rotate: 45
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
      }
    ]
  }
  
  chartInstance.value.setOption(option)
  
  // 添加点击事件
  chartInstance.value.on('click', (params) => {
    if (params.componentType === 'series') {
      const category = statisticsData.value[params.dataIndex].category
      loadBillDetail(category)
    }
  })
}

const loadStatistics = async () => {
  loading.value = true
  try {
    let year, month
    if (timeType.value === 'month') {
      [year, month] = selectedMonth.value ? selectedMonth.value.split('-') : [null, null]
    } else {
      year = selectedYear.value
      month = null
    }
    
    const res = await getCategoryStatistics({
      year: year ? parseInt(year) : undefined,
      month: month ? parseInt(month) : undefined,
      amountType: amountType.value
    })
    
    if (res.code === 200) {
      statisticsData.value = res.data || []
      if (hasData.value) {
        setTimeout(() => {
          initChart()
        }, 100)
      }
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    console.error('查询分类统计失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const loadBillDetail = async (category) => {
  selectedCategory.value = category
  billLoading.value = true
  
  try {
    let year, month, startDate, endDate
    if (timeType.value === 'month') {
      [year, month] = selectedMonth.value.split('-')
      startDate = `${year}-${month}-01`
      const lastDay = new Date(year, month, 0).getDate()
      endDate = `${year}-${month}-${lastDay}`
    } else {
      year = selectedYear.value
      startDate = `${year}-01-01`
      endDate = `${year}-12-31`
    }
    
    const res = await pageBills({
      current: 1,
      size: 1000,
      startDate,
      endDate,
      category: category === '未分类' ? null : category,
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

const handleTimeTypeChange = () => {
  if (timeType.value === 'month') {
    const now = new Date()
    selectedMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  } else {
    selectedYear.value = String(new Date().getFullYear())
  }
  loadStatistics()
}

const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

onMounted(() => {
  const now = new Date()
  selectedMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  loadStatistics()
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
.category-statistics-container {
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

.bill-detail-card {
  margin-top: 20px;
}
</style>
