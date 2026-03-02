<template>
  <div class="bill-import">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账单导入</span>
          <el-button type="primary" @click="showUploadDialog">
            <el-icon><Upload /></el-icon>
            上传文件
          </el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable>
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
            <el-option label="部分成功" value="PARTIAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件名">
          <el-input v-model="queryForm.fileName" placeholder="请输入文件名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe>
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="createTime" label="导入时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PROCESSING'" type="info">处理中</el-tag>
            <el-tag v-else-if="row.status === 'SUCCESS'" type="success">成功</el-tag>
            <el-tag v-else-if="row.status === 'FAILED'" type="danger">失败</el-tag>
            <el-tag v-else-if="row.status === 'PARTIAL'" type="warning">部分成功</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总数" width="80" />
        <el-table-column prop="successCount" label="成功" width="80" />
        <el-table-column prop="failCount" label="失败" width="80" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传账单文件" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        accept=".xlsx,.xls,.csv"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .xlsx、.xls、.csv 格式，文件大小不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import { pageRecords, uploadFile, deleteRecord } from '@/api/billImport'

const router = useRouter()

// 查询表单
const queryForm = reactive({
  current: 1,
  size: 10,
  status: '',
  fileName: ''
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 上传对话框
const uploadDialogVisible = ref(false)
const uploadRef = ref()
const uploading = ref(false)
const currentFile = ref(null)

// 查询数据
const loadData = async () => {
  try {
    const res = await pageRecords(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

// 查询
const handleQuery = () => {
  queryForm.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.status = ''
  queryForm.fileName = ''
  handleQuery()
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadDialogVisible.value = true
  currentFile.value = null
}

// 文件选择
const handleFileChange = (file) => {
  currentFile.value = file.raw
}

// 文件超出限制
const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

// 上传文件
const handleUpload = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  try {
    const res = await uploadFile(currentFile.value)
    ElMessage.success('导入成功')
    uploadDialogVisible.value = false
    uploadRef.value.clearFiles()
    loadData()
    
    // 跳转到详情页
    router.push(`/bill-import/detail/${res.data}`)
  } catch (error) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    uploading.value = false
  }
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/bill-import/detail/${row.id}`)
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该导入记录吗？', '提示', {
      type: 'warning'
    })
    
    await deleteRecord(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.bill-import {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
