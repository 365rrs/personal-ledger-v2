<template>
  <div class="dashboard" v-loading="pageLoading">
    <!-- 顶部筛选 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-bar">
        <div class="filter-left">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            :clearable="false"
            @change="loadAll"
          />
          <span class="filter-hint">数据范围：{{ monthLabel }}（仅统计计入收支的账单）</span>
        </div>
        <el-button :icon="Refresh" @click="loadAll">刷新</el-button>
      </div>
    </el-card>

    <!-- 核心指标卡 -->
    <div class="stat-row">
      <el-card
        v-for="card in statCards"
        :key="card.key"
        class="stat-card"
        :class="card.key"
        shadow="hover"
      >
        <div class="stat-title">
          {{ card.title }}
          <el-tooltip v-if="card.tip" :content="card.tip" placement="top">
            <el-icon class="stat-tip"><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="stat-value">
          {{ card.text }}
          <span v-if="card.subText" class="stat-sub">{{ card.subText }}</span>
        </div>
        <div class="stat-compare">
          <template v-if="card.rate === null">
            <span class="compare-none">上月无数据</span>
          </template>
          <template v-else>
            <el-icon :class="card.rate >= 0 ? 'is-up' : 'is-down'">
              <component :is="card.rate >= 0 ? Top : Bottom" />
            </el-icon>
            <span :class="card.rate >= 0 ? 'is-up' : 'is-down'">
              {{ Math.abs(card.rate).toFixed(1) }}%
            </span>
            <span class="compare-label">较上月</span>
          </template>
        </div>
      </el-card>
    </div>

    <!-- 每日支出 + 分类占比 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>本月每日支出</span>
              <el-link type="primary" :underline="false" @click="goTo('/daily-expense')">查看详情</el-link>
            </div>
          </template>
          <div v-loading="dailyLoading" class="chart-wrapper">
            <el-empty v-if="!hasDailyData" description="本月暂无账单数据" :image-size="80" />
            <div v-show="hasDailyData" ref="dailyChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>本月分类支出</span>
              <el-link type="primary" :underline="false" @click="goTo('/category-statistics')">查看详情</el-link>
            </div>
          </template>
          <div v-loading="categoryLoading" class="chart-wrapper">
            <el-empty v-if="!hasCategoryData" description="本月暂无分类数据" :image-size="80" />
            <div v-show="hasCategoryData" ref="categoryChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 年度趋势 -->
    <el-card class="trend-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ selectedYear }} 年度收支趋势</span>
          <el-link type="primary" :underline="false" @click="goTo('/monthly-expense')">查看详情</el-link>
        </div>
      </template>
      <div v-loading="monthlyLoading" class="chart-wrapper">
        <el-empty v-if="!hasMonthlyData" description="本年度暂无账单数据" :image-size="80" />
        <div v-show="hasMonthlyData" ref="monthlyChartRef" class="chart chart-lg"></div>
      </div>
    </el-card>

    <!-- 最近账单 + 待支出提醒 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近账单</span>
              <el-link type="primary" :underline="false" @click="goTo('/bill')">全部账单</el-link>
            </div>
          </template>
          <el-table
            v-loading="recentLoading"
            :data="recentBills"
            size="small"
            stripe
            empty-text="暂无账单"
            @row-click="() => goTo('/bill')"
          >
            <el-table-column prop="transactionDate" label="日期" width="110" />
            <el-table-column prop="transactionDesc" label="描述" min-width="160" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="110" show-overflow-tooltip />
            <el-table-column label="金额" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.amountType === 'INCOME' ? 'text-income' : 'text-expense'">
                  {{ row.amountType === 'INCOME' ? '+' : '-' }}{{ formatAmount(row.amountType === 'INCOME' ? row.incomeAmount : row.expenseAmount) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>待支出提醒</span>
              <el-link type="primary" :underline="false" @click="goTo('/pending-expense')">全部待支出</el-link>
            </div>
          </template>
          <div v-loading="pendingLoading">
            <div class="pending-summary">
              <div class="pending-summary-item">
                <div class="pending-label">本月待支付</div>
                <div class="pending-value">¥ {{ formatAmount(pendingMonthAmount) }}</div>
              </div>
              <div class="pending-summary-item">
                <div class="pending-label">全年待支付</div>
                <div class="pending-value">¥ {{ formatAmount(pendingYearAmount) }}</div>
              </div>
            </div>
            <el-table
              :data="pendingList"
              size="small"
              stripe
              empty-text="暂无待支出项目"
              @row-click="() => goTo('/pending-expense')"
            >
              <el-table-column prop="paymentDate" label="支付日期" width="110" />
              <el-table-column prop="expenseName" label="项目" min-width="120" show-overflow-tooltip />
              <el-table-column label="金额" width="110" align="right">
                <template #default="{ row }">
                  <span class="text-expense">¥ {{ formatAmount(row.amount) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Top, Bottom, QuestionFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getStatistics,
  getDailyExpense,
  getCategoryStatistics,
  getMonthlyStatistics,
  pageBills
} from '@/api/bill'
import {
  getTotalPendingAmount,
  getPendingAmountByQuery,
  getPendingCountByQuery,
  pagePendingExpenses
} from '@/api/pendingExpense'

const router = useRouter()

// 饼图最多展示的分类数，其余归入「其他」
const CATEGORY_TOP_N = 8
// 最近账单展示条数
const RECENT_BILL_SIZE = 10
// 待支出提醒展示条数
const PENDING_SIZE = 5

const selectedMonth = ref('')

const pageLoading = ref(false)
const dailyLoading = ref(false)
const categoryLoading = ref(false)
const monthlyLoading = ref(false)
const recentLoading = ref(false)
const pendingLoading = ref(false)

const currentStat = ref({ totalIncome: 0, totalExpense: 0, balance: 0, billCount: 0 })
const previousStat = ref(null)
const dailyData = ref([])
const categoryData = ref([])
const monthlyData = ref([])
const recentBills = ref([])
const pendingList = ref([])
const pendingMonthAmount = ref(0)
const pendingMonthCount = ref(0)
const pendingYearAmount = ref(0)
// 上月待支出金额，用于指标卡环比；上月无待支出数据时为 null
const prevPendingMonthAmount = ref(null)

const dailyChartRef = ref(null)
const categoryChartRef = ref(null)
const monthlyChartRef = ref(null)
let dailyChart = null
let categoryChart = null
let monthlyChart = null

const selectedYear = computed(() => Number(selectedMonth.value.split('-')[0]))
const selectedMonthNum = computed(() => Number(selectedMonth.value.split('-')[1]))
const monthLabel = computed(() => `${selectedYear.value} 年 ${selectedMonthNum.value} 月`)

const hasDailyData = computed(() => dailyData.value.length > 0)
const hasCategoryData = computed(() => categoryData.value.length > 0)
const hasMonthlyData = computed(() => monthlyData.value.some(item => item.billCount > 0))

const formatAmount = (value) => Number(value || 0).toFixed(2)

// 计算环比：上月为 0 或无数据时返回 null，避免出现无意义的百分比
const calcRate = (current, previous) => {
  if (previous === null || previous === undefined) return null
  const prev = Number(previous)
  if (prev === 0) return null
  return ((Number(current) - prev) / Math.abs(prev)) * 100
}

// 本月支出 + 本月待支出，即本月预计总支出
const totalExpectedExpense = computed(
  () => Number(currentStat.value.totalExpense || 0) + Number(pendingMonthAmount.value || 0)
)

// 上月预计总支出，上月两项数据均缺失时返回 null，不展示环比
const prevTotalExpectedExpense = computed(() => {
  const prevExpense = previousStat.value?.totalExpense
  const prevPending = prevPendingMonthAmount.value
  if ((prevExpense === null || prevExpense === undefined) && prevPending === null) {
    return null
  }
  return Number(prevExpense || 0) + Number(prevPending || 0)
})

const statCards = computed(() => {
  const cur = currentStat.value
  const prev = previousStat.value
  return [
    {
      key: 'expense',
      title: '本月支出',
      text: `¥ ${formatAmount(cur.totalExpense)}`,
      rate: calcRate(cur.totalExpense, prev?.totalExpense)
    },
    {
      key: 'pending',
      title: '本月待支出',
      text: `¥ ${formatAmount(pendingMonthAmount.value)}`,
      rate: calcRate(pendingMonthAmount.value, prevPendingMonthAmount.value)
    },
    {
      key: 'expected',
      // title: '本月预计支出',
      title: '支出 + 待支出',
      tip: '本月支出 + 本月待支出',
      text: `¥ ${formatAmount(totalExpectedExpense.value)}`,
      rate: calcRate(totalExpectedExpense.value, prevTotalExpectedExpense.value)
    },
    {
      key: 'count',
      title: '本月笔数',
      tip: '主数字为本月已记账笔数，括号内为本月待支出笔数',
      text: String(cur.billCount || 0),
      subText: `(待 ${pendingMonthCount.value})`,
      rate: calcRate(cur.billCount, prev?.billCount)
    }
  ]
})

const goTo = (path) => {
  router.push(path)
}

// 计算某年某月的起止日期
const getMonthRange = (year, month) => {
  const lastDay = new Date(year, month, 0).getDate()
  const mm = String(month).padStart(2, '0')
  return {
    startDate: `${year}-${mm}-01`,
    endDate: `${year}-${mm}-${String(lastDay).padStart(2, '0')}`
  }
}

// 加载本月与上月汇总，用于指标卡及环比
const loadSummary = async () => {
  const year = selectedYear.value
  const month = selectedMonthNum.value
  const prevDate = new Date(year, month - 2, 1)

  const [curRes, prevRes] = await Promise.all([
    getStatistics({ ...getMonthRange(year, month), includeInStatistics: '1' }),
    getStatistics({
      ...getMonthRange(prevDate.getFullYear(), prevDate.getMonth() + 1),
      includeInStatistics: '1'
    })
  ])

  if (curRes.code === 200 && curRes.data) {
    currentStat.value = curRes.data
  }
  // 上月无任何账单时不展示环比
  previousStat.value = prevRes.code === 200 && prevRes.data && prevRes.data.billCount > 0
    ? prevRes.data
    : null
}

const loadDailyExpense = async () => {
  dailyLoading.value = true
  try {
    const res = await getDailyExpense({ year: selectedYear.value, month: selectedMonthNum.value })
    dailyData.value = res.code === 200 ? (res.data || []) : []
    if (hasDailyData.value) {
      await nextTick()
      renderDailyChart()
    }
  } finally {
    dailyLoading.value = false
  }
}

const loadCategoryStatistics = async () => {
  categoryLoading.value = true
  try {
    const res = await getCategoryStatistics({
      year: selectedYear.value,
      month: selectedMonthNum.value,
      amountType: 'EXPENSE'
    })
    categoryData.value = res.code === 200 ? (res.data || []) : []
    if (hasCategoryData.value) {
      await nextTick()
      renderCategoryChart()
    }
  } finally {
    categoryLoading.value = false
  }
}

const loadMonthlyStatistics = async () => {
  monthlyLoading.value = true
  try {
    const res = await getMonthlyStatistics({ year: selectedYear.value, includeInStatistics: '1' })
    monthlyData.value = res.code === 200 ? (res.data || []) : []
    if (hasMonthlyData.value) {
      await nextTick()
      renderMonthlyChart()
    }
  } finally {
    monthlyLoading.value = false
  }
}

const loadRecentBills = async () => {
  recentLoading.value = true
  try {
    const res = await pageBills({
      current: 1,
      size: RECENT_BILL_SIZE,
      orderBy: 'transactionDate',
      orderDirection: 'desc'
    })
    recentBills.value = res.code === 200 ? (res.data?.records || []) : []
  } finally {
    recentLoading.value = false
  }
}

const loadPendingExpense = async () => {
  pendingLoading.value = true
  try {
    const { startDate, endDate } = getMonthRange(selectedYear.value, selectedMonthNum.value)
    const prevDate = new Date(selectedYear.value, selectedMonthNum.value - 2, 1)
    const prevRange = getMonthRange(prevDate.getFullYear(), prevDate.getMonth() + 1)

    const [yearRes, monthRes, prevMonthRes, listRes, countRes] = await Promise.all([
      getTotalPendingAmount(selectedYear.value),
      getPendingAmountByQuery({
        statuses: ['PENDING'],
        paymentDateStart: startDate,
        paymentDateEnd: endDate
      }),
      getPendingAmountByQuery({
        statuses: ['PENDING'],
        paymentDateStart: prevRange.startDate,
        paymentDateEnd: prevRange.endDate
      }),
      pagePendingExpenses({
        statuses: ['PENDING'],
        paymentDateStart: startDate,
        sortField: 'paymentDate',
        sortOrder: 'asc',
        pageNum: 1,
        pageSize: PENDING_SIZE
      }),
      getPendingCountByQuery({
        statuses: ['PENDING'],
        paymentDateStart: startDate,
        paymentDateEnd: endDate
      })
    ])

    pendingYearAmount.value = yearRes.code === 200 ? (yearRes.data || 0) : 0
    pendingMonthAmount.value = monthRes.code === 200 ? (monthRes.data || 0) : 0
    prevPendingMonthAmount.value = prevMonthRes.code === 200 ? (prevMonthRes.data ?? null) : null
    pendingList.value = listRes.code === 200 ? (listRes.data?.records || []) : []
    pendingMonthCount.value = countRes.code === 200 ? (countRes.data || 0) : 0
  } finally {
    pendingLoading.value = false
  }
}

const loadAll = async () => {
  pageLoading.value = true
  try {
    await Promise.all([
      loadSummary(),
      loadDailyExpense(),
      loadCategoryStatistics(),
      loadMonthlyStatistics(),
      loadRecentBills(),
      loadPendingExpense()
    ])
  } catch (error) {
    ElMessage.error('加载首页数据失败')
    console.error('加载首页数据失败', error)
  } finally {
    pageLoading.value = false
  }
}

// ==================== 图表渲染 ====================

const renderDailyChart = () => {
  if (!dailyChartRef.value) return
  if (!dailyChart) {
    dailyChart = echarts.init(dailyChartRef.value)
  }
  dailyChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const item = params[0]
        const value = Number(item.value)
        const label = value >= 0 ? '净支出' : '净收入'
        return `${item.name}<br/>${label}：¥${Math.abs(value).toFixed(2)}`
      }
    },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      // 只显示「日」，避免完整日期挤在一起
      data: dailyData.value.map(item => Number(item.date.split('-')[2]))
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: (value) => `¥${value}` }
    },
    series: [
      {
        type: 'bar',
        data: dailyData.value.map(item => ({
          value: Number(item.balance),
          // 净收入的日期用绿色区分
          itemStyle: { color: Number(item.balance) >= 0 ? '#F56C6C' : '#67C23A' }
        })),
        barMaxWidth: 24
      }
    ]
  }, true)
}

