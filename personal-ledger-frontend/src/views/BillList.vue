<template>
  <div class="bill-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账单列表</span>
          <div>
            <el-button link type="primary" @click="showColumnSettings = true">
              <el-icon><Setting /></el-icon>
              列设置
            </el-button>
            <el-button type="primary" @click="handleCreate">+ 记一笔</el-button>
          </div>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :model="queryForm" class="query-form">
        <!-- 第一行：快捷日期 -->
        <div style="margin-bottom: 12px;">
          <span style="margin-right: 12px;">快捷日期：</span>
          <el-radio-group v-model="quickDate" @change="handleQuickDateChange">
            <el-radio-button label="今天" value="today" />
            <el-radio-button label="昨天" value="yesterday" />
            <el-radio-button label="本周" value="week" />
            <el-radio-button label="上周" value="lastWeek" />
            <el-radio-button label="本月" value="month" />
            <el-radio-button label="上月" value="lastMonth" />
            <el-radio-button label="本年" value="year" />
            <el-radio-button label="去年" value="lastYear" />
          </el-radio-group>
        </div>
        
        <!-- 第二行：日期范围 -->
        <div style="margin-bottom: 12px;">
          <span style="margin-right: 12px;">日期范围：</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </div>
        
        <!-- 第三行：收支类型 + 是否计入收支 + 分类 + 二级分类 -->
        <div style="margin-bottom: 12px;">
          <span style="margin-right: 12px;">收支类型：</span>
          <el-select v-model="queryForm.amountType" placeholder="全部" clearable style="width: 120px; margin-right: 12px;">
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
          </el-select>
          
          <span style="margin-right: 12px;">是否计入收支：</span>
          <el-select v-model="queryForm.includeInStatistics" placeholder="全部" clearable style="width: 100px; margin-right: 12px;">
            <el-option label="是" value="1" />
            <el-option label="否" value="0" />
          </el-select>
          
          <span style="margin-right: 12px;">分类：</span>
          <el-select 
            v-model="queryForm.categoryId" 
            placeholder="请选择分类" 
            filterable
            clearable
            @change="handleCategoryChange"
            style="width: 150px; margin-right: 12px;"
          >
            <el-option
              v-for="cat in categoryList"
              :key="cat.id"
              :label="cat.categoryName"
              :value="cat.id"
            />
          </el-select>
          
          <span style="margin-right: 12px;">二级分类：</span>
          <el-select 
            v-model="queryForm.subCategoryId" 
            placeholder="请选择二级分类" 
            filterable
            clearable
            style="width: 150px;"
          >
            <el-option
              v-for="subCat in currentSubCategoryList"
              :key="subCat.id"
              :label="subCat.categoryName"
              :value="subCat.id"
            />
          </el-select>
        </div>
        
        <!-- 折叠区域：更多查询条件 -->
        <el-collapse-transition>
          <div v-show="showAdvancedSearch">
            <!-- 第四行：支付渠道 + 交易类型 + 标签 -->
            <div style="margin-bottom: 12px;">
              <span style="margin-right: 12px;">支付渠道：</span>
              <el-select 
                v-model="queryForm.paymentChannel" 
                placeholder="请选择支付渠道" 
                filterable
                clearable 
                style="width: 150px; margin-right: 12px;"
              >
                <el-option
                  v-for="channel in paymentChannelList"
                  :key="channel.id"
                  :label="channel.channelName"
                  :value="channel.channelName"
                >
                  <span style="margin-right: 8px;">{{ channel.icon }}</span>
                  <span>{{ channel.channelName }}</span>
                  <el-tag v-if="channel.channelType" size="small" style="margin-left: 4px;">{{ getChannelTypeLabel(channel.channelType) }}</el-tag>
                </el-option>
              </el-select>
              
              <span style="margin-right: 12px;">交易类型：</span>
              <el-input v-model="queryForm.transactionType" placeholder="请输入交易类型" clearable style="width: 150px; margin-right: 12px;" />
              
              <span style="margin-right: 12px;">标签：</span>
              <el-select 
                v-model="queryForm.tagIds" 
                placeholder="请选择标签" 
                multiple 
                filterable
                clearable
                style="width: 300px; margin-right: 12px;"
              >
                <el-option
                  v-for="tag in tagList"
                  :key="tag.id"
                  :label="tag.tagName"
                  :value="tag.id"
                >
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <span 
                      class="color-dot" 
                      :style="{ backgroundColor: tag.tagColor }"
                      style="width: 12px; height: 12px; border-radius: 50%; display: inline-block;"
                    ></span>
                    <span>{{ tag.tagName }}</span>
                    <span v-if="tag.tagCategory" style="color: #8492a6; font-size: 13px">（{{ tag.tagCategory }}）</span>
                  </div>
                </el-option>
              </el-select>
              
              <span style="margin-right: 12px;">是否手工记账：</span>
              <el-select v-model="queryForm.manualEntry" placeholder="全部" clearable style="width: 120px;">
                <el-option label="是" value="1" />
                <el-option label="否" value="0" />
              </el-select>
            </div>
            
            <!-- 第五行：关键词搜索 + 金额范围 -->
            <div style="margin-bottom: 12px;">
              <span style="margin-right: 12px;">关键词：</span>
              <el-input 
                v-model="queryForm.keywords" 
                placeholder="搜索交易描述或用户备注" 
                clearable 
                style="width: 300px; margin-right: 12px;"
              />
              
              <span style="margin-right: 12px;">金额范围：</span>
              <el-input-number v-model="queryForm.minAmount" placeholder="最小金额" :min="0" :precision="2" style="width: 160px; margin-right: 8px;" />
              <span style="margin: 0 8px;">至</span>
              <el-input-number v-model="queryForm.maxAmount" placeholder="最大金额" :min="0" :precision="2" style="width: 160px;" />
            </div>
          </div>
        </el-collapse-transition>
        
        <!-- 第六行：展开/收起按钮 -->
        <div style="margin-bottom: 12px;">
          <el-button link type="primary" @click="toggleAdvancedSearch">
            {{ showAdvancedSearch ? '收起高级搜索' : '展开高级搜索' }}
          </el-button>
        </div>
        
        <!-- 第七行：查询和重置按钮 -->
        <div>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </el-form>

      <!-- 统计信息 -->
      <div class="statistics-cards">
        <div class="stat-card stat-card-balance">
          <div class="stat-label">结余</div>
          <div class="stat-value">{{ statistics.balance || 0 }}</div>
        </div>
        <div class="stat-operator">=</div>
        <div class="stat-card stat-card-income">
          <div class="stat-label">收入</div>
          <div class="stat-value">{{ statistics.totalIncome || 0 }}</div>
        </div>
        <div class="stat-operator">-</div>
        <div class="stat-card stat-card-expense">
          <div class="stat-label">支出</div>
          <div class="stat-value">{{ statistics.totalExpense || 0 }}</div>
        </div>
      </div>

      <!-- 批量操作工具栏 -->
      <div v-if="selectedRows.length > 0" class="batch-toolbar">
        <div class="batch-info">
          <el-icon :size="18"><Select /></el-icon>
          <span>已选择 {{ selectedRows.length }} 条账单</span>
        </div>
        <div class="batch-actions">
          <el-button type="primary" size="small" @click="showBatchPaymentChannel = true">
            <el-icon><Wallet /></el-icon>
            <span>支付渠道</span>
          </el-button>
          <el-button type="success" size="small" @click="showBatchCategory = true">
            <el-icon><Folder /></el-icon>
            <span>分类</span>
          </el-button>
          <el-button type="warning" size="small" @click="showBatchRemark = true">
            <el-icon><Edit /></el-icon>
            <span>备注</span>
          </el-button>
          <el-button type="info" size="small" @click="showBatchTag = true">
            <el-icon><Collection /></el-icon>
            <span>标签</span>
          </el-button>
          <el-button type="danger" size="small" @click="showBatchStatistics = true">
            <el-icon><DataAnalysis /></el-icon>
            <span>收支</span>
          </el-button>
          <el-button type="success" size="small" plain @click="handleBatchClean">
            <el-icon><RefreshRight /></el-icon>
            <span>数据清洗</span>
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table 
        :data="tableData" 
        style="width: 100%"
        class="bill-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <!-- 交易日期 -->
        <el-table-column 
          v-if="selectedColumns.includes('transactionDate')"
          prop="transactionDate" 
          label="交易日期" 
          width="100"
        />
        
        <!-- 交易时间 -->
        <el-table-column 
          v-if="selectedColumns.includes('transactionTime')"
          prop="transactionTime" 
          label="交易时间" 
          width="100" 
        />
        
        <!-- 收入 -->
        <el-table-column 
          v-if="selectedColumns.includes('incomeAmount')"
          label="收入" 
          width="80"
          align="right"
        >
          <template #default="{ row }">
            <span v-if="row.amountType === 'INCOME'" style="color: #67c23a;">
              {{ row.incomeAmount }}
            </span>
          </template>
        </el-table-column>
        
        <!-- 支出 -->
        <el-table-column 
          v-if="selectedColumns.includes('expenseAmount')"
          label="支出" 
          width="80"
          align="right"
        >
          <template #default="{ row }">
            <span v-if="row.amountType === 'EXPENSE'" style="color: #f56c6c;">
              {{ row.expenseAmount }}
            </span>
          </template>
        </el-table-column>
        
        <!-- 交易类型 -->
        <el-table-column 
          v-if="selectedColumns.includes('transactionType')"
          prop="transactionType" 
          label="交易类型" 
          width="120" 
          show-overflow-tooltip 
        />
        
        <!-- 支付渠道 -->
        <el-table-column 
          v-if="selectedColumns.includes('paymentChannel')"
          prop="paymentChannel" 
          label="支付渠道" 
          width="150" 
          show-overflow-tooltip 
        >
          <template #default="{ row }">
            <span v-if="row.paymentChannel">{{ row.paymentChannel }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        
        <!-- 分类 -->
        <el-table-column 
          v-if="selectedColumns.includes('category')"
          prop="category" 
          label="分类" 
          width="100"
          show-overflow-tooltip 
        />
        
        <!-- 二级分类 -->
        <el-table-column 
          v-if="selectedColumns.includes('subCategory')"
          prop="subCategory" 
          label="二级分类" 
          width="100"
          show-overflow-tooltip 
        />
        
        <!-- 交易描述 -->
        <el-table-column 
          v-if="selectedColumns.includes('transactionDesc')"
          prop="transactionDesc" 
          label="交易描述" 
          min-width="150" 
          show-overflow-tooltip 
        />
        
        <!-- 用户备注 -->
        <el-table-column 
          v-if="selectedColumns.includes('manualRemark')"
          prop="manualRemark" 
          label="用户备注" 
          min-width="120" 
          show-overflow-tooltip 
        />
        
        <!-- 标签 -->
        <el-table-column 
          v-if="selectedColumns.includes('tags')"
          prop="tags" 
          label="标签" 
          width="160"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <div v-if="row.tagIds && row.tagIds.length > 0" style="display: flex; flex-wrap: wrap; gap: 4px;">
              <el-tag
                v-for="tagId in row.tagIds"
                :key="tagId"
                size="small"
                effect="plain"
                :style="{ backgroundColor: getTagColor(tagId) + '20', borderColor: getTagColor(tagId), color: getTagColor(tagId) }"
              >
                {{ getTagName(tagId) }}
              </el-tag>
            </div>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        
        <!-- 是否计入收支 -->
        <el-table-column 
          v-if="selectedColumns.includes('includeInStatistics')"
          label="是否计入收支" 
          width="90" 
          align="center"
        >
          <template #default="{ row }">
            <el-tag v-if="row.includeInStatistics === '1'" type="success">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        
        <!-- 手工记账 -->
        <el-table-column 
          v-if="selectedColumns.includes('manualEntry')"
          label="手工记账" 
          width="90" 
          align="center"
        >
          <template #default="{ row }">
            <el-tag v-if="row.manualEntry === '1'" type="primary">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        
        <!-- 操作 -->
        <el-table-column 
          v-if="selectedColumns.includes('action')"
          label="操作" 
          width="150" 
          fixed="right" 
          align="center"
        >
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 列设置弹窗 -->
    <el-dialog
      v-model="showColumnSettings"
      title="列设置"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="column-settings">
        <el-checkbox-group v-model="selectedColumns">
          <div 
            v-for="col in allColumns" 
            :key="col.prop"
            class="column-item"
          >
            <el-checkbox :label="col.prop">{{ col.label }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="resetToDefault">恢复默认</el-button>
        <el-button type="primary" @click="saveColumnSettings">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改支付渠道弹窗 -->
    <el-dialog
      v-model="showBatchPaymentChannel"
      title="批量修改支付渠道"
      width="500px"
      @close="handleBatchDialogClose('paymentChannel')"
    >
      <el-form ref="batchFormRef" :model="batchForms.paymentChannel" label-width="100px">
        <el-form-item label="支付渠道" required>
          <el-select 
            v-model="batchForms.paymentChannel.channel" 
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
            >
              <span style="margin-right: 8px;">{{ channel.icon }}</span>
              <span>{{ channel.channelName }}</span>
              <el-tag v-if="channel.channelType" size="small" style="margin-left: 4px;">{{ getChannelTypeLabel(channel.channelType) }}</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchPaymentChannel = false">取消</el-button>
        <el-button type="primary" @click="handleBatchUpdate('paymentChannel')" :loading="batchSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改分类弹窗 -->
    <el-dialog
      v-model="showBatchCategory"
      title="批量修改分类"
      width="500px"
      @close="handleBatchDialogClose('category')"
    >
      <el-form ref="batchFormRef" :model="batchForms.category" label-width="100px">
        <el-form-item label="分类" required>
          <el-select 
            v-model="batchForms.category.categoryId" 
            placeholder="请选择分类" 
            filterable
            clearable
            @change="handleBatchCategoryChange"
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
        <el-form-item label="二级分类">
          <el-select 
            v-model="batchForms.category.subCategoryId" 
            placeholder="请选择二级分类" 
            filterable
            clearable
            style="width: 100%;"
          >
            <el-option
              v-for="subCat in batchSubCategoryList"
              :key="subCat.id"
              :label="subCat.categoryName"
              :value="subCat.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchCategory = false">取消</el-button>
        <el-button type="primary" @click="handleBatchUpdate('category')" :loading="batchSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改备注弹窗 -->
    <el-dialog
      v-model="showBatchRemark"
      title="批量修改备注"
      width="500px"
      @close="handleBatchDialogClose('remark')"
    >
      <el-form ref="batchFormRef" :model="batchForms.remark" label-width="100px">
        <el-form-item label="备注内容">
          <el-input 
            v-model="batchForms.remark.manualRemark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchRemark = false">取消</el-button>
        <el-button type="primary" @click="handleBatchUpdate('remark')" :loading="batchSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改标签弹窗 -->
    <el-dialog
      v-model="showBatchTag"
      title="批量修改标签"
      width="500px"
      @close="handleBatchDialogClose('tag')"
    >
      <el-form ref="batchFormRef" :model="batchForms.tag" label-width="100px">
        <el-form-item label="标签">
          <el-select 
            v-model="batchForms.tag.tagIds" 
            placeholder="请选择标签" 
            multiple 
            filterable
            clearable
            style="width: 100%;"
          >
            <el-option
              v-for="tag in getEnabledTagList"
              :key="tag.id"
              :label="tag.tagName"
              :value="tag.id"
            >
              <div style="display: flex; align-items: center; gap: 8px;">
                <span 
                  class="color-dot" 
                  :style="{ backgroundColor: tag.tagColor }"
                  style="width: 12px; height: 12px; border-radius: 50%; display: inline-block;"
                ></span>
                <span>{{ tag.tagName }}</span>
                <span v-if="tag.tagCategory" style="color: #8492a6; font-size: 13px">（{{ tag.tagCategory }}）</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchTag = false">取消</el-button>
        <el-button type="primary" @click="handleBatchUpdate('tag')" :loading="batchSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量修改统计弹窗 -->
    <el-dialog
      v-model="showBatchStatistics"
      title="批量修改是否计入收支"
      width="500px"
      @close="handleBatchDialogClose('statistics')"
    >
      <el-form ref="batchFormRef" :model="batchForms.statistics" label-width="100px">
        <el-form-item label="计入收支" required>
          <el-radio-group v-model="batchForms.statistics.includeInStatistics">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchStatistics = false">取消</el-button>
        <el-button type="primary" @click="handleBatchUpdate('statistics')" :loading="batchSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量数据清洗确认弹窗 -->
    <el-dialog
      v-model="showBatchCleanDialog"
      title="数据清洗"
      width="500px"
    >
      <div style="padding: 20px;">
        <p style="margin-bottom: 16px;">确定要对选中的 <strong>{{ selectedRows.length }}</strong> 条账单进行数据清洗吗？</p>
        <el-alert
          title="清洗说明"
          type="info"
          :closable="false"
          show-icon
        >
          <ul style="margin: 8px 0 0 20px; padding: 0;">
            <li>根据预设的清洗规则自动填充分类、支付渠道、用户备注等字段</li>
            <li>已填写的字段可能会被覆盖，请谨慎操作</li>
            <li>清洗过程不会修改原始交易描述和金额</li>
          </ul>
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="showBatchCleanDialog = false">取消</el-button>
        <el-button type="success" @click="executeBatchClean" :loading="batchSubmitting">确定清洗</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="金额类型" prop="amountType">
          <el-radio-group v-model="form.amountType" :disabled="isViewMode || (isEditMode && form.manualEntry === '0')">
            <el-radio label="INCOME">收入</el-radio>
            <el-radio label="EXPENSE">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="1" :disabled="isViewMode || (isEditMode && form.manualEntry === '0')" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select 
            v-model="form.categoryId" 
            placeholder="请选择分类" 
            filterable
            clearable
            @change="handleFormCategoryChange"
            :disabled="isViewMode"
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
        <el-form-item label="二级分类" prop="subCategoryId">
          <el-select 
            v-model="form.subCategoryId" 
            placeholder="请选择二级分类" 
            filterable
            clearable
            :disabled="isViewMode"
            style="width: 100%;"
          >
            <el-option
              v-for="subCat in formSubCategoryList"
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
            :disabled="isViewMode || (isEditMode && form.manualEntry === '0')"
          />
        </el-form-item>
        <el-form-item label="交易时间">
          <el-time-picker
            v-model="form.transactionTime"
            placeholder="选择时间"
            value-format="HH:mm:ss"
            :disabled="isViewMode || (isEditMode && form.manualEntry === '0')"
          />
        </el-form-item>
        <el-form-item label="交易类型">
          <el-input v-model="form.transactionType" placeholder="请输入交易类型" :disabled="isViewMode || (isEditMode && form.manualEntry === '0')" />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select 
            v-model="form.paymentChannel" 
            placeholder="请选择支付渠道" 
            filterable
            clearable
            :disabled="isViewMode"
            style="width: 100%;"
            @change="handlePaymentChannelChange"
          >
            <el-option
              v-for="channel in paymentChannelList"
              :key="channel.id"
              :label="channel.channelName"
              :value="channel.channelName"
            >
              <span style="margin-right: 8px;">{{ channel.icon }}</span>
              <span>{{ channel.channelName }}</span>
              <el-tag v-if="channel.channelType" size="small" style="margin-left: 4px;">{{ getChannelTypeLabel(channel.channelType) }}</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="交易描述">
          <el-input v-model="form.transactionDesc" type="textarea" :rows="3" :disabled="isViewMode || (isEditMode && form.manualEntry === '0')" />
        </el-form-item>
        <el-form-item label="手工备注">
          <el-input v-model="form.manualRemark" type="textarea" :rows="2" :disabled="isViewMode" />
        </el-form-item>
        <el-form-item label="是否计入收支">
          <el-switch v-model="form.includeInStatistics" active-value="1" inactive-value="0" :disabled="isViewMode" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select 
            v-model="form.tagIds" 
            placeholder="请选择标签" 
            multiple 
            filterable
            :disabled="isViewMode"
            style="width: 100%;"
          >
            <!-- 编辑时只显示启用的标签 -->
            <template v-if="isEditMode">
              <el-option
                v-for="tag in getEnabledTagList"
                :key="tag.id"
                :label="tag.tagName"
                :value="tag.id"
              >
                <div style="display: flex; align-items: center; gap: 8px;">
                  <span 
                    class="color-dot" 
                    :style="{ backgroundColor: tag.tagColor }"
                    style="width: 12px; height: 12px; border-radius: 50%; display: inline-block;"
                  ></span>
                  <span>{{ tag.tagName }}</span>
                  <span v-if="tag.tagCategory" style="color: #8492a6; font-size: 13px">（{{ tag.tagCategory }}）</span>
                </div>
              </el-option>
            </template>
            <!-- 查看时显示所有标签（包括停用的） -->
            <template v-else>
              <el-option
                v-for="tag in tagList"
                :key="tag.id"
                :label="tag.tagName"
                :value="tag.id"
              >
                <div style="display: flex; align-items: center; gap: 8px;">
                  <span 
                    class="color-dot" 
                    :style="{ backgroundColor: tag.tagColor }"
                    style="width: 12px; height: 12px; border-radius: 50%; display: inline-block;"
                  ></span>
                  <span>{{ tag.tagName }}</span>
                  <span v-if="tag.tagCategory" style="color: #8492a6; font-size: 13px">（{{ tag.tagCategory }}）</span>
                </div>
              </el-option>
            </template>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isViewMode" type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Select, Wallet, Folder, Edit, Collection, DataAnalysis, Delete, RefreshRight } from '@element-plus/icons-vue'
import { pageBills, getStatistics, createBill, updateBill, batchUpdateBills } from '@/api/bill'
import { getTagList } from '@/api/tag'
import { getCategoryList } from '@/api/category'
import { listPaymentChannels } from '@/api/paymentChannel'
import { batchCleanBills } from '@/api/dataCleanRule'
import request from '@/utils/request'

// 列设置 - 所有可用列
const allColumns = [
  { prop: 'transactionDate', label: '交易日期' },
  { prop: 'transactionTime', label: '交易时间' },
  { prop: 'incomeAmount', label: '收入' },
  { prop: 'expenseAmount', label: '支出' },
  { prop: 'transactionType', label: '交易类型' },
  { prop: 'paymentChannel', label: '支付渠道' },
  { prop: 'category', label: '分类' },
  { prop: 'subCategory', label: '二级分类' },
  { prop: 'transactionDesc', label: '交易描述' },
  { prop: 'manualRemark', label: '用户备注' },
  { prop: 'tags', label: '标签' },
  { prop: 'includeInStatistics', label: '是否计入收支' },
  { prop: 'manualEntry', label: '手工记账' },
  { prop: 'action', label: '操作' }
]

// 默认显示的列（所有字段）
const defaultColumns = [
  'transactionDate',
  'transactionTime',
  'incomeAmount',
  'expenseAmount',
  'transactionType',
  'paymentChannel',
  'category',
  'subCategory',
  'transactionDesc',
  'manualRemark',
  'tags',
  'includeInStatistics',
  'manualEntry',
  'action'
]

// 列设置相关
const showColumnSettings = ref(false)
const selectedColumns = ref([...defaultColumns])

// localStorage key
const STORAGE_KEY = 'bill_list_columns'

// 加载保存的列配置
const loadColumnSettings = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      selectedColumns.value = JSON.parse(saved)
    }
  } catch (e) {
    console.error('加载列配置失败:', e)
    selectedColumns.value = [...defaultColumns]
  }
}

