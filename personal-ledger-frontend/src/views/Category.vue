<template>
  <div class="category">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类列表</span>
          <el-button type="primary">新增分类</el-button>
        </div>
      </template>
      <el-table :data="[]" stripe>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="type" label="类型" width="120" />
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
