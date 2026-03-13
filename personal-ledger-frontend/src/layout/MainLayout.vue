<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">个人账本</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-sub-menu index="bill">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>账单管理</span>
          </template>
          <el-menu-item index="/bill">账单列表</el-menu-item>
          <el-menu-item index="/bill-import">账单导入</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="statistics">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>统计分析</span>
          </template>
          <el-menu-item index="/daily-expense">每日支出</el-menu-item>
          <el-menu-item index="/cumulative-expense">累计支出</el-menu-item>
          <el-menu-item index="/monthly-expense">月度支出</el-menu-item>
          <el-menu-item index="/monthly-cumulative-expense">月度累计支出</el-menu-item>
          <el-menu-item index="/category-statistics">分类统计</el-menu-item>
          <el-menu-item index="/large-expense">大额支出</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/category">分类管理</el-menu-item>
          <el-menu-item index="/tag">标签管理</el-menu-item>
          <el-menu-item index="/payment-channel">支付渠道</el-menu-item>
          <el-menu-item index="/data-clean-rule">清洗规则</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, Tickets, DataAnalysis, Setting } from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/dashboard': '首页',
    '/bill': '账单列表',
    '/bill-import': '账单导入',
    '/category': '分类管理',
    '/tag': '标签管理',
    '/payment-channel': '支付渠道',
    '/data-clean-rule': '清洗规则',
    '/daily-expense': '每日支出',
    '/cumulative-expense': '累计支出',
    '/monthly-expense': '月度支出',
    '/monthly-cumulative-expense': '月度累计支出',
    '/category-statistics': '分类统计',
    '/large-expense': '大额支出'
  }
  return titles[route.path] || '个人账本系统'
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  background-color: #2b3a4a;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
