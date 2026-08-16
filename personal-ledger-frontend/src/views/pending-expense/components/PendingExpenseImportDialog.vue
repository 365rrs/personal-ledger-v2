<template>
  <el-dialog
    v-model="visible"
    title="导入待支出项目"
    width="600px"
    @close="handleClose"
  >
    <div v-if="!importing && !importResult">
      <el-alert
        title="导入说明"
        type="info"
        :closable="false"
        show-icon
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li>支持 .xlsx 和 .xls 格式的 Excel 文件</li>
          <li>请确保文件包含以下列：项目名称、金额、支付日期、周期、计划类型</li>
          <li>周期可选值：MONTHLY（每月）、YEARLY（每年）、ONETIME（一次性）</li>
          <li>计划类型可选值：RIGID（刚性支出）、INTENDED（意向计划支出）</li>
          <li>支付日期格式：YYYY-MM-DD（如：2025-01-15）</li>
          <li>可选列：状态、分类名称、支付渠道名称、备注</li>
        </ul>
      </el-alert>
      
      <el-upload
        ref="uploadRef"
        class="upload-container"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 .xlsx 或 .xls 文件，大小不超过 10MB
          </div>
        </template>
      </el-upload>
    </div>
    
    <div v-if="importing" style="text-align: center; padding: 40px 0;">
      <el-icon class="is-loading" :size="50" color="#409EFF">
        <Loading />
      </el-icon>
      <div style="margin-top: 20px; color: #606266; font-size: 16px; font-weight: 500;">
        正在导入数据，请稍候...
      </div>
      <div v-if="uploadProgress > 0" style="margin-top: 15px; padding: 0 40px;">
        <el-progress 
          :percentage="uploadProgress" 
          :stroke-width="8"
          :color="uploadProgress === 100 ? '#67C23A' : '#409EFF'"
        />
      </div>
    </div>
    
    <div v-if="importResult">
      <el-result
        :icon="importResult.failCount === 0 ? 'success' : 'warning'"
        :title="importResult.failCount === 0 ? '导入成功' : '导入完成（部分失败）'"
      >
        <template #sub-title>
          <div style="margin-bottom: 10px;">
            <span style="color: #67C23A; font-weight: bold; font-size: 16px;">
              成功: {{ importResult.successCount }} 条
            </span>
            <span v-if="importResult.failCount > 0" style="margin-left: 20px; color: #F56C6C; font-weight: bold; font-size: 16px;">
              失败: {{ importResult.failCount }} 条
            </span>
          </div>
          <div style="color: #909399; font-size: 14px;">
            总计: {{ importResult.totalCount }} 条记录
          </div>
        </template>
        <template #extra>
          <el-button type="primary" @click="handleCloseResult">完成</el-button>
        </template>
      </el-result>
      
      <div v-if="importResult.errors.length > 0" style="margin-top: 20px;">
        <el-divider>错误详情</el-divider>
        <el-scrollbar max-height="300px">
          <el-alert
            v-for="(error, index) in importResult.errors"
            :key="index"
            :title="error"
            type="error"
            :closable="false"
            style="margin-bottom: 10px;"
          />
        </el-scrollbar>
      </div>
    </div>
    
    <template #footer v-if="!importing && !importResult">
      <el-button @click="visible = false">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleUpload" 
        :disabled="!selectedFile"
      >
        开始导入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Loading } from '@element-plus/icons-vue'
import { importFromExcel } from '@/api/pendingExpense'
import * as XLSX from 'xlsx'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const uploadRef = ref()
const selectedFile = ref(null)
const importing = ref(false)
const importResult = ref(null)
const uploadProgress = ref(0)
const totalRows = ref(0)

// 读取 Excel 文件获取总行数
const getExcelRowCount = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result)
        const workbook = XLSX.read(data, { type: 'array' })
        const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
        const jsonData = XLSX.utils.sheet_to_json(firstSheet, { header: 1 })
        // 减去标题行
        const rowCount = Math.max(0, jsonData.length - 1)
        resolve(rowCount)
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = reject
    reader.readAsArrayBuffer(file)
  })
}

// 文件选择处理
const handleFileChange = async (file) => {
  const fileName = file.name
  const fileExt = fileName.substring(fileName.lastIndexOf('.')).toLowerCase()
  
  if (!['.xlsx', '.xls'].includes(fileExt)) {
    ElMessage.error('只支持 .xlsx 和 .xls 格式的文件')
    uploadRef.value.clearFiles()
    selectedFile.value = null
    totalRows.value = 0
    return
  }
  
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 10MB')
    uploadRef.value.clearFiles()
    selectedFile.value = null
    totalRows.value = 0
    return
  }
  
  try {
    // 读取文件获取总行数
    totalRows.value = await getExcelRowCount(file.raw)
    selectedFile.value = file.raw
  } catch (error) {
    console.error('读取文件失败:', error)
    ElMessage.error('读取文件失败，请确认文件格式正确')
    uploadRef.value.clearFiles()
    selectedFile.value = null
    totalRows.value = 0
  }
}

// 开始上传
const handleUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }
  
  try {
    importing.value = true
    uploadProgress.value = 0
    
    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 200)
    
    const result = await importFromExcel(selectedFile.value)
    
    // 停止进度模拟
    clearInterval(progressInterval)
    uploadProgress.value = 100
    
    // 短暂延迟让用户看到 100%
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 解析结果 - 后端返回的是错误信息列表
    const errors = result.data || []
    const failCount = errors.length
    const successCount = Math.max(0, totalRows.value - failCount)
    
    importResult.value = {
      successCount: successCount,
      failCount: failCount,
      totalCount: totalRows.value,
      errors: errors
    }
    
    if (errors.length === 0) {
      ElMessage.success(`导入成功，共导入 ${successCount} 条记录`)
      emit('success')
    } else {
      ElMessage.warning(`导入完成，成功 ${successCount} 条，失败 ${failCount} 条`)
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error(error.message || '导入失败，请检查文件格式和内容')
    importing.value = false
    uploadProgress.value = 0
  } finally {
    importing.value = false
  }
}

// 关闭结果并刷新
const handleCloseResult = () => {
  visible.value = false
  if (importResult.value && importResult.value.errors.length === 0) {
    emit('success')
  }
}

// 关闭对话框
const handleClose = () => {
  if (!importing.value) {
    selectedFile.value = null
    importResult.value = null
    uploadProgress.value = 0
    totalRows.value = 0
    uploadRef.value?.clearFiles()
  }
}
</script>

<style scoped>
.upload-container {
  margin-top: 20px;
}

.el-icon--upload {
  font-size: 67px;
  color: #C0C4CC;
  margin: 40px 0 16px;
  line-height: 50px;
}

.el-upload__text {
  font-size: 14px;
  color: #606266;
}

.el-upload__text em {
  color: #409EFF;
  font-style: normal;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 7px;
}

.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.el-scrollbar {
  padding: 10px;
  background-color: #F5F7FA;
  border-radius: 4px;
}
</style>
