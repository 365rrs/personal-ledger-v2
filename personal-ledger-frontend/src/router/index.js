import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
