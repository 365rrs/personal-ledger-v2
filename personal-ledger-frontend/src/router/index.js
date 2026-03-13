import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/bill',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/Category.vue')
      },
      {
        path: 'tag',
        name: 'TagManagement',
        component: () => import('../views/TagManagement.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/Statistics.vue')
      },
      {
        path: 'bill-import',
        name: 'BillImport',
        component: () => import('../views/BillImport.vue')
      },
      {
        path: 'bill-import/detail/:id',
        name: 'BillImportDetail',
        component: () => import('../views/BillImportDetail.vue')
      },
      {
        path: 'bill',
        name: 'BillList',
        component: () => import('../views/BillList.vue')
      },
      {
        path: 'payment-channel',
        name: 'PaymentChannel',
        component: () => import('../views/PaymentChannel.vue')
      },
      {
        path: 'data-clean-rule',
        name: 'DataCleanRule',
        component: () => import('../views/DataCleanRule.vue')
      },
      {
        path: 'daily-expense',
        name: 'DailyExpense',
        component: () => import('../views/DailyExpense.vue')
      },
      {
        path: 'cumulative-expense',
        name: 'CumulativeExpense',
        component: () => import('../views/CumulativeExpense.vue')
      },
      {
        path: 'category-statistics',
        name: 'CategoryStatistics',
        component: () => import('../views/CategoryStatistics.vue')
      },
      {
        path: 'large-expense',
        name: 'LargeExpense',
        component: () => import('../views/LargeExpense.vue')
      },
      {
        path: 'monthly-expense',
        name: 'MonthlyExpense',
        component: () => import('../views/MonthlyExpense.vue')
      },
      {
        path: 'monthly-cumulative-expense',
        name: 'MonthlyCumulativeExpense',
        component: () => import('../views/MonthlyCumulativeExpense.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
