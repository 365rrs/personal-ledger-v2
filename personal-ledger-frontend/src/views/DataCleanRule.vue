<template>
  <div class="data-clean-rule">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据清洗规则管理</span>
          <el-button type="primary" @click="handleCreate">+ 新建规则</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="规则类型">
          <el-select v-model="queryForm.ruleType" placeholder="全部" clearable style="width: 120px">
            <el-option label="支付渠道" value="PAYMENT_CHANNEL" />
            <el-option label="分类" value="CATEGORY" />
            <el-option label="用户备注" value="MANUAL_REMARK" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-select v-model="queryForm.enabled" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input 
            v-model="queryForm.keywords" 
            placeholder="搜索目标值或备注" 
            clearable 
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <div class="table-area">
        <el-table 
          :data="tableData" 
          stripe 
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="ruleTypeName" label="规则类型" width="120" />
          <el-table-column label="匹配条件" min-width="250">
            <template #default="{ row }">
              <div v-if="row.matchFields && row.matchFields !== '{}'">
                <div 
                  v-for="(field, index) in displayMatchFields(row.matchFields)" 
                  :key="index"
                  class="match-field-item"
                >
                  <el-tag 
                    size="small"
                    type="info"
                    style="margin-right: 4px; margin-bottom: 4px;"
                  >
                    {{ field.fieldLabel }}
                  </el-tag>
                  <el-tag 
                    size="small"
                    style="margin-right: 4px; margin-bottom: 4px;"
                  >
                    {{ field.matchModeLabel }}
                  </el-tag>
                  <span style="color: #666;">{{ field.value }}</span>
                </div>
              </div>
              <span v-else style="color: #999;">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="targetValue" label="目标值" width="150" show-overflow-tooltip />
          <el-table-column prop="priority" label="优先级" width="80" align="center" />
          <el-table-column prop="enabled" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled === '1' ? 'success' : 'info'">
                {{ row.enabled === '1' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注说明" width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
          style="margin-top: 20px; justify-content: flex-end;"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditMode ? '编辑清洗规则' : '新建清洗规则'"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="规则类型" prop="ruleType">
          <el-select 
            v-model="formData.ruleType" 
            placeholder="请选择规则类型"
            style="width: 100%"
            @change="handleRuleTypeChange"
          >
            <el-option label="支付渠道" value="PAYMENT_CHANNEL" />
            <el-option label="分类" value="CATEGORY" />
            <el-option label="用户备注" value="MANUAL_REMARK" />
          </el-select>
        </el-form-item>

        <el-form-item label="匹配条件" prop="matchFields">
          <div class="match-fields-editor">
            <div v-for="(field, index) in matchFieldsList" :key="index" class="field-row">
              <el-select 
                v-model="field.name" 
                placeholder="选择字段"
                style="width: 180px; margin-right: 8px;"
              >
                <el-option
                  v-for="opt in availableFieldOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
              <el-select 
                v-model="field.matchMode" 
                placeholder="匹配模式"
                style="width: 100px; margin-right: 8px;"
              >
                <el-option label="精确" value="exact" />
                <el-option label="包含" value="contains" />
                <el-option label="正则" value="regex" />
              </el-select>
              <el-input 
                v-model="field.value" 
                placeholder="字段值"
                style="flex: 1; margin-right: 8px;"
              />
              <el-button 
                type="danger" 
                size="small" 
                @click="removeField(index)"
              >
                删除
              </el-button>
            </div>
            <el-button type="primary" plain @click="addField">
              + 添加匹配字段
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="目标值" prop="targetValue">
          <el-input 
            v-model="formData.targetValue" 
            placeholder="请输入目标值"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-input-number 
            v-model="formData.priority" 
            :min="0" 
            :max="999"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="是否启用" prop="enabled">
          <el-radio-group v-model="formData.enabled">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
          <el-input 
            v-model="formData.remark" 
            type="textarea"
            :rows="3"
            placeholder="请输入备注说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRules, getRuleDetail, createRule, updateRule, deleteRule } from '@/api/dataCleanRule'

// 规则类型对应的可配置字段（使用 Bill 实体中的字段名）
const RULE_FIELD_OPTIONS = {
  PAYMENT_CHANNEL: [
    { label: '支付渠道', value: 'paymentChannel' },
    { label: '交易类型', value: 'amountType' },
    { label: '交易描述', value: 'transactionDesc' }
  ],
  CATEGORY: [
    { label: '分类名称', value: 'category' },
    { label: '二级分类名称', value: 'subCategory' },
    { label: '收支类型', value: 'amountType' },
    { label: '交易类型', value: 'transactionType' },
    { label: '支付渠道', value: 'paymentChannel' },
    { label: '用户备注', value: 'manualRemark' }
  ],
  MANUAL_REMARK: [
    { label: '用户备注', value: 'manualRemark' },
    { label: '交易描述', value: 'transactionDesc' },
    { label: '分类名称', value: 'category' }
  ]
}

// 字段名映射为中文显示
const FIELD_LABELS = {
  paymentChannel: '支付渠道',
  amountType: '收支类型',
  category: '分类名称',
  subCategory: '二级分类名称',
  transactionType: '交易类型',
  transactionDesc: '交易描述',
  manualRemark: '用户备注'
}

// 匹配模式映射为中文显示
const MATCH_MODE_LABELS = {
  exact: '精确',
  contains: '包含',
  regex: '正则'
}

// 查询表单
const queryForm = reactive({
  current: 1,
  size: 10,
  ruleType: '',
  enabled: '',
  keywords: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)

// 弹窗控制
const dialogVisible = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)

// 表单数据
const formData = ref({
  id: null,
  ruleType: '',
  matchFields: '',
  targetValue: '',
  priority: 100,
  enabled: '1',
  remark: ''
})

// 匹配字段列表（用于编辑器）
const matchFieldsList = ref([])

// 表单验证规则
const formRules = {
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  matchFields: [
    { required: true, message: '请至少添加一个匹配字段', trigger: 'change' }
  ],
  targetValue: [
    { required: true, message: '请输入目标值', trigger: 'blur' },
    { max: 200, message: '最多输入 200 个字符', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请输入优先级', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

// 可用的字段选项（根据选中的规则类型动态变化）
const availableFieldOptions = computed(() => {
  if (!formData.value.ruleType) {
    return []
  }
  return RULE_FIELD_OPTIONS[formData.value.ruleType] || []
})

// 监听 matchFieldsList 变化，确保 formData.matchFields 始终同步
watch(matchFieldsList, () => {
  updateMatchFields()
  console.log('watch 监听到 matchFieldsList 变化:', matchFieldsList.value)
}, { deep: true })

// 获取字段的中文标签
function getFieldLabel(key) {
  return FIELD_LABELS[key] || key
}

// 获取匹配模式的中文标签
function getMatchModeLabel(mode) {
  return MATCH_MODE_LABELS[mode] || mode
}

// 解析并展示匹配条件（用于列表展示）
function displayMatchFields(matchFieldsJson) {
  if (!matchFieldsJson) return []
  
  try {
    const matchFieldsObj = JSON.parse(matchFieldsJson)
    
    // 新格式：{"fields": [{"name": "xxx", "matchMode": "exact", "value": "xxx"}]}
    if (matchFieldsObj.fields && Array.isArray(matchFieldsObj.fields)) {
      return matchFieldsObj.fields.map(f => ({
        fieldLabel: getFieldLabel(f.name),
        matchModeLabel: getMatchModeLabel(f.matchMode),
        value: f.value
      }))
    }
    
    // 兼容旧格式
    return Object.entries(matchFieldsObj).map(([key, value]) => ({
      fieldLabel: getFieldLabel(key),
      matchModeLabel: '精确',
      value
    }))
  } catch (e) {
    console.error('解析匹配条件失败:', e)
    return []
  }
}

// 查询数据
async function fetchData() {
  loading.value = true
  try {
    const res = await pageRules(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 查询
function handleQuery() {
  queryForm.current = 1
  fetchData()
}

// 重置
function handleReset() {
  queryForm.ruleType = ''
  queryForm.enabled = ''
  queryForm.keywords = ''
  queryForm.current = 1
  fetchData()
}

// 分页大小变化
function handleSizeChange(size) {
  queryForm.size = size
  queryForm.current = 1
  fetchData()
}

// 页码变化
function handleCurrentChange(current) {
  queryForm.current = current
  fetchData()
}

// 规则类型变化时，清空匹配字段
function handleRuleTypeChange() {
  matchFieldsList.value = []
  formData.value.matchFields = ''
}

// 添加匹配字段
function addField() {
  matchFieldsList.value.push({ name: '', matchMode: 'exact', value: '' })
  // 使用 nextTick 确保 DOM 更新后再调用 updateMatchFields
  nextTick(() => {
    updateMatchFields()
  })
}

// 删除匹配字段
function removeField(index) {
  matchFieldsList.value.splice(index, 1)
  // 使用 nextTick 确保 DOM 更新后再调用 updateMatchFields
  nextTick(() => {
    updateMatchFields()
  })
}

// 更新 match_fields JSON
function updateMatchFields() {
  const result = {
    fields: []
  }
  matchFieldsList.value.forEach(f => {
    if (f.name && f.value) {
      result.fields.push({
        name: f.name,
        matchMode: f.matchMode || 'exact', // 默认精确匹配
        value: f.value
      })
    }
  })
  formData.value.matchFields = JSON.stringify(result)
  console.log('匹配字段已更新:', result, 'JSON:', formData.value.matchFields)
}

// 新建
function handleCreate() {
  isEditMode.value = false
  dialogVisible.value = true
  formData.value = {
    id: null,
    ruleType: '',
    matchFields: '',
    targetValue: '',
    priority: 100,
    enabled: '1',
    remark: ''
  }
  matchFieldsList.value = []
}

// 编辑
async function handleEdit(row) {
  isEditMode.value = true
  
  try {
    console.log('开始获取详情，ID:', row.id)
    const res = await getRuleDetail(row.id)
    console.log('获取到的详情数据:', res)
    
    // 后端返回格式：{ code: 200, message: 'success', data: {...} }
    // request interceptor 已经返回 res，所以这里 res.data 就是实际的 VO
    const ruleData = res.data
    console.log('解析后的规则数据:', ruleData)
    
    // 填充表单数据
    formData.value = {
      id: ruleData.id,
      ruleType: ruleData.ruleType,
      matchFields: ruleData.matchFields || '{}',
      targetValue: ruleData.targetValue,
      priority: ruleData.priority,
      enabled: ruleData.enabled || '1',
      remark: ruleData.remark || ''
    }
    
    // 解析 match_fields
    matchFieldsList.value = []
    
    // 新的数据格式：{"fields": [{"name": "xxx", "matchMode": "exact", "value": "xxx"}]}
    try {
      const matchFieldsObj = JSON.parse(ruleData.matchFields || '{}')
      if (matchFieldsObj.fields && Array.isArray(matchFieldsObj.fields)) {
        matchFieldsList.value = matchFieldsObj.fields.map(f => ({
          name: f.name,
          matchMode: f.matchMode || 'exact',
          value: f.value
        }))
      } else if (ruleData.matchFieldsMap && Object.keys(ruleData.matchFieldsMap).length > 0) {
        // 兼容旧格式：{"field_name": "value"}
        matchFieldsList.value = Object.entries(ruleData.matchFieldsMap).map(([name, value]) => ({
          name,
          matchMode: 'exact', // 旧格式默认精确匹配
          value
        }))
      }
    } catch (e) {
      console.error('解析 matchFields 失败:', e)
    }
    
    console.log('解析后的匹配字段列表:', matchFieldsList.value)
    
    // 手动调用一次 updateMatchFields 确保 formData.matchFields 被正确填充
    updateMatchFields()
    
    console.log('表单数据已填充:', formData.value)
    console.log('匹配字段列表:', matchFieldsList.value)
    
    // 等待数据准备好后再打开弹窗
    await nextTick()
    dialogVisible.value = true
    
    // 清除表单验证状态
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败：' + (error.message || '未知错误'))
    isEditMode.value = false
  }
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm('确定要删除该清洗规则吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRule(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 提交
async function handleSubmit() {
  if (!formRef.value) return
  
  console.log('=== 开始提交 ===')
  console.log('表单数据:', JSON.parse(JSON.stringify(formData.value)))
  console.log('匹配字段列表:', JSON.parse(JSON.stringify(matchFieldsList.value)))
  console.log('formData.matchFields 的值:', formData.value.matchFields)
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      console.log('表单验证失败')
      return
    }
    
    // 验证至少有一个匹配字段
    const hasValidFields = matchFieldsList.value.some(f => f.name && f.value)
    console.log('是否有有效的匹配字段:', hasValidFields)
    if (!hasValidFields) {
      ElMessage.warning('请至少添加一个匹配字段')
      return
    }
    
    submitting.value = true
    try {
      const submitData = { ...formData.value }
      console.log('准备提交的数据:', JSON.parse(JSON.stringify(submitData)))
      
      if (isEditMode.value) {
        await updateRule(submitData)
        ElMessage.success('更新成功')
      } else {
        await createRule(submitData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 弹窗关闭
function handleDialogClose() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  matchFieldsList.value = []
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.data-clean-rule {
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

.table-area {
  margin-top: 20px;
}

.match-fields-editor {
  width: 100%;
}

.field-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.match-field-item {
  display: inline-flex;
  align-items: center;
  margin-bottom: 4px;
}

.match-field-item:not(:last-child) {
  border-right: 1px solid #dcdfe6;
  padding-right: 8px;
  margin-right: 8px;
}

:deep(.el-dialog__body) {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
