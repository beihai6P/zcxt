<template>
  <el-card>
    <div class="header">
      <div class="title">耗材管理</div>
      <div class="spacer" />
      <el-button type="primary" @click="openCreate">新增耗材</el-button>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-form :inline="true" class="filters">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="名称/类型" style="width: 220px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" row-key="consumableId">
      <el-table-column prop="consumableName" label="耗材名称" min-width="160" />
      <el-table-column prop="consumableType" label="类型" width="140" />
      <el-table-column prop="stockQuantity" label="库存数量" width="100">
        <template #default="{ row }">
          <span :style="{ color: row.stockQuantity <= row.warningThreshold ? 'red' : 'inherit' }">
            {{ row.stockQuantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="warningThreshold" label="预警阈值" width="100" />
      <el-table-column prop="updateTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openInbound(row)">入库</el-button>
          <el-button link type="warning" @click="openOutbound(row)">出库</el-button>
          <el-button link type="primary" @click="openTxns(row)">流水</el-button>
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

  <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增耗材' : '编辑耗材'" width="500px">
    <el-form :model="dialog.form" label-width="100px">
      <el-form-item label="耗材名称" required>
        <el-input v-model="dialog.form.consumableName" />
      </el-form-item>
      <el-form-item label="耗材类型" required>
        <el-input v-model="dialog.form.consumableType" />
      </el-form-item>
      <el-form-item label="库存数量">
        <el-input-number v-model="dialog.form.stockQuantity" :min="0" />
      </el-form-item>
      <el-form-item label="预警阈值">
        <el-input-number v-model="dialog.form.warningThreshold" :min="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="dialog.loading" @click="submit">提交</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="txnDialog.visible" title="耗材流水" width="860px">
    <el-table :data="txnDialog.rows">
      <el-table-column prop="txnTime" label="时间" width="180" />
      <el-table-column prop="txnType" label="类型" width="80" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="deptId" label="部门" width="120" />
      <el-table-column prop="userId" label="使用人" width="120" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
    </el-table>
    <div class="pager" style="margin-top: 16px">
      <el-pagination
        v-model:current-page="txnDialog.page"
        v-model:page-size="txnDialog.size"
        :total="txnDialog.total"
        background
        layout="prev, pager, next, total"
        @current-change="loadTxns"
      />
    </div>
  </el-dialog>

  <el-dialog v-model="opDialog.visible" :title="opDialog.mode === 'in' ? '耗材入库' : '耗材出库'" width="520px">
    <el-form :model="opDialog.form" label-width="100px">
      <el-form-item label="数量" required>
        <el-input-number v-model="opDialog.form.quantity" :min="1" />
      </el-form-item>
      <el-form-item label="部门ID" v-if="opDialog.mode === 'out'">
        <el-input v-model="opDialog.form.deptId" />
      </el-form-item>
      <el-form-item label="使用人ID" v-if="opDialog.mode === 'out'">
        <el-input v-model="opDialog.form.userId" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="opDialog.form.remark" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="opDialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="opDialog.loading" @click="submitOp">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'

type Consumable = {
  consumableId: string
  consumableType: string
  consumableName: string
  stockQuantity: number
  warningThreshold: number
  updateTime: string
}

const rows = ref<Consumable[]>([])
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
    consumableName: '',
    consumableType: '',
    stockQuantity: 0,
    warningThreshold: 10,
  },
})

const txnDialog = reactive({
  visible: false,
  consumableId: '',
  rows: [] as any[],
  total: 0,
  page: 1,
  size: 10,
})

const opDialog = reactive({
  visible: false,
  loading: false,
  mode: 'in' as 'in' | 'out',
  consumableId: '',
  form: {
    quantity: 1,
    deptId: '',
    userId: '',
    remark: '',
  },
})

const load = async () => {
  const r = await http.get('/api/consumables', {
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
    consumableName: '',
    consumableType: '',
    stockQuantity: 0,
    warningThreshold: 10,
  }
}

const openCreate = () => {
  dialog.mode = 'create'
  dialog.editingId = ''
  resetForm()
  dialog.visible = true
}

const openEdit = (row: Consumable) => {
  dialog.mode = 'edit'
  dialog.editingId = row.consumableId
  dialog.form = {
    consumableName: row.consumableName,
    consumableType: row.consumableType,
    stockQuantity: row.stockQuantity,
    warningThreshold: row.warningThreshold,
  }
  dialog.visible = true
}

const submit = async () => {
  if (!dialog.form.consumableName || !dialog.form.consumableType) {
    ElMessage.warning('请填写名称和类型')
    return
  }
  dialog.loading = true
  try {
    const payload = { ...dialog.form }
    if (dialog.mode === 'create') {
      const r = await http.post('/api/consumables', payload)
      if (!r.data?.success) throw new Error(r.data?.message || '提交失败')
      ElMessage.success('新增成功')
    } else {
      const r = await http.put(`/api/consumables/${dialog.editingId}`, payload)
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

const openTxns = (row: Consumable) => {
  txnDialog.visible = true
  txnDialog.consumableId = row.consumableId
  txnDialog.page = 1
  loadTxns()
}

const loadTxns = async () => {
  const r = await http.get(`/api/consumables/${txnDialog.consumableId}/txns`, {
    params: { page: txnDialog.page, size: txnDialog.size },
  })
  if (r.data?.success) {
    txnDialog.rows = r.data.data.records
    txnDialog.total = r.data.data.total
  }
}

const openInbound = (row: Consumable) => {
  opDialog.mode = 'in'
  opDialog.consumableId = row.consumableId
  opDialog.form = { quantity: 1, deptId: '', userId: '', remark: '' }
  opDialog.visible = true
}

const openOutbound = (row: Consumable) => {
  opDialog.mode = 'out'
  opDialog.consumableId = row.consumableId
  opDialog.form = { quantity: 1, deptId: '', userId: '', remark: '' }
  opDialog.visible = true
}

const submitOp = async () => {
  opDialog.loading = true
  try {
    if (opDialog.mode === 'in') {
      const r = await http.post(`/api/consumables/${opDialog.consumableId}/in`, {
        quantity: opDialog.form.quantity,
        remark: opDialog.form.remark || null,
      })
      if (!r.data?.success) throw new Error(r.data?.message || '操作失败')
      ElMessage.success('入库成功')
    } else {
      const r = await http.post(`/api/consumables/${opDialog.consumableId}/out`, {
        quantity: opDialog.form.quantity,
        deptId: opDialog.form.deptId || null,
        userId: opDialog.form.userId || null,
        remark: opDialog.form.remark || null,
      })
      if (!r.data?.success) throw new Error(r.data?.message || '操作失败')
      ElMessage.success('出库成功')
    }
    opDialog.visible = false
    await load()
    if (txnDialog.visible && txnDialog.consumableId === opDialog.consumableId) {
      await loadTxns()
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  } finally {
    opDialog.loading = false
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
</style>
