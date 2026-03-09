<template>
  <div class="ledger">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账单列表</span>
          <el-button type="primary">新增账单</el-button>
        </div>
      </template>
      <el-table :data="[]">
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="transactionTime" label="交易时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default>
            <el-button type="primary" size="small">编辑</el-button>
            <el-button type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
