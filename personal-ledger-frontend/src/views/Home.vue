<template>
  <div class="home">
    <el-container>
      <el-header>
        <h1>个人账本系统</h1>
      </el-header>
      <el-main>
        <el-card>
          <h2>欢迎使用个人账本系统</h2>
          <p>记录您的每一笔收支</p>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home {
  width: 100%;
  height: 100%;
}

.el-header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.el-main {
  padding: 20px;
}
</style>
