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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
