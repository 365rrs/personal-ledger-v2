# Frontend Development Guide

## Overview

Personal Ledger 前端使用 Vue 3 + Vite + Element Plus 构建，提供现代化的单页应用体验。

## Core Technologies

- **Vue 3**: Composition API, `<script setup>` 语法
- **Vite**: 快速的开发服务器和构建工具
- **Element Plus**: 企业级 UI 组件库
- **Vue Router**: 客户端路由
- **Axios**: HTTP 客户端
- **ECharts**: 数据可视化
- **pinyin-pro**: 拼音检索功能

## Project Structure

```
src/
├── api/              # API 服务模块
├── components/       # 可复用组件
├── layout/           # 布局组件
├── router/           # 路由配置
├── styles/           # 全局样式
├── utils/            # 工具函数
├── views/            # 页面组件
├── App.vue           # 根组件
└── main.js           # 入口文件
```

## Development Workflow

### 1. 启动开发服务器

```bash
cd personal-ledger-frontend
npm install
npm run dev
```

访问 http://localhost:3000

### 2. 局域网访问

配置已设置 `host: '0.0.0.0'`，可通过局域网 IP 访问：
```
http://<your-ip>:3000
```

### 3. 构建生产版本

```bash
npm run build
```

生成的文件在 `dist/` 目录

## API Integration

### API 模块结构

每个功能模块对应一个 API 文件：

```javascript
// src/api/bill.js
import request from '@/utils/request'

export function pageBills(data) {
  return request({
    url: '/bill/page',
    method: 'post',
    data
  })
}

export function getBill(id) {
  return request({
    url: '/bill',
    method: 'get',
    params: { id }
  })
}
```

### Request 配置

`src/utils/request.js` 配置了 Axios 实例：

```javascript
const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(config => {
  // 添加 token 等
  return config
})

// 响应拦截器
service.interceptors.response.use(
  response => response.data,
  error => {
    // 统一错误处理
    return Promise.reject(error)
  }
)
```

### API 代理配置

`vite.config.js` 中配置了开发环境代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8081',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## Component Development

### 使用 Composition API

```vue
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 响应式数据
const count = ref(0)
const form = reactive({
  name: '',
  age: 0
})

// 计算属性
const doubleCount = computed(() => count.value * 2)

// 生命周期
onMounted(() => {
  console.log('Component mounted')
})

// 方法
const increment = () => {
  count.value++
}
