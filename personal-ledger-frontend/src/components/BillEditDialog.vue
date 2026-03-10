<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="金额类型" prop="amountType">
        <el-radio-group v-model="form.amountType" :disabled="form.manualEntry === '0'">
          <el-radio label="INCOME">收入</el-radio>
          <el-radio label="EXPENSE">支出</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="金额" prop="amount">
        <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="1" :disabled="form.manualEntry === '0'" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select 
          v-model="form.categoryId" 
          placeholder="请选择分类" 
          filterable
          clearable
          @change="handleCategoryChange"
          style="width: 100%;"
        >
          <el-option
            v-for="cat in categoryList"
            :key="cat.id"
            :label="cat.categoryName"
            :value="cat.id"
          >
            <span>{{ cat.categoryName }}</span>
            <el-tag size="small" :type="cat.categoryType === 'EXPENSE' ? 'danger' : 'success'" style="margin-left: 8px;">
              {{ cat.categoryType === 'EXPENSE' ? '支出' : '收入' }}
            </el-tag>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="二级分类">
        <el-select 
          v-model="form.subCategoryId" 
          placeholder="请选择二级分类" 
          filterable
          clearable
          style="width: 100%;"
        >
          <el-option
            v-for="subCat in subCategoryList"
            :key="subCat.id"
            :label="subCat.categoryName"
            :value="subCat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="交易日期" prop="transactionDate">
        <el-date-picker
          v-model="form.transactionDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          :disabled="form.manualEntry === '0'"
        />
      </el-form-item>
      <el-form-item label="交易时间">
        <el-time-picker
          v-model="form.transactionTime"
          placeholder="选择时间"
          value-format="HH:mm:ss"
          :disabled="form.manualEntry === '0'"
        />
      </el-form-item>
      <el-form-item label="交易类型">
        <el-input v-model="form.transactionType" placeholder="请输入交易类型" :disabled="form.manualEntry === '0'" />
      </el-form-item>
      <el-form-item label="支付渠道">
        <el-select 
          v-model="form.paymentChannel" 
          placeholder="请选择支付渠道" 
          filterable
          clearable
          style="width: 100%;"
        >
          <el-option
            v-for="channel in paymentChannelList"
            :key="channel.id"
            :label="channel.channelName"
            :value="channel.channelName"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="交易描述">
        <el-input v-model="form.transactionDesc" type="textarea" :rows="3" :disabled="form.manualEntry === '0'" />
      </el-form-item>
      <el-form-item label="手工备注">
        <el-input v-model="form.manualRemark" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="是否计入收支">
        <el-switch v-model="form.includeInStatistics" active-value="1" inactive-value="0" />
      </el-form-item>
      <el-form-item label="标签">
        <el-select 
          v-model="form.tagIds" 
          placeholder="请选择标签" 
          multiple 
          filterable
          style="width: 100%;"
        >
          <el-option
            v-for="tag in tagList"
            :key="tag.id"
            :label="tag.tagName"
            :value="tag.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateBill } from '@/api/bill'
import { getCategoryList } from '@/api/category'
import { listPaymentChannels } from '@/api/paymentChannel'
import { getTagList } from '@/api/tag'

const props = defineProps({
  modelValue: Boolean,
  billData: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const title = ref('编辑账单')
const formRef = ref()
const submitting = ref(false)
const categoryList = ref([])
const paymentChannelList = ref([])
const tagList = ref([])

const form = reactive({
  id: null,
  amountType: '',
  amount: null,
  categoryId: null,
  subCategoryId: null,
  transactionDate: '',
  transactionTime: '',
  transactionType: '',
  paymentChannel: '',
  paymentChannelId: null,
  transactionDesc: '',
  manualRemark: '',
  includeInStatistics: '1',
  tagIds: [],
  manualEntry: '0'
})

const rules = {
  amountType: [{ required: true, message: '请选择金额类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  transactionDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }]
}

const subCategoryList = computed(() => {
  if (!form.categoryId) return []
  const parent = categoryList.value.find(cat => cat.id === form.categoryId)
  return parent && parent.children ? parent.children : []
})

const handleCategoryChange = () => {
  form.subCategoryId = null
}

const loadData = async () => {
  try {
    const [catRes, channelRes, tagRes] = await Promise.all([
      getCategoryList('', ''),
      listPaymentChannels({ size: 100 }),
      getTagList({ size: 100 })
    ])
    categoryList.value = (catRes.data || []).filter(cat => !cat.parentId)
    paymentChannelList.value = (channelRes.data || []).sort((a, b) => a.sortOrder - b.sortOrder)
    tagList.value = (tagRes.data.records || []).filter(tag => tag.tagStatus === 'enable')
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    let categoryName = ''
    let subCategoryName = ''
    
    if (form.categoryId) {
      const parent = categoryList.value.find(cat => cat.id === form.categoryId)
      if (parent) categoryName = parent.categoryName
    }
    
    if (form.subCategoryId) {
      const parent = categoryList.value.find(cat => cat.id === form.categoryId)
      if (parent && parent.children) {
        const subCat = parent.children.find(sub => sub.id === form.subCategoryId)
        if (subCat) subCategoryName = subCat.categoryName
      }
    }
    
    const channel = paymentChannelList.value.find(c => c.channelName === form.paymentChannel)
    
    const data = {
      id: form.id,
      amountType: form.amountType,
      transactionType: form.transactionType,
      transactionDate: form.transactionDate,
      transactionTime: form.transactionTime,
      categoryId: form.categoryId,
      category: categoryName,
      subCategoryId: form.subCategoryId,
      subCategory: subCategoryName,
      paymentChannel: form.paymentChannel,
      paymentChannelId: channel ? channel.id : null,
      transactionDesc: form.transactionDesc,
      manualRemark: form.manualRemark,
      includeInStatistics: form.includeInStatistics,
      manualEntry: form.manualEntry,
      tagIds: form.tagIds
    }
    
    if (form.amountType === 'INCOME') {
      data.incomeAmount = form.amount
    } else {
      data.expenseAmount = form.amount
    }
    
    submitting.value = true
    await updateBill(data)
    ElMessage.success('更新成功')
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

const handleClose = () => {
  formRef.value?.resetFields()
}

watch(() => props.billData, (data) => {
  if (data) {
    Object.assign(form, {
      id: data.id,
      amountType: data.amountType,
      amount: data.amountType === 'INCOME' ? data.incomeAmount : data.expenseAmount,
      categoryId: data.categoryId,
      subCategoryId: data.subCategoryId,
      transactionDate: data.transactionDate,
      transactionTime: data.transactionTime,
      transactionType: data.transactionType,
      paymentChannel: data.paymentChannel,
      paymentChannelId: data.paymentChannelId || null,
      transactionDesc: data.transactionDesc,
      manualRemark: data.manualRemark,
      includeInStatistics: data.includeInStatistics,
      tagIds: data.tagIds || [],
      manualEntry: data.manualEntry || '0'
    })
  }
}, { immediate: true })

watch(visible, (val) => {
  if (val) loadData()
})
</script>