// 保存列配置
const saveColumnSettings = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(selectedColumns.value))
    ElMessage.success('列设置已保存')
    showColumnSettings.value = false
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// 恢复默认设置
const resetToDefault = () => {
  selectedColumns.value = [...defaultColumns]
  ElMessage.success('已恢复默认设置')
}

// 查询表单
const queryForm = reactive({
  current: 1,
  size: 20,
  transactionType: '',
  categoryId: null,  // 分类 ID
  subCategoryId: null,  // 二级分类 ID
  paymentChannel: '',
  amountType: '',
  includeInStatistics: '',
  manualEntry: '',  // 是否手工记账
  tagIds: [],  // 标签 ID 数组
  keywords: '',
  minAmount: null,
  maxAmount: null,
  startDate: '',
  endDate: ''
})

const dateRange = ref([])
const quickDate = ref('month') // 快捷日期选择，默认为本月
const showAdvancedSearch = ref(false) // 是否显示高级搜索

// 表格数据
const tableData = ref([])
const total = ref(0)

// 统计数据
const statistics = ref({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0
})

// 标签列表
const tagList = ref([])

// 分类列表
const categoryList = ref([])

// 支付渠道列表
const paymentChannelList = ref([])

// 当前选中的分类 ID（用于筛选二级分类）
const selectedCategoryId = ref(null)