const renderCategoryChart = () => {
  if (!categoryChartRef.value) return
  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  // Top N 之外的分类合并为「其他」，避免图例过长
  const sorted = [...categoryData.value].sort((a, b) => Number(b.amount) - Number(a.amount))
  const top = sorted.slice(0, CATEGORY_TOP_N).map(item => ({
    name: item.category,
    value: Number(item.amount)
  }))
  const restTotal = sorted.slice(CATEGORY_TOP_N)
    .reduce((sum, item) => sum + Number(item.amount), 0)
  if (restTotal > 0) {
    top.push({ name: '其他', value: restTotal })
  }

  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: ({ name, value, percent }) => `${name}<br/>¥${value.toFixed(2)}（${percent}%）`
    },
    legend: { type: 'scroll', orient: 'vertical', right: 10, top: 'center' },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['38%', '50%'],
        avoidLabelOverlap: true,
        label: { show: false },
        data: top
      }
    ]
  }, true)
}

const renderMonthlyChart = () => {
  if (!monthlyChartRef.value) return
  if (!monthlyChart) {
    monthlyChart = echarts.init(monthlyChartRef.value)
  }

  const now = new Date()
  // 当前年份只展示到当月，历史年份展示全年
  const maxMonth = selectedYear.value === now.getFullYear() ? now.getMonth() + 1 : 12
  const data = monthlyData.value.filter(item => item.month <= maxMonth)

  monthlyChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['支出', '收入', '结余'], top: 0 },
    grid: { left: 60, right: 20, top: 40, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.map(item => `${item.month}月`)
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: (value) => `¥${value}` }
    },
    series: [
      {
        name: '支出',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#F56C6C' },
        data: data.map(item => Number(item.totalExpense))
      },
      {
        name: '收入',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#67C23A' },
        data: data.map(item => Number(item.totalIncome))
      },
      {
        name: '结余',
        type: 'bar',
        itemStyle: { color: '#409EFF', opacity: 0.6 },
        barMaxWidth: 28,
        data: data.map(item => Number(item.balance))
      }
    ]
  }, true)
}

