<template>
  <el-card>
    <div class="header">
      <div class="title">盘点任务管理</div>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新增任务</el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-form :inline="true" class="filters">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="任务名称" style="width: 220px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" row-key="inventoryId">
      <el-table-column prop="inventoryName" label="任务名称" min-width="160" />
      <el-table-column prop="scopeType" label="盘点范围" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="endTime" label="结束时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="viewDetails(row)">详情</el-button>
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

  <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增任务' : '编辑任务'" width="580px">
    <el-form :model="dialog.form" label-width="110px">
      <el-form-item label="任务名称" required>
        <el-input v-model="dialog.form.inventoryName" />
      </el-form-item>
      <template v-if="dialog.mode === 'create'">
        <el-form-item label="盘点范围" required>
          <el-select v-model="dialog.form.scopeType" placeholder="请选择">
            <el-option label="全局盘点" value="全局盘点" />
            <el-option label="部门盘点" value="部门盘点" />
            <el-option label="分类盘点" value="分类盘点" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门ID" v-if="dialog.form.scopeType === '部门盘点'">
          <el-input v-model="dialog.form.deptId" />
        </el-form-item>
        <el-form-item label="资产类型" v-if="dialog.form.scopeType === '分类盘点'">
          <el-input v-model="dialog.form.assetType" />
        </el-form-item>
        <el-form-item label="创建人ID" required>
          <el-input v-model="dialog.form.creatorId" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="dialog.form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="dialog.form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="状态">
          <el-select v-model="dialog.form.status" placeholder="请选择">
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
          </el-select>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="dialog.loading" @click="submit">提交</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailDialog.visible" title="盘点详情" width="800px">
    <div class="detail-actions">
      <el-button type="primary" @click="finishInventory">完成盘点</el-button>
      <el-button @click="loadDetails">刷新</el-button>
    </div>
    <el-table :data="detailDialog.rows">
      <el-table-column prop="assetId" label="资产编号" width="160" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="abnormalType" label="异常类型" width="120" />
      <el-table-column prop="abnormalReason" label="异常原因" min-width="160" />
      <el-table-column prop="checkTime" label="盘点时间" width="160" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="markNormal(row)">正常</el-button>
          <el-button link type="danger" @click="markAbnormal(row)">异常</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager" style="margin-top: 16px;">
      <el-pagination
        v-model:current-page="detailDialog.page"
        v-model:page-size="detailDialog.size"
        :total="detailDialog.total"
        background
        layout="prev, pager, next"
        @current-change="loadDetails"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../api/http'
import { useAuthStore } from '../stores/auth'

type Inventory = {
  inventoryId: string
  inventoryName: string
  scopeType: string
  deptId: string | null
  assetType: string | null
  creatorId: string
  status: string
  startTime: string | null
  endTime: string | null
}

const rows = ref<Inventory[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive({ keyword: '' })

const dialog = reactive({
  visible: false,
  mode: 'create' as 'create' | 'edit',
  loading: false,
  editingId: '',
  form: {
    inventoryName: '',
    scopeType: '全局盘点',
    deptId: '',
    assetType: '',
    creatorId: 'system',
    status: '进行中',
    startTime: '',
    endTime: '',
  },
})

const detailDialog = reactive({
  visible: false,
  inventoryId: '',
  rows: [],
  total: 0,
  page: 1,
  size: 10,
})

const auth = useAuthStore()

const load = async () => {
  const r = await http.get('/api/inventories', {
    params: {
      page: page.value,
      size: size.value,
      keyword: query.keyword || undefined,
    },
  })
  if (r.data?.success) {
    rows.value = r.data.data.records
    total.value = r.data.data.total
  }
}

const resetForm = () => {
  dialog.form = {
    inventoryName: '',
    scopeType: '全局盘点',
    deptId: '',
    assetType: '',
    creatorId: 'system',
    status: '进行中',
    startTime: '',
    endTime: '',
  }
}

const openCreate = () => {
  dialog.mode = 'create'
  dialog.editingId = ''
  resetForm()
  dialog.visible = true
}

const openEdit = (row: Inventory) => {
  dialog.mode = 'edit'
  dialog.editingId = row.inventoryId
  dialog.form = {
    ...dialog.form,
    inventoryName: row.inventoryName,
    status: row.status,
  }
  dialog.visible = true
}

const submit = async () => {
  if (!dialog.form.inventoryName) {
    ElMessage.warning('请输入任务名称')
    return
  }
  dialog.loading = true
  try {
    if (dialog.mode === 'create') {
      const payload = {
        inventoryName: dialog.form.inventoryName,
        scopeType: dialog.form.scopeType,
        deptId: dialog.form.deptId || null,
        assetType: dialog.form.assetType || null,
        creatorId: dialog.form.creatorId,
        startTime: dialog.form.startTime || null,
        endTime: dialog.form.endTime || null,
      }
      const r = await http.post('/api/inventories', payload)
      if (!r.data?.success) throw new Error(r.data?.message || '提交失败')
      ElMessage.success('新增成功')
    } else {
      const payload = {
        inventoryName: dialog.form.inventoryName,
        status: dialog.form.status,
      }
      const r = await http.put(`/api/inventories/${dialog.editingId}`, payload)
      if (!r.data?.success) throw new Error(r.data?.message || '提交失败')
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

const viewDetails = (row: Inventory) => {
  detailDialog.inventoryId = row.inventoryId
  detailDialog.page = 1
  detailDialog.visible = true
  loadDetails()
}

const loadDetails = async () => {
  const r = await http.get(`/api/inventories/${detailDialog.inventoryId}/details`, {
    params: {
      page: detailDialog.page,
      size: detailDialog.size,
    },
  })
  if (r.data?.success) {
    detailDialog.rows = r.data.data.records
    detailDialog.total = r.data.data.total
  }
}

const markNormal = async (row: any) => {
  try {
    const r = await http.post(`/api/inventories/${detailDialog.inventoryId}/check`, {
      assetId: row.assetId,
      status: '已盘点',
      abnormalType: null,
      abnormalReason: null,
      checkerId: auth.user?.userId || 'system',
    })
    if (!r.data?.success) throw new Error(r.data?.message || '操作失败')
    ElMessage.success('已标记为正常')
    await loadDetails()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  }
}

const markAbnormal = async (row: any) => {
  try {
    const res = await ElMessageBox.prompt('请输入异常原因', '标记异常', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：资产缺失/状态不符等',
    })
    const r = await http.post(`/api/inventories/${detailDialog.inventoryId}/check`, {
      assetId: row.assetId,
      status: '异常',
      abnormalType: '异常',
      abnormalReason: res.value,
      checkerId: auth.user?.userId || 'system',
    })
    if (!r.data?.success) throw new Error(r.data?.message || '操作失败')
    ElMessage.success('已标记为异常')
    await loadDetails()
  } catch (e: any) {
    if (e === 'cancel' || e?.message === 'cancel') return
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  }
}

const finishInventory = async () => {
  try {
    const r = await http.post(`/api/inventories/${detailDialog.inventoryId}/finish`)
    if (!r.data?.success) throw new Error(r.data?.message || '操作失败')
    ElMessage.success('盘点已完成')
    detailDialog.visible = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  }
}

onMounted(() => {
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
.detail-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
</style>
