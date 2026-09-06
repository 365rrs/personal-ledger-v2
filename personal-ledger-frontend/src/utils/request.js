import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,  // 30秒超时，避免第一次导出时超时
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

request.interceptors.response.use(
  response => {
    // 如果是 blob 类型，直接返回数据
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

export default request
