<template>
  <el-card>
    <div class="header">
      <div class="title">资产基础管理</div>
      <div class="spacer" />
      <el-upload :show-file-list="false" accept=".xlsx,.xls" :http-request="importExcel">
        <el-button>导入Excel</el-button>
      </el-upload>
      <el-button @click="exportExcel">导出Excel</el-button>
      <el-button :disabled="selectedIds.length === 0" @click="printSelected">批量打印二维码</el-button>
      <el-button type="primary" @click="openCreate">新增资产</el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-form :inline="true" class="filters">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="资产名称/型号" style="width: 220px" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.assetType" clearable placeholder="全部" style="width: 160px">
          <el-option v-for="t in assetTypes" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" row-key="assetId" @selection-change="onSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="assetId" label="资产编号" width="160" />
      <el-table-column prop="assetName" label="资产名称" min-width="160" />
      <el-table-column prop="assetType" label="类型" width="140" />
      <el-table-column prop="model" label="型号" width="160" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.assetId)">查看</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        background
        layout="prev, pager, next, sizes, total"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="load"
        @size-change="load"
      />
    </div>
  </el-card>

  <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增资产' : '编辑资产'" width="680px">
    <el-form :model="dialog.form" label-width="110px">
      <el-form-item label="资产编号" v-if="dialog.mode === 'create'">
        <el-input v-model="dialog.form.assetId" placeholder="可留空自动生成" />
      </el-form-item>
      <el-form-item label="资产类型">
        <el-select v-model="dialog.form.assetType" placeholder="请选择">
          <el-option v-for="t in assetTypes" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="资产名称">
        <el-input v-model="dialog.form.assetName" />
      </el-form-item>
      <el-form-item label="型号">
        <el-input v-model="dialog.form.model" />
      </el-form-item>
      <el-form-item label="规格">
        <el-input v-model="dialog.form.specification" />
      </el-form-item>
      <template v-if="dialog.form.assetType === 'PC-组装机'">
        <el-form-item label="主机型号">
          <el-input v-model="dialog.form.hostModel" />
        </el-form-item>
        <el-form-item label="主机配置">
          <el-input v-model="dialog.form.hostSpec" placeholder="CPU、内存、硬盘等" />
        </el-form-item>
        <el-form-item label="显示器型号">
          <el-input v-model="dialog.form.monitorModel" />
        </el-form-item>
        <el-form-item label="显示器配置">
          <el-input v-model="dialog.form.monitorSpec" placeholder="尺寸、分辨率等" />
        </el-form-item>
      </template>
      <el-form-item label="采购日期">
        <el-date-picker v-model="dialog.form.purchaseDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="所属部门ID">
        <el-input v-model="dialog.form.deptId" />
      </el-form-item>
      <el-form-item label="使用人ID">
        <el-input v-model="dialog.form.userId" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="dialog.form.status" placeholder="请选择">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="dialog.form.remark" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="dialog.loading" @click="submit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import type { UploadRequestOptions } from 'element-plus'

type Asset = {
  assetId: string
  assetType: string
  assetName: string
  model: string
  specification: string | null
  purchaseDate: string
  deptId: string
  userId: string | null
  status: string
  remark: string | null
}

const assetTypes = ['PC-笔记本', 'PC-组装机', '移动端', '办公设备', '耗材配件']
const statuses = ['在用', '闲置', '维修', '报废']

const router = useRouter()
const route = useRoute()
const rows = ref<Asset[]>([])
const total = ref(0)
const selectedIds = ref<string[]>([])
const page = ref(1)
const size = ref(10)
const query = reactive({ keyword: '', assetType: '', status: '', deptId: '' })

const dialog = reactive({
  visible: false,
  mode: 'create' as 'create' | 'edit',
  loading: false,
  editingId: '',
  form: {
    assetId: '',
    assetType: assetTypes[0],
    assetName: '',
    model: '',
    specification: '',
    hostModel: '',
    hostSpec: '',
    monitorModel: '',
    monitorSpec: '',
    purchaseDate: '',
    deptId: 'dept-it',
    userId: '',
    status: '闲置',
    remark: '',
  },
})

const load = async () => {
  const r = await http.get('/api/assets', {
    params: {
      page: page.value,
      size: size.value,
      keyword: query.keyword || undefined,
      assetType: query.assetType || undefined,
      status: query.status || undefined,
      deptId: query.deptId || undefined,
    },
  })
  if (r.data?.success) {
    rows.value = r.data.data.records
    total.value = r.data.data.total
  }
}

const resetForm = () => {
  dialog.form = {
    assetId: '',
    assetType: assetTypes[0],
    assetName: '',
    model: '',
    specification: '',
    hostModel: '',
    hostSpec: '',
    monitorModel: '',
    monitorSpec: '',
    purchaseDate: '',
    deptId: 'dept-it',
    userId: '',
    status: '闲置',
    remark: '',
  }
}

