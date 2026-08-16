<template>
  <el-dialog
    v-model="visible"
    title="批量创建周期性支出"
    width="700px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="项目名称" prop="expenseName">
        <el-input 
          v-model="form.expenseName" 
          placeholder="请输入项目名称（如：房租、水电费）" 
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="金额" prop="amount">
        <el-input-number 
          v-model="form.amount" 
          :min="0.01" 
          :max="999999.99"
          :precision="2" 
          :step="100"
          style="width: 100%;"
        />
      </el-form-item>
      
      <el-form-item label="周期" prop="period">
        <el-radio-group v-model="form.period">
          <el-radio label="MONTHLY">每月</el-radio>
          <el-radio label="YEARLY">每年</el-radio>
        </el-radio-group>
        <div style="margin-top: 5px; font-size: 12px; color: #909399;">
          选择周期后，系统将根据您选择的月份和日期批量创建待支出记录
        </div>
      </el-form-item>
      
      <el-form-item label="计划类型" prop="planType">
        <el-radio-group v-model="form.planType">
          <el-radio label="RIGID">刚性支出</el-radio>
          <el-radio label="INTENDED">意向计划支出</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="年份" prop="year">
        <el-input-number 
          v-model="form.year" 
          :min="1900" 
          :max="9999"
          :step="1"
          controls-position="right"
          style="width: 200px;"
        />
      </el-form-item>
      
      <el-form-item label="选择月份" prop="months">
        <el-checkbox-group v-model="form.months" @change="updateEstimate">
          <el-checkbox v-for="month in 12" :key="month" :label="month">
            {{ month }}月
          </el-checkbox>
        </el-checkbox-group>
        <div style="margin-top: 5px; font-size: 12px; color: #909399;">
          可多选，已选 {{ form.months.length }} 个月份
        </div>
      </el-form-item>
      
      <el-form-item label="选择日期" prop="days">
        <el-checkbox-group v-model="form.days" @change="updateEstimate">
          <el-checkbox v-for="day in 31" :key="day" :label="day">
            {{ day }}日
          </el-checkbox>
        </el-checkbox-group>
        <div style="margin-top: 5px; font-size: 12px; color: #909399;">
          可多选，已选 {{ form.days.length }} 个日期
        </div>
      </el-form-item>
      
      <el-form-item label="分类">
        <el-select 
          v-model="form.categoryId" 
          placeholder="请选择分类" 
          filterable
          clearable
          style="width: 100%;"
        >
          <el-option
            v-for="cat in categoryList"
            :key="cat.id"
            :label="cat.categoryName"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="支付渠道">
        <el-select 
          v-model="form.paymentChannelId" 
          placeholder="请选择支付渠道" 
          filterable
          clearable
          style="width: 100%;"
        >
          <el-option
            v-for="channel in paymentChannelList"
            :key="channel.id"
            :label="channel.channelName"
            :value="channel.id"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input 
          v-model="form.remark" 
          type="textarea" 
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="请输入备注信息"
        />
      </el-form-item>
      
      <el-alert 
        v-if="estimatedCount > 0" 
        :title="`预计创建 ${estimatedCount} 条待支出记录（${form.months.length} 个月份 × ${form.days.length} 个日期）`" 
        type="info" 
        :closable="false"
        show-icon
      />
    </el-form>
    
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleSubmit" 
        :loading="submitting"
        :disabled="estimatedCount === 0"
      >
        批量创建
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createRecurringExpense } from '@/api/pendingExpense'
import { getCategoryList } from '@/api/category'
import { listPaymentChannels } from '@/api/paymentChannel'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref()
const submitting = ref(false)
const categoryList = ref([])
const paymentChannelList = ref([])
const estimatedCount = ref(0)

const form = reactive({
  expenseName: '',
  amount: null,
  period: 'MONTHLY',
  planType: 'RIGID',
  year: new Date().getFullYear(),
  months: [],
  days: [],
  categoryId: null,
  paymentChannelId: null,
  remark: ''
})

const rules = {
  expenseName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 100, message: '项目名称长度不能超过100字符', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' },
    { type: 'number', max: 999999.99, message: '金额不能超过999999.99', trigger: 'blur' }
  ],
  period: [
    { required: true, message: '请选择周期', trigger: 'change' }
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' }
  ],
  year: [
    { required: true, message: '请输入年份', trigger: 'blur' },
    { type: 'number', min: 1900, message: '年份不能早于1900年', trigger: 'blur' },
    { type: 'number', max: 9999, message: '年份不能晚于9999年', trigger: 'blur' }
  ],
  months: [
    { type: 'array', required: true, message: '请至少选择一个月份', trigger: 'change' }
  ],
  days: [
    { type: 'array', required: true, message: '请至少选择一个日期', trigger: 'change' }
  ]
}

// 更新预计创建数量
const updateEstimate = () => {
  estimatedCount.value = form.months.length * form.days.length
}

// 加载分类和支付渠道数据
const loadData = async () => {
  try {
    const [catRes, channelRes] = await Promise.all([
      getCategoryList('EXPENSE', '1'),
      listPaymentChannels({ size: 100 })
    ])
    
    // 扁平化分类列表
    const flattenCategories = (categories) => {
      const result = []
      categories.forEach(cat => {
        if (cat.parentId === null || cat.parentId === undefined) {
          result.push(cat)
        }
        if (cat.children && cat.children.length > 0) {
          result.push(...cat.children)
        }
      })
      return result
    }
    
    categoryList.value = flattenCategories(catRes.data || [])
    paymentChannelList.value = (channelRes.data || [])
      .filter(c => c.enabled === '1')
      .sort((a, b) => a.sortOrder - b.sortOrder)
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 二次确认
    await ElMessageBox.confirm(
      `确认要创建 ${estimatedCount.value} 条待支出记录吗？`,
      '确认批量创建',
      {
        confirmButtonText: '确认创建',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    submitting.value = true
    
    const result = await createRecurringExpense(form)
    
    ElMessage.success(`批量创建成功，共创建 ${result.data.length} 条记录`)
    
    visible.value = false
    emit('success')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    expenseName: '',
    amount: null,
    period: 'MONTHLY',
    planType: 'RIGID',
    year: new Date().getFullYear(),
    months: [],
    days: [],
    categoryId: null,
    paymentChannelId: null,
    remark: ''
  })
  estimatedCount.value = 0
}

// 监听对话框打开
watch(visible, (val) => {
  if (val) {
    loadData()
  }
})
</script>

<style scoped>
.el-checkbox {
  margin-right: 12px;
  margin-bottom: 8px;
}

.el-alert {
  margin-top: 10px;
}
</style>
