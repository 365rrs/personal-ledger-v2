<template>
  <el-dialog
    v-model="visible"
    title="待支出项目详情"
    width="640px"
  >
    <el-descriptions v-loading="loading" :column="2" border>
      <el-descriptions-item label="项目名称" :span="2">
        {{ detail.expenseName || '-' }}
      </el-descriptions-item>

      <el-descriptions-item label="金额">
        <span style="color: #F56C6C; font-weight: 500;">
          ¥{{ formatAmount(detail.amount) }}
        </span>
      </el-descriptions-item>

      <el-descriptions-item label="支付日期">
        {{ detail.paymentDate || '-' }}
      </el-descriptions-item>

      <el-descriptions-item label="周期">
        <el-tag v-if="detail.period" :type="getPeriodTagType(detail.period)" size="small">
          {{ detail.periodName }}
        </el-tag>
        <span v-else>-</span>
      </el-descriptions-item>

      <el-descriptions-item label="计划类型">
        <el-tag v-if="detail.planType" :type="getPlanTypeTagType(detail.planType)" size="small">
          {{ detail.planTypeName }}
        </el-tag>
        <span v-else>-</span>
      </el-descriptions-item>

      <el-descriptions-item label="状态">
        <el-tag v-if="detail.status" :type="getStatusTagType(detail.status)" size="small">
          {{ detail.statusName }}
        </el-tag>
        <span v-else>-</span>
      </el-descriptions-item>

      <el-descriptions-item label="分类">
        {{ detail.categoryName || '-' }}
      </el-descriptions-item>

      <el-descriptions-item label="备注" :span="2">
        {{ detail.remark || '-' }}
      </el-descriptions-item>

      <el-descriptions-item label="创建时间">
        {{ formatDateTime(detail.createTime) }}
      </el-descriptions-item>

      <el-descriptions-item label="更新时间">
        {{ formatDateTime(detail.updateTime) }}
      </el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" :icon="Edit" @click="handleToEdit">编辑</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { getPendingExpense } from '@/api/pendingExpense'

const props = defineProps({
  modelValue: Boolean,
  detailId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'edit'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const detail = ref({})

const loadDetail = async () => {
  if (!props.detailId) {
    return
  }
  loading.value = true
  try {
    const res = await getPendingExpense(props.detailId)
    detail.value = res.data || {}
  } catch (error) {
    console.error('加载项目详情失败:', error)
    ElMessage.error('加载项目详情失败')
  } finally {
    loading.value = false
  }
}

const handleToEdit = () => {
  visible.value = false
  emit('edit', props.detailId)
}

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 19)
}

const getStatusTagType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return typeMap[status] || ''
}

const getPeriodTagType = (period) => {
  const typeMap = {
    MONTHLY: 'primary',
    YEARLY: 'success',
    ONETIME: 'info'
  }
  return typeMap[period] || ''
}

const getPlanTypeTagType = (planType) => {
  const typeMap = {
    RIGID: 'danger',
    INTENDED: 'warning'
  }
  return typeMap[planType] || ''
}

watch(visible, (val) => {
  if (val) {
    detail.value = {}
    loadDetail()
  }
})
</script>
