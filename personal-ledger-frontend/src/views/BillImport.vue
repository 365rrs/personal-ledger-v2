<template>
  <div class="bill-import">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账单导入</span>
          <div class="header-actions">
            <el-button type="primary" @click="showUploadDialog">
              <el-icon><Upload /></el-icon>
              上传文件
            </el-button>
            <el-button @click="loadData" :icon="Refresh" :loading="refreshing">
              {{ refreshing ? '刷新中...' : '刷新' }}
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="fileName" label="文件名" width="400" show-overflow-tooltip />

        <el-table-column prop="createTime" label="导入时间" width="180" />

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PROCESSING'" type="info">处理中</el-tag>
            <el-tag v-else-if="row.status === 'SUCCESS'" type="success">成功</el-tag>
            <el-tag v-else-if="row.status === 'FAILED'" type="danger">失败</el-tag>
            <el-tag v-else-if="row.status === 'PARTIAL'" type="warning">部分成功</el-tag>
          </template>
        </el-table-column>

        <!-- 统计信息 -->
        <el-table-column prop="totalCount" label="总数" width="80" align="right" header-cell-class-name="header-total">
          <template #default="{ row }">
            <span class="stat-badge stat-badge-total">{{ row.totalCount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="uniqueCount" label="新增" width="80" align="right" header-cell-class-name="header-success">
          <template #default="{ row }">
            <span class="stat-badge stat-badge-success">{{ row.uniqueCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="duplicateCount" label="重复" width="80" align="right" header-cell-class-name="header-duplicate">
          <template #default="{ row }">
            <span class="stat-badge stat-badge-duplicate">{{ row.duplicateCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="failCount" label="失败" width="80" align="right">
          <template #default="{ row }">
            <span class="stat-badge stat-badge-fail">{{ row.failCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="pendingCount" label="待转换" width="80" align="right">
          <template #default="{ row }">
            <span class="stat-badge stat-badge-pending">{{ row.pendingCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="convertedCount" label="已转换" width="80" align="right">
          <template #default="{ row }">
            <span class="stat-badge stat-badge-converted">{{ row.convertedCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" align="center">
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
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
import { Upload, UploadFilled, Refresh } from '@element-plus/icons-vue'
import { pageRecords, uploadFile, deleteRecord } from '@/api/billImport'

const router = useRouter()

// 查询表单
const queryForm = reactive({
  current: 1,
  size: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 上传对话框
const uploadDialogVisible = ref(false)
const uploadRef = ref()
const uploading = ref(false)
const currentFile = ref(null)
const refreshing = ref(false)

// 加载数据
const loadData = async () => {
  refreshing.value = true
  try {
    const res = await pageRecords(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    refreshing.value = false
  }
}

// 分页变化处理
const handleSizeChange = (size) => {
  queryForm.size = size
  queryForm.current = 1
  loadData()
}

const handleCurrentChange = (current) => {
  queryForm.current = current
  loadData()
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

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.stat-number {
  font-size: 14px;
  font-weight: 600;
}

.stat-number.total {
  color: #409eff;
}

.stat-number.success {
  color: #67c23a;
}

.stat-number.duplicate {
  color: #e6a23c;
}

.stat-number.fail {
  color: #f56c6c;
}

.stat-number.pending {
  color: #909399;
}

.stat-number.converted {
  color: #67c23a;
}

/* 统计徽章样式 */
.stat-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: normal;
  min-width: 20px;
  text-align: center;
}

.stat-badge-total {
  background-color: #e3f2fd;
  color: #1976d2;
}

.stat-badge-success {
  background-color: #e8f5e9;
  color: #388e3c;
}

.stat-badge-duplicate {
  background-color: #fff3e0;
  color: #f57c00;
}

.stat-badge-fail {
  background-color: #ffebee;
  color: #c62828;
}

.stat-badge-pending {
  background-color: #f5f5f5;
  color: #757575;
}

.stat-badge-converted {
  background-color: #e8f5e9;
  color: #2e7d32;
}
</style>