const openCreate = () => {
  dialog.mode = 'create'
  dialog.editingId = ''
  resetForm()
  dialog.visible = true
}

const openEdit = async (row: Asset) => {
  dialog.mode = 'edit'
  dialog.editingId = row.assetId
  
  let hostModel = ''
  let hostSpec = ''
  let monitorModel = ''
  let monitorSpec = ''
  
  if (row.assetType === 'PC-组装机') {
    try {
      const r = await http.get(`/api/assets/${row.assetId}/assembly`)
      if (r.data?.success && r.data.data) {
        hostModel = r.data.data.hostModel || ''
        hostSpec = r.data.data.hostSpec || ''
        monitorModel = r.data.data.monitorModel || ''
        monitorSpec = r.data.data.monitorSpec || ''
      }
    } catch (e) {
      console.warn('获取组装机配置失败', e)
    }
  }
  
  dialog.form = {
    assetId: row.assetId,
    assetType: row.assetType,
    assetName: row.assetName,
    model: row.model,
    specification: row.specification || '',
    hostModel,
    hostSpec,
    monitorModel,
    monitorSpec,
    purchaseDate: row.purchaseDate,
    deptId: row.deptId,
    userId: row.userId || '',
    status: row.status,
    remark: row.remark || '',
  }
  dialog.visible = true
}

const submit = async () => {
  dialog.loading = true
  try {
    if (dialog.mode === 'create') {
      const { hostModel, hostSpec, monitorModel, monitorSpec, ...rest } = dialog.form
      const payload = { ...rest, userId: rest.userId || null }
      const r = await http.post('/api/assets', payload)
      if (!r.data?.success) throw new Error(r.data?.message || '提交失败')
      
      const newAssetId = r.data.data.assetId || dialog.form.assetId
      if (dialog.form.assetType === 'PC-组装机') {
        await http.put(`/api/assets/${newAssetId}/assembly`, {
          hostModel, hostSpec, monitorModel, monitorSpec
        })
      }
      ElMessage.success('新增成功')
    } else {
      const { assetId, hostModel, hostSpec, monitorModel, monitorSpec, ...rest } = dialog.form
      const payload = { ...rest, userId: rest.userId || null }
      const r = await http.put(`/api/assets/${dialog.editingId}`, payload)
      if (!r.data?.success) throw new Error(r.data?.message || '提交失败')
      
      if (dialog.form.assetType === 'PC-组装机') {
        await http.put(`/api/assets/${dialog.editingId}/assembly`, {
          hostModel, hostSpec, monitorModel, monitorSpec
        })
      }
      ElMessage.success('保存成功')
    }
    dialog.visible = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提交失败')
  } finally {
    dialog.loading = false
  }
}

const goDetail = (assetId: string) => router.push(`/assets/${assetId}`)

const onSelectionChange = (rows: Asset[]) => {
  selectedIds.value = rows.map(r => r.assetId)
}

const printSelected = async () => {
  if (selectedIds.value.length === 0) return
  try {
    const r = await http.post('/api/assets/qrcodes/print', { assetIds: selectedIds.value, cols: 2, rows: 4 }, { responseType: 'blob' })
    const url = URL.createObjectURL(r.data)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 10000)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '打印失败')
  }
}

const exportExcel = async () => {
  try {
    const r = await http.get('/api/assets/export', { responseType: 'blob' })
    const url = URL.createObjectURL(r.data)
    const a = document.createElement('a')
    a.href = url
    a.download = '资产明细.xlsx'
    a.click()
    setTimeout(() => URL.revokeObjectURL(url), 10000)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '导出失败')
  }
}

const importExcel = async (options: UploadRequestOptions) => {
  console.log('开始导入Excel文件:', options.file)
  const form = new FormData()
  form.append('file', options.file)
  try {
    console.log('发送导入请求...')
    // 移除手动设置的 Content-Type，让 axios 自动处理
    const r = await http.post('/api/assets/import', form)
    console.log('导入请求响应:', r)
    if (!r.data?.success) throw new Error(r.data?.message || '导入失败')
    const data = r.data.data
    if (data.failCount > 0) {
      ElMessage.warning(`导入完成：成功${data.successCount}，失败${data.failCount}（仅展示前50条）`)
    } else {
      ElMessage.success(`导入完成：成功${data.successCount}`)
    }
    await load()
  } catch (e: any) {
    console.error('导入失败:', e)
    console.error('错误响应:', e?.response)
    ElMessage.error(e?.response?.data?.message || e?.message || '导入失败')
  }
}

onMounted(() => {
  const q: any = route.query || {}
  if (typeof q.keyword === 'string') query.keyword = q.keyword
  if (typeof q.assetType === 'string') query.assetType = q.assetType
  if (typeof q.status === 'string') query.status = q.status
  if (typeof q.deptId === 'string') query.deptId = q.deptId
  load()
})
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.title {
  font-size: 18px;
  font-weight: 600;
  color: #333333;
}
.spacer {
  flex: 1;
}
.filters {
  margin-bottom: 8px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