// 加载标签列表（包含停用和启用的）
const loadTagList = async () => {
  try {
    const res = await getTagList({ size: 100 })  // 不传 tagStatus，获取所有标签
    tagList.value = res.data.records || []
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

// 加载分类列表
const loadCategoryList = async () => {
  try {
    const res = await getCategoryList('', '')  // 获取所有分类
    categoryList.value = (res.data || []).filter(cat => !cat.parentId)  // 只取一级分类
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

// 加载支付渠道列表（按排序）
const loadPaymentChannelList = async () => {
  try {
    const res = await listPaymentChannels({ size: 100 })
    // 按 sortOrder 排序
    paymentChannelList.value = (res.data || []).sort((a, b) => a.sortOrder - b.sortOrder)
  } catch (error) {
    console.error('加载支付渠道列表失败:', error)
  }
}

// 获取启用的标签列表（用于编辑时选择）
const getEnabledTagList = computed(() => {
  return tagList.value.filter(tag => tag.tagStatus === 'enable')
})

// 当前分类下的二级分类列表
const currentSubCategoryList = computed(() => {
  if (!selectedCategoryId.value) {
    return []
  }
  const parent = categoryList.value.find(cat => cat.id === selectedCategoryId.value)
  return parent && parent.children ? parent.children : []
})

// 表单中的二级分类列表（根据选中的一级分类动态计算）
const formSubCategoryList = computed(() => {
  if (!form.categoryId) {
    return []
  }
  const parent = categoryList.value.find(cat => cat.id === form.categoryId)
  return parent && parent.children ? parent.children : []
})

// 分类变化处理
const handleCategoryChange = (categoryId) => {
  selectedCategoryId.value = categoryId
  queryForm.subCategoryId = null  // 清空二级分类选择
}

// 分类变化处理（表单）
const handleFormCategoryChange = (categoryId) => {
  form.subCategoryId = null  // 清空二级分类选择
}

// 支付渠道变化处理（表单）
const handlePaymentChannelChange = (channelName) => {
  if (!channelName) {
    // 清空时，同时清空 ID
    form.paymentChannelId = null
  } else {
    // 根据名称查找对应的 ID
    const channel = paymentChannelList.value.find(c => c.channelName === channelName)
    if (channel) {
      form.paymentChannelId = channel.id
    }
  }
}

// 根据标签 ID 获取标签名称
const getTagName = (tagId) => {
  const tag = tagList.value.find(t => t.id === tagId)
  return tag ? tag.tagName : ''
}

// 根据标签 ID 获取标签颜色
const getTagColor = (tagId) => {
  const tag = tagList.value.find(t => t.id === tagId)
  return tag && tag.tagColor ? tag.tagColor : '#409EFF' // 默认蓝色
}

// 渠道类型映射
const channelTypeMap = {
  'CASH': '现金',
  'BANK_CARD': '银行卡',
  'CREDIT_CARD': '信用卡',
  'E_WALLET': '电子钱包',
  'OTHER': '其他'
}

// 获取渠道类型标签
const getChannelTypeLabel = (type) => {
  return channelTypeMap[type] || type
}

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('记一笔')
const formRef = ref()
const isViewMode = ref(false) // 是否为查看模式
const isEditMode = ref(false) // 是否为编辑模式
const form = reactive({
  id: null,
  amountType: 'EXPENSE',
  transactionType: '',
  amount: null,
  categoryId: null,  // 分类 ID
  subCategoryId: null,  // 二级分类 ID
  transactionDate: '',
  transactionTime: '',
  paymentChannel: '',  // 支付渠道名称
  paymentChannelId: null,  // 支付渠道 ID
  transactionDesc: '',
  manualRemark: '',
  includeInStatistics: '1',
  tagIds: [],  // 标签 ID 数组
  manualEntry: '1'  // 是否手工记账，默认为是
})

const rules = {
  amountType: [{ required: true, message: '请选择金额类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  transactionDate: [{ required: true, message: '请选择交易日期', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  try {
    const res = await pageBills(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

// 加载统计
const loadStatistics = async () => {
  try {
    const res = await getStatistics(queryForm)
    statistics.value = res.data
  } catch (error) {
    ElMessage.error('加载统计失败')
  }
}

// 日期范围变化
const handleDateChange = (val) => {
  if (val) {
    queryForm.startDate = val[0]
    queryForm.endDate = val[1]
  } else {
    queryForm.startDate = ''
    queryForm.endDate = ''
  }
}

// 快捷日期选择
const handleQuickDateChange = (value) => {
  if (!value) {
    // 清空快捷日期时，同时清空日期范围
    queryForm.startDate = ''
    queryForm.endDate = ''
    dateRange.value = []
    return
  }
  
  const today = new Date()
  let startDate = ''
  let endDate = ''
  
  switch (value) {
    case 'today':
      startDate = formatDate(today)
      endDate = formatDate(today)
      break
    case 'yesterday':
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
      startDate = formatDate(yesterday)
      endDate = formatDate(yesterday)
      break
    case 'week':
      // 本周一
      const currentDay = today.getDay() || 7 // 周日为 0，转为 7
      const mondayOffset = (currentDay - 1) * 24 * 60 * 60 * 1000
      const monday = new Date(today.getTime() - mondayOffset)
      startDate = formatDate(monday)
      endDate = formatDate(today)
      break
    case 'lastWeek':
      // 上周一到上周日
      const currentDay2 = today.getDay() || 7
      const lastMondayOffset = (currentDay2 + 6) * 24 * 60 * 60 * 1000
      const lastSundayOffset = (currentDay2 - 1) * 24 * 60 * 60 * 1000
      const lastMonday = new Date(today.getTime() - lastMondayOffset)
      const lastSunday = new Date(today.getTime() - lastSundayOffset)
      startDate = formatDate(lastMonday)
      endDate = formatDate(lastSunday)
      break
    case 'month':
      // 本月 1 号到今天
      const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
      startDate = formatDate(firstDayOfMonth)
      endDate = formatDate(today)
      break
    case 'lastMonth':
      // 上月 1 号到上月最后一天
      const firstDayOfLastMonth = new Date(today.getFullYear(), today.getMonth() - 1, 1)
      const lastDayOfLastMonth = new Date(today.getFullYear(), today.getMonth(), 0)
      startDate = formatDate(firstDayOfLastMonth)
      endDate = formatDate(lastDayOfLastMonth)
      break
    case 'year':
      // 本年 1 月 1 号到今天
      const firstDayOfYear = new Date(today.getFullYear(), 0, 1)
      startDate = formatDate(firstDayOfYear)
      endDate = formatDate(today)
      break
    case 'lastYear':
      // 去年 1 月 1 号到去年 12 月 31 号
      const firstDayOfLastYear = new Date(today.getFullYear() - 1, 0, 1)
      const lastDayOfLastYear = new Date(today.getFullYear(), 0, 0)
      startDate = formatDate(firstDayOfLastYear)
      endDate = formatDate(lastDayOfLastYear)
      break
  }
  
  queryForm.startDate = startDate
  queryForm.endDate = endDate
  dateRange.value = [startDate, endDate]
}

// 格式化日期 YYYY-MM-DD
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 分页变化处理
const handleSizeChange = (size) => {
  queryForm.size = size
  queryForm.current = 1
  loadData()
  loadStatistics()
}

const handleCurrentChange = (current) => {
  queryForm.current = current
  loadData()
  loadStatistics()
}

// 查询
const handleQuery = () => {
  queryForm.current = 1
  loadData()
  loadStatistics()
}

// 重置
const handleReset = () => {
  queryForm.current = 1
  queryForm.transactionType = ''
  queryForm.categoryId = null
  queryForm.subCategoryId = null
  queryForm.paymentChannel = ''
  queryForm.amountType = ''
  queryForm.includeInStatistics = ''
  queryForm.manualEntry = ''  // 重置手工记账
  queryForm.tagIds = []  // 重置标签
  queryForm.keywords = ''
  queryForm.minAmount = null
  queryForm.maxAmount = null
  queryForm.startDate = ''
  queryForm.endDate = ''
  dateRange.value = []
  quickDate.value = 'month' // 重置时恢复默认值为本月
  selectedCategoryId.value = null  // 清空选中的分类
  handleQuickDateChange('month') // 同时更新查询条件
  showAdvancedSearch.value = false // 收起高级搜索
  handleQuery()
}

// 切换高级搜索显示/隐藏
const toggleAdvancedSearch = () => {
  showAdvancedSearch.value = !showAdvancedSearch.value
}

// 新增
const handleCreate = () => {
  dialogTitle.value = '记一笔'
  isViewMode.value = false // 重置查看模式
  isEditMode.value = false // 重置编辑模式
  dialogVisible.value = true
  Object.assign(form, {
    id: null,
    amountType: 'EXPENSE',
    transactionType: '',
    amount: null,
    category: '',
    transactionDate: new Date().toISOString().split('T')[0],
    transactionTime: '',
    paymentChannel: '',
    transactionDesc: '',
    manualRemark: '',
    includeInStatistics: '1',
    tagIds: []  // 清空标签
  })
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑账单'
  isEditMode.value = true // 设置为编辑模式
  dialogVisible.value = true
  Object.assign(form, {
    id: row.id,
    amountType: row.amountType,
    transactionType: row.transactionType,
    amount: row.amountType === 'INCOME' ? row.incomeAmount : row.expenseAmount,
    categoryId: row.categoryId,
    subCategoryId: row.subCategoryId,
    transactionDate: row.transactionDate,
    transactionTime: row.transactionTime,
    paymentChannel: row.paymentChannel,
    paymentChannelId: row.paymentChannelId || null,  // 新增：支付渠道 ID
    transactionDesc: row.transactionDesc,
    manualRemark: row.manualRemark,
    includeInStatistics: row.includeInStatistics,
    tagIds: row.tagIds || [],  // 加载标签
    manualEntry: row.manualEntry || '0'  // 加载是否手工记账
  })
}

// 查看
const handleView = (row) => {
  dialogTitle.value = '账单详情'
  isViewMode.value = true // 设置为查看模式
  isEditMode.value = false // 确保编辑模式为 false
  dialogVisible.value = true
  Object.assign(form, {
    id: row.id,
    amountType: row.amountType,
    transactionType: row.transactionType,
    amount: row.amountType === 'INCOME' ? row.incomeAmount : row.expenseAmount,
    categoryId: row.categoryId,
    subCategoryId: row.subCategoryId,
    transactionDate: row.transactionDate,
    transactionTime: row.transactionTime,
    paymentChannel: row.paymentChannel,
    paymentChannelId: row.paymentChannelId || null,  // 新增：支付渠道 ID
    transactionDesc: row.transactionDesc,
    manualRemark: row.manualRemark,
    includeInStatistics: row.includeInStatistics,
    manualEntry: row.manualEntry,
    tagIds: row.tagIds || []  // 加载标签 ID
  })
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 根据 categoryId 查找分类名称
    let categoryName = ''
    let subCategoryName = ''
    
    if (form.categoryId) {
      const parent = categoryList.value.find(cat => cat.id === form.categoryId)
      if (parent) {
        categoryName = parent.categoryName
      }
    }
    
    if (form.subCategoryId) {
      const parent = categoryList.value.find(cat => cat.id === form.categoryId)
      if (parent && parent.children) {
        const subCat = parent.children.find(sub => sub.id === form.subCategoryId)
        if (subCat) {
          subCategoryName = subCat.categoryName
        }
      }
    }
    
    const data = {
      id: form.id,
      amountType: form.amountType,
      transactionType: form.transactionType,
      transactionDate: form.transactionDate,
      transactionTime: form.transactionTime,
      categoryId: form.categoryId,
      category: categoryName,  // 同时传递分类名称
      subCategoryId: form.subCategoryId,
      subCategory: subCategoryName,  // 同时传递二级分类名称
      paymentChannel: form.paymentChannel,
      paymentChannelId: form.paymentChannelId,  // 新增：支付渠道 ID
      transactionDesc: form.transactionDesc,
      manualRemark: form.manualRemark,
      includeInStatistics: form.includeInStatistics,
      manualEntry: form.manualEntry,  // 新增：是否手工记账
      tagIds: form.tagIds  // 标签 ID 数组
    }
    
    console.log('提交账单数据:', data)
    
    if (form.amountType === 'INCOME') {
      data.incomeAmount = form.amount
    } else {
      data.expenseAmount = form.amount
    }
    
    if (form.id) {
      await updateBill(data)
      ElMessage.success('更新成功')
    } else {
      await createBill(data)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  formRef.value?.resetFields()
  isViewMode.value = false // 重置查看模式
  isEditMode.value = false // 重置编辑模式
}

// ========== 批量操作相关 ==========

// 选中的行
const selectedRows = ref([])

// 批量操作弹窗显示控制
const showBatchPaymentChannel = ref(false)
const showBatchCategory = ref(false)
const showBatchRemark = ref(false)
const showBatchTag = ref(false)
const showBatchStatistics = ref(false)
const showBatchCleanDialog = ref(false)

// 批量提交状态
const batchSubmitting = ref(false)

// 批量表单数据
const batchForms = reactive({
  paymentChannel: {
    channel: ''
  },
  category: {
    categoryId: null,
    subCategoryId: null
  },
  remark: {
    manualRemark: ''
  },
  tag: {
    tagIds: []
  },
  statistics: {
    includeInStatistics: '1'
  }
})

// 批量表单引用
const batchFormRef = ref()

// 批量分类变化时的二级分类列表
const batchSelectedCategoryId = ref(null)

// 批量表单中的二级分类列表
const batchSubCategoryList = computed(() => {
  if (!batchSelectedCategoryId.value) {
    return []
  }
  const parent = categoryList.value.find(cat => cat.id === batchSelectedCategoryId.value)
  return parent && parent.children ? parent.children : []
})

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedRows.value = selection.map(row => row.id)
}

// 批量分类变化处理
const handleBatchCategoryChange = (categoryId) => {
  batchSelectedCategoryId.value = categoryId
  batchForms.category.subCategoryId = null  // 清空二级分类选择
}

// 批量对话框关闭
const handleBatchDialogClose = (type) => {
  if (batchFormRef.value) {
    batchFormRef.value.resetFields()
  }
  // 重置对应的表单数据
  switch (type) {
    case 'paymentChannel':
      batchForms.paymentChannel.channel = ''
      break
    case 'category':
      batchForms.category.categoryId = null
      batchForms.category.subCategoryId = null
      batchSelectedCategoryId.value = null
      break
    case 'remark':
      batchForms.remark.manualRemark = ''
      break
    case 'tag':
      batchForms.tag.tagIds = []
      break
    case 'statistics':
      batchForms.statistics.includeInStatistics = '1'
      break
  }
}

// 批量更新
const handleBatchUpdate = async (type) => {
  try {
    batchSubmitting.value = true
    
    // 构建批量更新数据
    const data = {
      billIds: selectedRows.value,
      updateFields: []  // 明确指定要更新的字段
    }
    
    // 根据类型设置不同的更新字段
    switch (type) {
      case 'paymentChannel':
        if (!batchForms.paymentChannel.channel) {
          ElMessage.warning('请选择支付渠道')
          return
        }
        data.paymentChannel = batchForms.paymentChannel.channel
        // 根据名称查找 ID
        const channel = paymentChannelList.value.find(c => c.channelName === batchForms.paymentChannel.channel)
        if (channel) {
          data.paymentChannelId = channel.id
        }
        data.updateFields.push('paymentChannel')
        break
      case 'category':
        if (!batchForms.category.categoryId) {
          ElMessage.warning('请选择分类')
          return
        }
        data.categoryId = batchForms.category.categoryId
        data.subCategoryId = batchForms.category.subCategoryId
        // 查找分类名称
        const parent = categoryList.value.find(cat => cat.id === batchForms.category.categoryId)
        if (parent) {
          data.category = parent.categoryName
        }
        if (batchForms.category.subCategoryId) {
          const subCat = batchSubCategoryList.value.find(sub => sub.id === batchForms.category.subCategoryId)
          if (subCat) {
            data.subCategory = subCat.categoryName
          }
        }
        data.updateFields.push('categoryId')
        break
      case 'remark':
        data.manualRemark = batchForms.remark.manualRemark
        data.updateFields.push('manualRemark')
        break
      case 'tag':
        data.tagIds = batchForms.tag.tagIds
        data.updateFields.push('tagIds')
        break
      case 'statistics':
        data.includeInStatistics = batchForms.statistics.includeInStatistics
        data.updateFields.push('includeInStatistics')
        break
    }
    
    console.log('批量更新数据:', data)
    
    // 调用批量更新接口
    await batchUpdateBills(data)
    
    ElMessage.success('批量更新成功')
    
    // 关闭弹窗
    handleBatchDialogClose(type)
    if (type === 'paymentChannel') showBatchPaymentChannel.value = false
    else if (type === 'category') showBatchCategory.value = false
    else if (type === 'remark') showBatchRemark.value = false
    else if (type === 'tag') showBatchTag.value = false
    else if (type === 'statistics') showBatchStatistics.value = false
    
    // 重新加载数据
    loadData()
    loadStatistics()
  } catch (error) {
    console.error('批量更新失败:', error)
    ElMessage.error(error.message || '批量更新失败')
  } finally {
    batchSubmitting.value = false
  }
}

// 批量数据清洗
const handleBatchClean = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要清洗的账单')
    return
  }
  showBatchCleanDialog.value = true
}

// 执行批量清洗
const executeBatchClean = async () => {
  try {
    batchSubmitting.value = true
    
    console.log('开始批量清洗，账单 ID:', selectedRows.value)
    
    // 调用批量清洗接口
    const res = await batchCleanBills(selectedRows.value)
    
    const successCount = res.data || 0
    ElMessage.success(`成功清洗 ${successCount} 条账单`)
    showBatchCleanDialog.value = false
    
    // 重新加载数据
    loadData()
    loadStatistics()
  } catch (error) {
    console.error('批量清洗失败:', error)
    ElMessage.error(error.message || '批量清洗失败')
  } finally {
    batchSubmitting.value = false
  }
}

onMounted(() => {
  // 初始化快捷日期为本月
  if (quickDate.value === 'month') {
    handleQuickDateChange('month')
  }
  // 加载保存的列配置
  loadColumnSettings()
  // 加载标签列表
  loadTagList()
  loadCategoryList()  // 加载分类列表
  loadPaymentChannelList()  // 加载支付渠道列表
  loadData()
  loadStatistics()
})
</script>

<style scoped>
.bill-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header > div {
  display: flex;
  align-items: center;
  gap: 12px;
}

.column-settings {
  max-height: 400px;
  overflow-y: auto;
}

.column-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.column-item:last-child {
  border-bottom: none;
}

.query-form {
  margin-bottom: 20px;
  font-size: 14px; /* 查询表单字体 */
}

/* 查询表单中的标签文字 */
.query-form span {
  font-size: 14px;
  color: #606266;
}

/* 查询表单中的输入框和选择器 */
.query-form .el-input,
.query-form .el-select,
.query-form .el-radio-group {
  font-size: 14px;
}

/* 日期选择器字体 */
.query-form .el-date-editor {
  font-size: 14px;
}

/* 数字输入框字体 */
.query-form .el-input-number {
  font-size: 14px;
}

.statistics-cards {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.stat-card {
  flex: 0 0 auto;
  width: 240px;
  height: 90px;
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-card-balance {
  /* 移除左侧彩色边框 */
}

.stat-card-income {
  /* 移除左侧彩色边框 */
}

.stat-card-expense {
  /* 移除左侧彩色边框 */
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: 500;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.stat-card-balance .stat-value {
  color: #409eff;
}

.stat-card-income .stat-value {
  color: #67c23a;
}

.stat-card-expense .stat-value {
  color: #f56c6c;
}

.stat-operator {
  font-size: 20px;
  font-weight: bold;
  color: #909399;
  margin: 0 4px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

/* 批量操作工具栏 */
.batch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: linear-gradient(to right, #f5f7fa, #fafcff);
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.batch-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.batch-info .el-icon {
  font-size: 18px;
  color: #409eff;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.batch-actions .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 100px;
}
</style>
