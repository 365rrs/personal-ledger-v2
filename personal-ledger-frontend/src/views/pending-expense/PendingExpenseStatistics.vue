<template>
  <div class="pending-expense-statistics-container">
    <!-- 全局筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <div class="global-filter">
        <div class="filter-item">
          <label>查询年份：</label>
          <el-date-picker
            v-model="selectedYear"
            type="year"
            placeholder="选择年份"
            format="YYYY年"
            value-format="YYYY"
            clearable
            @change="fetchStatistics"
            style="width: 200px;"
          />
        </div>
        <div class="filter-actions">
          <el-button type="primary" icon="Search" @click="fetchStatistics">
            查询
          </el-button>
          <el-button icon="Refresh" @click="resetFilter">
            重置
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="statistics-cards">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card total-amount">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="40"><Coin /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-label">待支付总金额</div>
              <div class="card-value">¥{{ totalAmount.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card total-count">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="40"><Document /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-label">待支付项目数量</div>
              <div class="card-value">{{ totalCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card rigid-expense">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="40"><Warning /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-label">刚性支出金额</div>
              <div class="card-value">¥{{ rigidAmount.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card intended-expense">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="40"><Star /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-label">意向计划支出金额</div>
              <div class="card-value">¥{{ intendedAmount.toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 按周期统计图表 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按周期统计</span>
            </div>
          </template>
          <div v-loading="loading" class="chart-container">
            <div v-if="!hasPeriodData" class="no-data">
              <el-empty description="暂无数据" />
            </div>
            <div v-else ref="periodChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>

      <!-- 按计划类型统计图表 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按计划类型统计</span>
            </div>
          </template>
          <div v-loading="loading" class="chart-container">
            <div v-if="!hasPlanTypeData" class="no-data">
              <el-empty description="暂无数据" />
            </div>
            <div v-else ref="planTypeChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 按月份统计图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按月份统计</span>
            </div>
          </template>
          <div v-loading="loading" class="chart-container">
            <div v-if="!hasMonthlyData" class="no-data">
              <el-empty description="暂无数据" />
            </div>
            <div v-else ref="monthlyChartRef" class="chart-large"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 按分类统计图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>按分类统计（前10）</span>
            </div>
          </template>
          <div v-loading="loading" class="chart-container">
            <div v-if="!hasCategoryData" class="no-data">
              <el-empty description="暂无数据" />
            </div>
            <div v-else ref="categoryChartRef" class="chart-large"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Coin, Document, Warning, Star } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getTotalPendingAmount,
  getMonthlyStatistics,
  getCategoryStatistics,
  getPeriodStatistics,
  getPlanTypeStatistics
} from '@/api/pendingExpense'

// 图表引用
const periodChartRef = ref(null)
const planTypeChartRef = ref(null)
const monthlyChartRef = ref(null)
const categoryChartRef = ref(null)

// 图表实例
const periodChartInstance = ref(null)
const planTypeChartInstance = ref(null)
const monthlyChartInstance = ref(null)
const categoryChartInstance = ref(null)

// 统计数据
const totalAmount = ref(0)
const totalCount = ref(0)
const rigidAmount = ref(0)
const intendedAmount = ref(0)

const periodStatistics = ref([])
const planTypeStatistics = ref([])
const monthlyStatistics = ref([])
const categoryStatistics = ref([])

const loading = ref(false)
const selectedYear = ref('')

// 计算属性：判断是否有数据
const hasPeriodData = computed(() => periodStatistics.value.length > 0)
const hasPlanTypeData = computed(() => planTypeStatistics.value.length > 0)
const hasMonthlyData = computed(() => monthlyStatistics.value.length > 0)
const hasCategoryData = computed(() => categoryStatistics.value.length > 0)

// 周期名称映射
const periodNameMap = {
  'MONTHLY': '每月',
  'YEARLY': '每年',
  'ONETIME': '一次性'
}

// 计划类型名称映射
const planTypeNameMap = {
  'RIGID': '刚性支出',
  'INTENDED': '意向计划支出'
}

// 初始化按周期统计图表
const initPeriodChart = () => {
  if (!periodChartRef.value || periodStatistics.value.length === 0) return
  
  if (periodChartInstance.value) {
    periodChartInstance.value.dispose()
  }
  
  periodChartInstance.value = echarts.init(periodChartRef.value)
  
  const data = periodStatistics.value.map(item => ({
    name: item.periodName || periodNameMap[item.period] || item.period,
    value: item.amount,
    count: item.count
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.name}<br/>` +
               `金额：¥${params.value.toFixed(2)}<br/>` +
               `数量：${params.data.count} 项`
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '按周期统计',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: data
      }
    ]
  }
  
  periodChartInstance.value.setOption(option)
}

// 初始化按计划类型统计图表
const initPlanTypeChart = () => {
  if (!planTypeChartRef.value || planTypeStatistics.value.length === 0) return
  
  if (planTypeChartInstance.value) {
    planTypeChartInstance.value.dispose()
  }
  
  planTypeChartInstance.value = echarts.init(planTypeChartRef.value)
  
  const data = planTypeStatistics.value.map(item => ({
    name: item.planTypeName || planTypeNameMap[item.planType] || item.planType,
    value: item.amount,
    count: item.count,
    percentage: item.percentage
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.name}<br/>` +
               `金额：¥${params.value.toFixed(2)}<br/>` +
               `数量：${params.data.count} 项<br/>` +
               `占比：${params.data.percentage.toFixed(2)}%`
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '按计划类型统计',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: data,
        color: ['#F56C6C', '#409EFF']
      }
    ]
  }
  
  planTypeChartInstance.value.setOption(option)
}

// 初始化按月份统计图表
const initMonthlyChart = () => {
  if (!monthlyChartRef.value || monthlyStatistics.value.length === 0) return
  
  if (monthlyChartInstance.value) {
    monthlyChartInstance.value.dispose()
  }
  
  monthlyChartInstance.value = echarts.init(monthlyChartRef.value)
  
  const months = monthlyStatistics.value.map(item => {
    const month = item.yearMonth.split('-')[1]
    return `${month}月`
  })
  const amounts = monthlyStatistics.value.map(item => item.amount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const data = params[0]
        const item = monthlyStatistics.value[data.dataIndex]
        return `${data.name}<br/>` +
               `金额：¥${item.amount.toFixed(2)}<br/>` +
               `数量：${item.count} 项`
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
        name: '待支出金额',
        type: 'bar',
        data: amounts,
        itemStyle: {
          color: '#E6A23C'
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
  
  monthlyChartInstance.value.setOption(option)
}

// 初始化按分类统计图表
const initCategoryChart = () => {
  if (!categoryChartRef.value || categoryStatistics.value.length === 0) return
  
  if (categoryChartInstance.value) {
    categoryChartInstance.value.dispose()
  }
  
  categoryChartInstance.value = echarts.init(categoryChartRef.value)
  
  // 按金额降序排序，取前10
  const sortedData = [...categoryStatistics.value]
    .sort((a, b) => b.amount - a.amount)
    .slice(0, 10)
  
  const categories = sortedData.map(item => item.categoryName || '未分类')
  const amounts = sortedData.map(item => item.amount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const data = params[0]
        const item = sortedData[data.dataIndex]
        return `${data.name}<br/>` +
               `金额：¥${item.amount.toFixed(2)}<br/>` +
               `数量：${item.count} 项`
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
        name: '待支出金额',
        type: 'bar',
        data: amounts,
        itemStyle: {
          color: '#67C23A'
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
  
  categoryChartInstance.value.setOption(option)
}

// 加载待支付总金额
const loadTotalAmount = async () => {
  try {
    const year = selectedYear.value ? parseInt(selectedYear.value) : null
    const res = await getTotalPendingAmount(year)
    if (res.code === 200) {
      totalAmount.value = res.data || 0
    }
  } catch (error) {
    console.error('获取待支付总金额失败:', error)
  }
}

// 加载按计划类型统计
const loadPlanTypeStatistics = async () => {
  try {
    const year = selectedYear.value ? parseInt(selectedYear.value) : null
    const res = await getPlanTypeStatistics(year)
    if (res.code === 200) {
      planTypeStatistics.value = res.data || []
      
      // 更新卡片统计数据
      totalCount.value = planTypeStatistics.value.reduce((sum, item) => sum + item.count, 0)
      
      const rigidItem = planTypeStatistics.value.find(item => item.planType === 'RIGID')
      rigidAmount.value = rigidItem ? rigidItem.amount : 0
      
      const intendedItem = planTypeStatistics.value.find(item => item.planType === 'INTENDED')
      intendedAmount.value = intendedItem ? intendedItem.amount : 0
      
      setTimeout(() => {
        initPlanTypeChart()
      }, 100)
    }
  } catch (error) {
    console.error('获取按计划类型统计失败:', error)
  }
}

// 加载按周期统计
const loadPeriodStatistics = async () => {
  try {
    const year = selectedYear.value ? parseInt(selectedYear.value) : null
    const res = await getPeriodStatistics(year)
    if (res.code === 200) {
      periodStatistics.value = res.data || []
      setTimeout(() => {
        initPeriodChart()
      }, 100)
    }
  } catch (error) {
    console.error('获取按周期统计失败:', error)
  }
}

// 加载按月份统计
const loadMonthlyStatistics = async () => {
  try {
    const year = selectedYear.value ? parseInt(selectedYear.value) : null
    const res = await getMonthlyStatistics(year)
    if (res.code === 200) {
      monthlyStatistics.value = res.data || []
      setTimeout(() => {
        initMonthlyChart()
      }, 100)
    }
  } catch (error) {
    console.error('获取按月份统计失败:', error)
  }
}

// 加载按分类统计
const loadCategoryStatistics = async () => {
  try {
    const year = selectedYear.value ? parseInt(selectedYear.value) : null
    const res = await getCategoryStatistics(year)
    if (res.code === 200) {
      categoryStatistics.value = res.data || []
      setTimeout(() => {
        initCategoryChart()
      }, 100)
    }
  } catch (error) {
    console.error('获取按分类统计失败:', error)
  }
}

// 加载所有统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadTotalAmount(),
      loadPlanTypeStatistics(),
      loadPeriodStatistics(),
      loadMonthlyStatistics(),
      loadCategoryStatistics()
    ])
    ElMessage.success('统计数据加载成功')
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
const resetFilter = () => {
  selectedYear.value = String(new Date().getFullYear())
  fetchStatistics()
}

// 窗口大小改变时重新调整图表
const handleResize = () => {
  if (periodChartInstance.value) {
    periodChartInstance.value.resize()
  }
  if (planTypeChartInstance.value) {
    planTypeChartInstance.value.resize()
  }
  if (monthlyChartInstance.value) {
    monthlyChartInstance.value.resize()
  }
  if (categoryChartInstance.value) {
    categoryChartInstance.value.resize()
  }
}

onMounted(() => {
  // 默认选择当前年份
  selectedYear.value = String(new Date().getFullYear())
  
  // 加载统计数据
  fetchStatistics()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  // 销毁图表实例
  if (periodChartInstance.value) {
    periodChartInstance.value.dispose()
  }
  if (planTypeChartInstance.value) {
    planTypeChartInstance.value.dispose()
  }
  if (monthlyChartInstance.value) {
    monthlyChartInstance.value.dispose()
  }
  if (categoryChartInstance.value) {
    categoryChartInstance.value.dispose()
  }
  
  // 移除窗口大小变化监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.pending-expense-statistics-container {
  padding: 20px;
}

/* 统计卡片样式 */
.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.total-amount .card-icon {
  background-color: #E6F7FF;
  color: #1890FF;
}

.total-count .card-icon {
  background-color: #F0F5FF;
  color: #597EF7;
}

.rigid-expense .card-icon {
  background-color: #FFF1F0;
  color: #F5222D;
}

.intended-expense .card-icon {
  background-color: #FFFBE6;
  color: #FAAD14;
}

.card-info {
  flex: 1;
}

.card-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #262626;
}

/* 图表区域样式 */
.chart-row {
  margin-bottom: 20px;
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
  min-height: 350px;
  position: relative;
}

.chart {
  width: 100%;
  height: 350px;
}

.chart-large {
  width: 100%;
  height: 400px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 350px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .statistics-cards {
    margin-bottom: 15px;
  }
  
  .card-value {
    font-size: 20px;
  }
  
  .chart {
    height: 300px;
  }
  
  .chart-large {
    height: 350px;
  }
}

/* 全局筛选卡片样式 */
.filter-card {
  margin-bottom: 20px;
  border: 1px solid #e8e8e8;
}

.global-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-item label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .global-filter {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-item {
    width: 100%;
  }
  
  .filter-item label {
    min-width: 80px;
  }
  
  .filter-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