const handleResize = () => {
  dailyChart?.resize()
  categoryChart?.resize()
  monthlyChart?.resize()
}

onMounted(() => {
  const now = new Date()
  selectedMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  loadAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  dailyChart?.dispose()
  categoryChart?.dispose()
  monthlyChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-hint {
  font-size: 13px;
  color: #909399;
}

.chart-row,
.bottom-row {
  margin-bottom: 20px;
}

/* 5 张指标卡等分，用 flex 避免 24 栅格无法整除 */
.stat-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-row .stat-card {
  flex: 1;
  min-width: 0;
}

.trend-card {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.stat-tip {
  font-size: 13px;
  color: #c0c4cc;
  cursor: help;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.3;
  white-space: nowrap;
}

.stat-sub {
  font-size: 14px;
  font-weight: normal;
  color: #e6a23c;
  margin-left: 4px;
}

.stat-compare {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.compare-none {
  color: #c0c4cc;
}

.compare-label {
  color: #909399;
}

.is-up {
  color: #f56c6c;
}

.is-down {
  color: #67c23a;
}

.expense .stat-value {
  color: #f56c6c;
}

.pending .stat-value {
  color: #e6a23c;
}

.expected .stat-value {
  color: #f56c6c;
}

.count .stat-value {
  color: #409eff;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 500;
}

.chart-wrapper {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart {
  width: 100%;
  height: 300px;
}

.chart-lg {
  height: 340px;
}

.text-income {
  color: #67c23a;
}

.text-expense {
  color: #f56c6c;
}

.pending-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
}

.pending-summary-item {
  flex: 1;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 12px;
  text-align: center;
}

.pending-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.pending-value {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
