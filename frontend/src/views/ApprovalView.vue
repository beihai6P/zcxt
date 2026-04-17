<template>
  <el-card>
    <div class="header">
      <div class="title">资产审批</div>
      <div class="spacer" />
      <el-button @click="load">刷新</el-button>
    </div>

    <el-form :inline="true" class="filters">
      <el-form-item label="资产编号">
        <el-input v-model="query.assetId" placeholder="请输入资产编号" style="width: 220px" clearable />
      </el-form-item>
      <el-form-item label="审批状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" row-key="approvalId">
      <el-table-column prop="approvalId" label="审批单号" width="160" />
      <el-table-column prop="assetId" label="资产编号" width="160" />
      <el-table-column prop="applicantId" label="申请人" width="120" />
    <el-table-column prop="applyType" label="申请类型" width="120" />
    <el-table-column prop="applyReason" label="申请理由" min-width="200" show-overflow-tooltip />
    <el-table-column prop="targetStatus" label="目标状态" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === '待审批'" link type="primary" @click="openApprove(row)">审批</el-button>
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

  <el-dialog v-model="dialog.visible" title="处理审批" width="500px">
    <el-form :model="dialog.form" label-width="100px">
      <el-form-item label="审批结果">
        <el-radio-group v-model="dialog.form.status">
          <el-radio label="已通过">通过</el-radio>
          <el-radio label="已拒绝">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审批意见">
        <el-input v-model="dialog.form.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="dialog.loading" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'

type AssetApproval = {
  approvalId: string
  assetId: string
  applicantId: string
  approverId: string | null
  applyType: string
  status: string
  applyReason: string | null
  approveRemark: string | null
  targetDeptId: string | null
  targetUserId: string | null
  targetStatus: string | null
  applyTime: string
  approveTime: string | null
}

const statuses = ['待审批', '已通过', '已拒绝']

const rows = ref<AssetApproval[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive({ assetId: '', status: '' })

const dialog = reactive({
  visible: false,
  loading: false,
  approvalId: '',
  form: {
    status: '已通过',
    comment: '',
  },
})

const getStatusType = (status: string) => {
  if (status === '待审批') return 'warning'
  if (status === '已通过') return 'success'
  if (status === '已拒绝') return 'danger'
  return 'info'
}

const load = async () => {
  const r = await http.get('/api/assets/approval', {
    params: {
      page: page.value,
      size: size.value,
      assetId: query.assetId || undefined,
      status: query.status || undefined,
    },
  })
  if (r.data?.success) {
    rows.value = r.data.data.records
    total.value = r.data.data.total
  }
}

const openApprove = (row: AssetApproval) => {
  dialog.approvalId = row.approvalId
  dialog.form.status = '已通过'
  dialog.form.comment = ''
  dialog.visible = true
}

const submit = async () => {
  dialog.loading = true
  try {
    const r = await http.post(`/api/assets/approval/${dialog.approvalId}/approve`, dialog.form)
    if (!r.data?.success) throw new Error(r.data?.message || '提交失败')
    ElMessage.success('审批成功')
    dialog.visible = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提交失败')
  } finally {
    dialog.loading = false
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
