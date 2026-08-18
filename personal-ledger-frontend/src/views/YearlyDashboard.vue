<template>
  <div class="yearly-dashboard" v-loading="pageLoading">
    <!-- 顶部筛选 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-bar">
        <div class="filter-left">
          <el-date-picker
            v-model="selectedYear"
            type="year"
            placeholder="选择年份"
            format="YYYY年"
            value-format="YYYY"
            :clearable="false"
            @change="loadAll"
          />
          <span class="filter-hint">
            数据范围：{{ rangeLabel }}（仅统计计入收支的账单）；同比对比 {{ prevYear }} 年同期
          </span>
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
        <div class="stat-value">{{ card.text }}</div>
        <div v-if="card.subText" class="stat-sub">{{ card.subText }}</div>
        <div class="stat-compare">
          <template v-if="card.rate === null">
            <span class="compare-none">{{ card.emptyText || '去年同期无数据' }}</span>
          </template>
          <template v-else>
            <el-icon :class="card.rate >= 0 ? 'is-up' : 'is-down'">
              <component :is="card.rate >= 0 ? Top : Bottom" />
            </el-icon>
            <span :class="card.rate >= 0 ? 'is-up' : 'is-down'">
              {{ Math.abs(card.rate).toFixed(1) }}%
            </span>
            <span class="compare-label">较去年同期</span>
          </template>
        </div>
      </el-card>
    </div>

    <!-- 年度收支趋势 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ yearNum }} 年月度收支趋势</span>
          <el-link type="primary" :underline="false" @click="goTo('/monthly-expense')">查看详情</el-link>
        </div>
      </template>
      <div v-loading="monthlyLoading" class="chart-wrapper">
        <el-empty v-if="!hasMonthlyData" description="本年度暂无账单数据" :image-size="80" />
        <div v-show="hasMonthlyData" ref="trendChartRef" class="chart chart-lg"></div>
      </div>
    </el-card>

    <!-- 支出同比 + 累计支出同比 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>月度支出同比</span>
              <span class="card-sub">{{ yearNum }} 年 vs {{ prevYear }} 年</span>
            </div>
          </template>
          <div v-loading="monthlyLoading" class="chart-wrapper">
            <el-empty v-if="!hasMonthlyData" description="暂无数据" :image-size="80" />
            <div v-show="hasMonthlyData" ref="compareChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>累计支出同比</span>
              <el-link type="primary" :underline="false" @click="goTo('/monthly-cumulative-expense')">查看详情</el-link>
            </div>
          </template>
          <div v-loading="monthlyLoading" class="chart-wrapper">
            <el-empty v-if="!hasMonthlyData" description="暂无数据" :image-size="80" />
            <div v-show="hasMonthlyData" ref="cumulativeChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 年度分类支出 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>年度分类支出占比</span>
              <el-link type="primary" :underline="false" @click="goTo('/category-statistics')">查看详情</el-link>
            </div>
          </template>
          <div v-loading="categoryLoading" class="chart-wrapper">
            <el-empty v-if="!hasCategoryData" description="本年度暂无分类数据" :image-size="80" />
            <div v-show="hasCategoryData" ref="categoryChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>分类支出排行（含同比）</span>
              <span class="card-sub">Top {{ CATEGORY_RANK_SIZE }}</span>
            </div>
          </template>
          <el-table
            v-loading="categoryLoading"
            :data="categoryRankList"
            size="small"
            stripe
            empty-text="暂无分类数据"
            max-height="300"
          >
            <el-table-column label="#" type="index" width="50" align="center" />
            <el-table-column prop="category" label="分类" min-width="120" show-overflow-tooltip />
            <el-table-column label="金额" width="130" align="right">
              <template #default="{ row }">
                <span class="text-expense">¥ {{ formatAmount(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="笔数" width="80" align="right" />
            <el-table-column label="占比" width="90" align="right">
              <template #default="{ row }">{{ Number(row.percentage || 0).toFixed(1) }}%</template>
            </el-table-column>
            <el-table-column label="同比" width="110" align="right">
              <template #default="{ row }">
                <span v-if="row.rate === null" class="compare-none">-</span>
                <span v-else :class="row.rate >= 0 ? 'is-up' : 'is-down'">
                  {{ row.rate >= 0 ? '+' : '-' }}{{ Math.abs(row.rate).toFixed(1) }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 年度待支出 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>全年待支出月度分布</span>
              <el-link type="primary" :underline="false" @click="goTo('/pending-expense/statistics')">查看详情</el-link>
            </div>
          </template>
          <div v-loading="pendingLoading" class="chart-wrapper">
            <el-empty v-if="!hasPendingData" description="本年度暂无待支出数据" :image-size="80" />
            <div v-show="hasPendingData" ref="pendingChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>待支出分类构成</span>
              <span class="card-sub">全年合计 ¥ {{ formatAmount(pendingYearAmount) }}</span>
            </div>
          </template>
          <el-table
            v-loading="pendingLoading"
            :data="pendingCategoryList"
            size="small"
            stripe
            empty-text="暂无待支出数据"
            max-height="300"
          >
            <el-table-column prop="categoryName" label="分类" min-width="100" show-overflow-tooltip />
            <el-table-column label="金额" width="120" align="right">
              <template #default="{ row }">
                <span class="text-pending">¥ {{ formatAmount(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="笔数" width="70" align="right" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 月度明细 + 年度大额支出 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>月度收支明细</span>
              <el-link type="primary" :underline="false" @click="goTo('/monthly-expense')">月度支出</el-link>
            </div>
          </template>
          <el-table
            v-loading="monthlyLoading"
            :data="monthlyTableData"
            size="small"
            stripe
            empty-text="本年度暂无账单数据"
            max-height="360"
            show-summary
            :summary-method="monthlySummary"
          >
            <el-table-column prop="monthLabel" label="月份" width="80" />
            <el-table-column label="收入" min-width="110" align="right">
              <template #default="{ row }">
                <span class="text-income">{{ formatAmount(row.totalIncome) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="支出" min-width="110" align="right">
              <template #default="{ row }">
                <span class="text-expense">{{ formatAmount(row.totalExpense) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="结余" min-width="110" align="right">
              <template #default="{ row }">
                <span :class="Number(row.balance) >= 0 ? 'text-income' : 'text-expense'">
                  {{ formatAmount(row.balance) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="billCount" label="笔数" width="80" align="right" />
            <el-table-column label="支出环比" width="100" align="right">
              <template #default="{ row }">
                <span v-if="row.momRate === null" class="compare-none">-</span>
                <span v-else :class="row.momRate >= 0 ? 'is-up' : 'is-down'">
                  {{ row.momRate >= 0 ? '+' : '-' }}{{ Math.abs(row.momRate).toFixed(1) }}%
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
              <span>年度大额支出 Top {{ LARGE_EXPENSE_SIZE }}</span>
              <el-link type="primary" :underline="false" @click="goTo('/large-expense')">查看详情</el-link>
            </div>
          </template>
          <el-table
            v-loading="largeLoading"
            :data="largeExpenses"
            size="small"
            stripe
            empty-text="暂无支出账单"
            max-height="360"
            @row-click="() => goTo('/bill')"
          >
            <el-table-column prop="transactionDate" label="日期" width="100" />
            <el-table-column prop="transactionDesc" label="描述" min-width="130" show-overflow-tooltip />
            <el-table-column label="金额" width="120" align="right">
              <template #default="{ row }">
                <span class="text-expense">¥ {{ formatAmount(row.expenseAmount) }}</span>
              </template>
            </el-table-column>
          </el-table>
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
  getCategoryStatistics,
  getMonthlyStatistics,
  pageBills
} from '@/api/bill'
import {
  getTotalPendingAmount,
  getMonthlyStatistics as getPendingMonthlyStatistics,
  getCategoryStatistics as getPendingCategoryStatistics
} from '@/api/pendingExpense'

const router = useRouter()

// 饼图最多展示的分类数，其余归入「其他」
const CATEGORY_TOP_N = 8
// 分类排行榜展示条数
const CATEGORY_RANK_SIZE = 10
// 大额支出展示条数
const LARGE_EXPENSE_SIZE = 10

const selectedYear = ref('')

const pageLoading = ref(false)
const monthlyLoading = ref(false)
const categoryLoading = ref(false)
const pendingLoading = ref(false)
const largeLoading = ref(false)

// 本年同期汇总（当前年份只统计到当月）
const currentStat = ref({ totalIncome: 0, totalExpense: 0, balance: 0, billCount: 0 })
// 去年同期汇总，无数据时为 null
const previousStat = ref(null)
const monthlyData = ref([])
const prevMonthlyData = ref([])
const categoryData = ref([])
const prevCategoryData = ref([])
const pendingMonthlyData = ref([])
const pendingCategoryList = ref([])
const pendingYearAmount = ref(0)
const largeExpenses = ref([])

const trendChartRef = ref(null)
const compareChartRef = ref(null)
const cumulativeChartRef = ref(null)
const categoryChartRef = ref(null)
const pendingChartRef = ref(null)
let trendChart = null
let compareChart = null
let cumulativeChart = null
let categoryChart = null
let pendingChart = null

const yearNum = computed(() => Number(selectedYear.value))
const prevYear = computed(() => yearNum.value - 1)

// 当前年份只统计到当月，历史年份统计全年
const maxMonth = computed(() => {
  const now = new Date()
  return yearNum.value === now.getFullYear() ? now.getMonth() + 1 : 12
})

const rangeLabel = computed(() =>
  maxMonth.value === 12
    ? `${yearNum.value} 年 1 - 12 月`
    : `${yearNum.value} 年 1 - ${maxMonth.value} 月`
)

const hasMonthlyData = computed(() => monthlyData.value.some(item => item.billCount > 0))
const hasCategoryData = computed(() => categoryData.value.length > 0)
const hasPendingData = computed(() => pendingMonthlyData.value.length > 0)

const formatAmount = (value) => Number(value || 0).toFixed(2)

// 计算同比：基期为 0 或无数据时返回 null，避免出现无意义的百分比
const calcRate = (current, previous) => {
  if (previous === null || previous === undefined) return null
  const prev = Number(previous)
  if (prev === 0) return null
  return ((Number(current) - prev) / Math.abs(prev)) * 100
}

// 已发生月份的月均支出（年度支出 / 已统计月数）
const avgExpense = computed(() =>
  maxMonth.value > 0 ? Number(currentStat.value.totalExpense || 0) / maxMonth.value : 0
)

// 年度支出 + 全年待支出，即年度预计总支出
const totalExpectedExpense = computed(
  () => Number(currentStat.value.totalExpense || 0) + Number(pendingYearAmount.value || 0)
)

// 预计月均支出：全年预计支出 / 12 个月
const expectedAvgExpense = computed(() => totalExpectedExpense.value / 12)

const statCards = computed(() => {
  const cur = currentStat.value
  const prev = previousStat.value
  return [
    {
      key: 'expense',
      title: '年度支出',
      tip: `已记账支出，统计范围：${rangeLabel.value}`,
      text: `¥ ${formatAmount(cur.totalExpense)}`,
      rate: calcRate(cur.totalExpense, prev?.totalExpense)
    },
    {
      key: 'pending',
      title: '全年待支出',
      tip: `${yearNum.value} 年全年状态为待支付的项目金额合计`,
      text: `¥ ${formatAmount(pendingYearAmount.value)}`,
      rate: null,
      emptyText: '不参与同比'
    },
    {
      key: 'expected',
      // title: '全年预计支出',
      title: '支出 + 待支出',
      tip: '年度支出 + 全年待支出',
      text: `¥ ${formatAmount(totalExpectedExpense.value)}`,
      rate: null,
      emptyText: '不参与同比'
    },
    {
      key: 'avg',
      title: '月均支出',
      tip: `主值：年度支出 / ${maxMonth.value} 个月；副值：全年预计支出 / 12 个月`,
      text: `¥ ${formatAmount(avgExpense.value)}`,
      subText: `预计 ¥ ${formatAmount(expectedAvgExpense.value)}/月`,
      rate: calcRate(avgExpense.value, prev ? Number(prev.totalExpense || 0) / maxMonth.value : null)
    },
    {
      key: 'count',
      title: '年度笔数',
      text: String(cur.billCount || 0),
      rate: calcRate(cur.billCount, prev?.billCount)
    }
  ]
})

// 按月份补齐 1-12 月，缺失月份填 0，便于图表和表格展示
const fillMonths = (list) => {
  const map = new Map((list || []).map(item => [Number(item.month), item]))
  return Array.from({ length: 12 }, (_, index) => {
    const month = index + 1
    const item = map.get(month)
    return {
      month,
      monthLabel: `${month}月`,
      totalIncome: Number(item?.totalIncome || 0),
      totalExpense: Number(item?.totalExpense || 0),
      balance: Number(item?.totalIncome || 0) - Number(item?.totalExpense || 0),
      billCount: Number(item?.billCount || 0)
    }
  })
}

// 图表与表格只展示到 maxMonth
const visibleMonthly = computed(() =>
  fillMonths(monthlyData.value).filter(item => item.month <= maxMonth.value)
)

const monthlyTableData = computed(() => {
  const list = visibleMonthly.value
  return list.map((item, index) => ({
    ...item,
    momRate: index === 0 ? null : calcRate(item.totalExpense, list[index - 1].totalExpense)
  }))
})

const monthlySummary = ({ columns, data }) => {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.label === '收入') {
      sums[index] = formatAmount(data.reduce((sum, row) => sum + Number(row.totalIncome), 0))
    } else if (column.label === '支出') {
      sums[index] = formatAmount(data.reduce((sum, row) => sum + Number(row.totalExpense), 0))
    } else if (column.label === '结余') {
      sums[index] = formatAmount(data.reduce((sum, row) => sum + Number(row.balance), 0))
    } else if (column.label === '笔数') {
      sums[index] = String(data.reduce((sum, row) => sum + Number(row.billCount), 0))
    } else {
      sums[index] = '-'
    }
  })
  return sums
}

// 分类排行榜，附带去年同期同比
const categoryRankList = computed(() => {
  const prevMap = new Map(prevCategoryData.value.map(item => [item.category, Number(item.amount)]))
  return [...categoryData.value]
    .sort((a, b) => Number(b.amount) - Number(a.amount))
    .slice(0, CATEGORY_RANK_SIZE)
    .map(item => ({
      ...item,
      rate: calcRate(item.amount, prevMap.has(item.category) ? prevMap.get(item.category) : null)
    }))
})

const goTo = (path) => {
  router.push(path)
}

// 年度日期范围，当前年份截止到当月最后一天
const getYearRange = (year, endMonth) => {
  const lastDay = new Date(year, endMonth, 0).getDate()
  return {
    startDate: `${year}-01-01`,
    endDate: `${year}-${String(endMonth).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`
  }
}

// 加载本年与去年同期汇总，用于指标卡及同比
const loadSummary = async () => {
  const [curRes, prevRes] = await Promise.all([
    getStatistics({ ...getYearRange(yearNum.value, maxMonth.value), includeInStatistics: '1' }),
    getStatistics({ ...getYearRange(prevYear.value, maxMonth.value), includeInStatistics: '1' })
  ])

  if (curRes.code === 200 && curRes.data) {
    currentStat.value = curRes.data
  }
  // 去年同期无任何账单时不展示同比
  previousStat.value = prevRes.code === 200 && prevRes.data && prevRes.data.billCount > 0
    ? prevRes.data
    : null
}

const loadMonthlyStatistics = async () => {
  monthlyLoading.value = true
  try {
    const [curRes, prevRes] = await Promise.all([
      getMonthlyStatistics({ year: yearNum.value, includeInStatistics: '1' }),
      getMonthlyStatistics({ year: prevYear.value, includeInStatistics: '1' })
    ])
    monthlyData.value = curRes.code === 200 ? (curRes.data || []) : []
    prevMonthlyData.value = prevRes.code === 200 ? (prevRes.data || []) : []
    if (hasMonthlyData.value) {
      await nextTick()
      renderTrendChart()
      renderCompareChart()
      renderCumulativeChart()
    }
  } finally {
    monthlyLoading.value = false
  }
}

const loadCategoryStatistics = async () => {
  categoryLoading.value = true
  try {
    const [curRes, prevRes] = await Promise.all([
      getCategoryStatistics({ year: yearNum.value, amountType: 'EXPENSE' }),
      getCategoryStatistics({ year: prevYear.value, amountType: 'EXPENSE' })
    ])
    categoryData.value = curRes.code === 200 ? (curRes.data || []) : []
    prevCategoryData.value = prevRes.code === 200 ? (prevRes.data || []) : []
    if (hasCategoryData.value) {
      await nextTick()
      renderCategoryChart()
    }
  } finally {
    categoryLoading.value = false
  }
}

const loadPendingExpense = async () => {
  pendingLoading.value = true
  try {
    const [totalRes, monthlyRes, categoryRes] = await Promise.all([
      getTotalPendingAmount(yearNum.value),
      getPendingMonthlyStatistics(yearNum.value),
      getPendingCategoryStatistics(yearNum.value)
    ])
    pendingYearAmount.value = totalRes.code === 200 ? (totalRes.data || 0) : 0
    pendingMonthlyData.value = monthlyRes.code === 200 ? (monthlyRes.data || []) : []
    pendingCategoryList.value = categoryRes.code === 200 ? (categoryRes.data || []) : []
    if (hasPendingData.value) {
      await nextTick()
      renderPendingChart()
    }
  } finally {
    pendingLoading.value = false
  }
}

const loadLargeExpenses = async () => {
  largeLoading.value = true
  try {
    const res = await pageBills({
      current: 1,
      size: LARGE_EXPENSE_SIZE,
      ...getYearRange(yearNum.value, 12),
      amountType: 'EXPENSE',
      includeInStatistics: '1',
      orderBy: 'expenseAmount',
      orderDirection: 'desc'
    })
    largeExpenses.value = res.code === 200 ? (res.data?.records || []) : []
  } finally {
    largeLoading.value = false
  }
}

const loadAll = async () => {
  pageLoading.value = true
  try {
    await Promise.all([
      loadSummary(),
      loadMonthlyStatistics(),
      loadCategoryStatistics(),
      loadPendingExpense(),
      loadLargeExpenses()
    ])
  } catch (error) {
    ElMessage.error('加载年度看板数据失败')
    console.error('加载年度看板数据失败', error)
  } finally {
    pageLoading.value = false
  }
}

// ==================== 图表渲染 ====================

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const data = visibleMonthly.value
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      valueFormatter: (value) => `¥${Number(value).toFixed(2)}`
    },
    legend: { data: ['支出', '收入', '结余'], top: 0 },
    grid: { left: 70, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: data.map(item => item.monthLabel) },
    yAxis: { type: 'value', axisLabel: { formatter: (value) => `¥${value}` } },
    series: [
      {
        name: '支出',
        type: 'bar',
        itemStyle: { color: '#F56C6C' },
        barMaxWidth: 22,
        data: data.map(item => item.totalExpense)
      },
      {
        name: '收入',
        type: 'bar',
        itemStyle: { color: '#67C23A' },
        barMaxWidth: 22,
        data: data.map(item => item.totalIncome)
      },
      {
        name: '结余',
        type: 'line',
        smooth: true,
        itemStyle: { color: '#409EFF' },
        data: data.map(item => item.balance)
      }
    ]
  }, true)
}

const renderCompareChart = () => {
  if (!compareChartRef.value) return
  if (!compareChart) {
    compareChart = echarts.init(compareChartRef.value)
  }
  const cur = fillMonths(monthlyData.value)
  const prev = fillMonths(prevMonthlyData.value)
  compareChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      valueFormatter: (value) => `¥${Number(value).toFixed(2)}`
    },
    legend: { data: [`${yearNum.value} 年`, `${prevYear.value} 年`], top: 0 },
    grid: { left: 70, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: cur.map(item => item.monthLabel) },
    yAxis: { type: 'value', axisLabel: { formatter: (value) => `¥${value}` } },
    series: [
      {
        name: `${yearNum.value} 年`,
        type: 'line',
        smooth: true,
        itemStyle: { color: '#F56C6C' },
        // 当前年份未到的月份不连线
        data: cur.map(item => (item.month <= maxMonth.value ? item.totalExpense : null))
      },
      {
        name: `${prevYear.value} 年`,
        type: 'line',
        smooth: true,
        lineStyle: { type: 'dashed' },
        itemStyle: { color: '#909399' },
        data: prev.map(item => item.totalExpense)
      }
    ]
  }, true)
}

// 将月度支出转为累计支出序列
const toCumulative = (list, limitMonth) => {
  let sum = 0
  return list.map(item => {
    if (item.month > limitMonth) return null
    sum += item.totalExpense
    return Number(sum.toFixed(2))
  })
}

const renderCumulativeChart = () => {
  if (!cumulativeChartRef.value) return
  if (!cumulativeChart) {
    cumulativeChart = echarts.init(cumulativeChartRef.value)
  }
  const cur = fillMonths(monthlyData.value)
  const prev = fillMonths(prevMonthlyData.value)
  cumulativeChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      valueFormatter: (value) => `¥${Number(value).toFixed(2)}`
    },
    legend: { data: [`${yearNum.value} 年累计`, `${prevYear.value} 年累计`], top: 0 },
    grid: { left: 70, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: cur.map(item => item.monthLabel) },
    yAxis: { type: 'value', axisLabel: { formatter: (value) => `¥${value}` } },
    series: [
      {
        name: `${yearNum.value} 年累计`,
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#E6A23C' },
        data: toCumulative(cur, maxMonth.value)
      },
      {
        name: `${prevYear.value} 年累计`,
        type: 'line',
        smooth: true,
        lineStyle: { type: 'dashed' },
        itemStyle: { color: '#909399' },
        data: toCumulative(prev, 12)
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
        center: ['35%', '50%'],
        avoidLabelOverlap: true,
        label: { show: false },
        data: top
      }
    ]
  }, true)
}

