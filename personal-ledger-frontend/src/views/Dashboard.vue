<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card income">
          <div class="stat-title">总收入</div>
          <div class="stat-value">¥ 0.00</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card expense">
          <div class="stat-title">总支出</div>
          <div class="stat-value">¥ 0.00</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card balance">
          <div class="stat-title">结余</div>
          <div class="stat-value">¥ 0.00</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card count">
          <div class="stat-title">账单数量</div>
          <div class="stat-value">0</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.income .stat-value {
  color: #67c23a;
}

.expense .stat-value {
  color: #f56c6c;
}

.balance .stat-value {
  color: #409eff;
}

.count .stat-value {
  color: #e6a23c;
}
</style>
