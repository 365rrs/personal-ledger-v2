<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="项目名称" prop="expenseName">
        <el-input 
          v-model="form.expenseName" 
          placeholder="请输入项目名称" 
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
      
      <el-form-item label="支付日期" prop="paymentDate">
        <el-date-picker
          v-model="form.paymentDate"
          type="date"
          placeholder="选择支付日期"
          value-format="YYYY-MM-DD"
          style="width: 100%;"
        />
      </el-form-item>
      
      <el-form-item label="周期" prop="period">
        <el-radio-group v-model="form.period">
          <el-radio label="MONTHLY">每月</el-radio>
          <el-radio label="YEARLY">每年</el-radio>
          <el-radio label="ONETIME">一次性</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="计划类型" prop="planType">
        <el-radio-group v-model="form.planType">
          <el-radio label="RIGID">刚性支出</el-radio>
          <el-radio label="INTENDED">意向计划支出</el-radio>
        </el-radio-group>
        <div style="margin-top: 5px; font-size: 12px; color: #909399;">
          <span v-if="form.planType === 'RIGID'">必须要支付的项目（如房租、水电费、手机套餐等）</span>
          <span v-else-if="form.planType === 'INTENDED'">计划购买但不是必须的项目（如电动车、摩托车等）</span>
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
    </el-form>
    
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ mode === 'create' ? '创建' : '保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createPendingExpense, updatePendingExpense, getPendingExpense } from '@/api/pendingExpense'
import { getCategoryList } from '@/api/category'

const props = defineProps({
  modelValue: Boolean,
  mode: {
    type: String,
    default: 'create', // 'create' or 'edit'
    validator: (value) => ['create', 'edit'].includes(value)
  },
  editId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const dialogTitle = computed(() => {
  return props.mode === 'create' ? '创建待支出项目' : '编辑待支出项目'
})

const formRef = ref()
const submitting = ref(false)
const categoryList = ref([])

const form = reactive({
  expenseName: '',
  amount: null,
  paymentDate: '',
  period: 'MONTHLY',
  planType: 'RIGID',
  categoryId: null,
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
  paymentDate: [
    { required: true, message: '请选择支付日期', trigger: 'change' }
  ],
  period: [
    { required: true, message: '请选择周期', trigger: 'change' }
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' }
  ]
}

// 加载分类数据
const loadData = async () => {
  try {
    const catRes = await getCategoryList('EXPENSE', '1') // 只获取支出类分类且已启用的
    
    // 扁平化分类列表（包括父分类和子分类）
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
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 加载编辑数据
const loadEditData = async () => {
  if (props.mode === 'edit' && props.editId) {
    try {
      const res = await getPendingExpense(props.editId)
      Object.assign(form, {
        expenseName: res.data.expenseName,
        amount: res.data.amount,
        paymentDate: res.data.paymentDate,
        period: res.data.period,
        planType: res.data.planType,
        categoryId: res.data.categoryId,
        remark: res.data.remark || ''
      })
    } catch (error) {
      console.error('加载项目详情失败:', error)
      ElMessage.error('加载项目详情失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    if (props.mode === 'create') {
      await createPendingExpense(form)
      ElMessage.success('创建成功')
    } else {
      await updatePendingExpense(props.editId, form)
      ElMessage.success('更新成功')
    }
    
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
    paymentDate: '',
    period: 'MONTHLY',
    planType: 'RIGID',
    categoryId: null,
    remark: ''
  })
}

// 监听对话框打开
watch(visible, (val) => {
  if (val) {
    loadData()
    if (props.mode === 'edit') {
      loadEditData()
    }
  }
})
</script>

<style scoped>
.el-input-number {
  width: 100%;
}
</style>