const renderPendingChart = () => {
  if (!pendingChartRef.value) return
  if (!pendingChart) {
    pendingChart = echarts.init(pendingChartRef.value)
  }

  // 后端返回 yearMonth（如 2026-03），按 1-12 月补齐
  const map = new Map(
    pendingMonthlyData.value.map(item => [Number(String(item.yearMonth).split('-')[1]), item])
  )
  const months = Array.from({ length: 12 }, (_, index) => index + 1)

  pendingChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const item = params[0]
        const stat = map.get(Number(item.name.replace('月', '')))
        return `${item.name}<br/>待支出：¥${Number(item.value).toFixed(2)}<br/>笔数：${stat?.count || 0}`
      }
    },
    grid: { left: 70, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: months.map(month => `${month}月`) },
    yAxis: { type: 'value', axisLabel: { formatter: (value) => `¥${value}` } },
    series: [
      {
        type: 'bar',
        itemStyle: { color: '#E6A23C' },
        barMaxWidth: 24,
        data: months.map(month => Number(map.get(month)?.amount || 0))
      }
    ]
  }, true)
}

const handleResize = () => {
  trendChart?.resize()
  compareChart?.resize()
  cumulativeChart?.resize()
  categoryChart?.resize()
  pendingChart?.resize()
}

onMounted(() => {
  selectedYear.value = String(new Date().getFullYear())
  loadAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  compareChart?.dispose()
  cumulativeChart?.dispose()
  categoryChart?.dispose()
  pendingChart?.dispose()
})
</script>

<style scoped>
.yearly-dashboard {
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

.section-card {
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
  font-size: 22px;
  font-weight: bold;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-sub {
  margin-top: 2px;
  font-size: 12px;
  font-weight: normal;
  color: #e6a23c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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

.expense .stat-value,
.expected .stat-value,
.avg .stat-value {
  color: #f56c6c;
}

.pending .stat-value {
  color: #e6a23c;
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

.card-sub {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
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
  height: 360px;
}

.text-income {
  color: #67c23a;
}

.text-expense {
  color: #f56c6c;
}

.text-pending {
  color: #e6a23c;
}

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
