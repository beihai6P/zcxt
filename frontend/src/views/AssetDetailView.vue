<template>
  <el-card v-if="asset">
    <div class="top">
      <div class="title">资产详情</div>
      <div class="spacer" />
      <el-button @click="printQr">打印二维码</el-button>
      <el-button @click="back">返回</el-button>
    </div>

    <el-descriptions :column="2" border>
      <el-descriptions-item label="资产编号">{{ asset.assetId }}</el-descriptions-item>
      <el-descriptions-item label="资产名称">{{ asset.assetName }}</el-descriptions-item>
      <el-descriptions-item label="类型">{{ asset.assetType }}</el-descriptions-item>
      <el-descriptions-item label="型号">{{ asset.model }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ asset.status }}</el-descriptions-item>
      <el-descriptions-item label="所属部门ID">{{ asset.deptId }}</el-descriptions-item>
      <el-descriptions-item label="使用人ID">{{ asset.userId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="采购日期">{{ asset.purchaseDate }}</el-descriptions-item>
    </el-descriptions>

    <div class="section">
      <div class="card-title">二维码</div>
      <el-image v-if="asset.qrcodeUrl" :src="asset.qrcodeUrl" style="width: 160px; height: 160px" />
    </div>

    <div class="section">
      <div class="card-title">图片与附件</div>
      <div class="attach-actions">
        <el-upload :show-file-list="false" :http-request="uploadFile">
          <el-button type="primary">上传附件</el-button>
        </el-upload>
      </div>
      <el-empty v-if="attachments.length === 0" description="暂无附件" />
      <div v-else class="attach-list">
        <el-card v-for="a in attachments" :key="a.attachmentId" class="attach-item" shadow="never">
          <div class="attach-row">
            <a :href="a.url" target="_blank" rel="noreferrer">{{ a.originalName }}</a>
            <div class="spacer" />
            <el-button link type="danger" @click="removeAttachment(a.attachmentId)">删除</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <div class="section">
      <div class="card-title">资产变动</div>
      <el-form :inline="true" :model="change">
        <el-form-item label="类型">
          <el-select v-model="change.changeType" style="width: 160px">
            <el-option v-for="t in changeTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="change.newStatus" style="width: 140px">
            <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门ID">
          <el-input v-model="change.deptId" style="width: 160px" />
        </el-form-item>
        <el-form-item label="使用人ID">
          <el-input v-model="change.userId" style="width: 160px" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="change.reason" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="changing" @click="submitChange">提交</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="section">
      <div class="card-title">变动历史</div>
      <el-table :data="history">
        <el-table-column prop="changeTime" label="时间" width="180" />
        <el-table-column prop="changeType" label="类型" width="120" />
        <el-table-column prop="reason" label="原因" />
        <el-table-column prop="operatorId" label="操作人" width="140" />
      </el-table>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import type { UploadRequestOptions } from 'element-plus'

const route = useRoute()
const router = useRouter()
const assetId = route.params.assetId as string

type Asset = {
  assetId: string
  assetType: string
  assetName: string
  model: string
  purchaseDate: string
  deptId: string
  userId: string | null
  status: string
  qrcodeUrl: string
}

type History = {
  historyId: string
  changeType: string
  changeTime: string
  reason: string
  operatorId: string
}

const asset = ref<Asset | null>(null)
const history = ref<History[]>([])
const attachments = ref<any[]>([])
const changing = ref(false)

const statuses = ['在用', '闲置', '维修', '报废']
const changeTypes = ['领用', '归还', '调动', '维修', '报废']

const change = reactive({
  changeType: '调动',
  newStatus: '在用',
  deptId: '',
  userId: '',
  approverId: '',
  reason: '',
})

const load = async () => {
  const a = await http.get(`/api/assets/${assetId}`)
  if (a.data?.success) asset.value = a.data.data

  const h = await http.get(`/api/assets/${assetId}/history`, { params: { page: 1, size: 50 } })
  if (h.data?.success) history.value = h.data.data.records

  const at = await http.get(`/api/files/assets/${assetId}`)
  if (at.data?.success) attachments.value = at.data.data
}

const submitChange = async () => {
  if (!asset.value) return
  changing.value = true
  try {
    const r = await http.post(`/api/assets/${assetId}/apply`, {
      applyType: change.changeType,
      applyReason: change.reason || null,
      targetStatus: change.newStatus,
      targetDeptId: change.deptId || null,
      targetUserId: change.userId || null,
    })
    if (!r.data?.success) throw new Error(r.data?.message || '提交失败')
    ElMessage.success('已提交审批')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提交失败')
  } finally {
    changing.value = false
  }
}

const printQr = async () => {
  try {
    const r = await http.post('/api/assets/qrcodes/print', { assetIds: [assetId], cols: 1, rows: 1 }, { responseType: 'blob' })
    const url = URL.createObjectURL(r.data)
    window.open(url, '_blank')
    setTimeout(() => URL.revokeObjectURL(url), 10000)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '打印失败')
  }
}

const uploadFile = async (options: UploadRequestOptions) => {
  const form = new FormData()
  form.append('assetId', assetId)
  form.append('file', options.file)
  try {
    const r = await http.post('/api/files/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (!r.data?.success) throw new Error(r.data?.message || '上传失败')
    ElMessage.success('上传成功')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '上传失败')
  }
}

const removeAttachment = async (id: string) => {
  try {
    const r = await http.delete(`/api/files/${id}`)
    if (!r.data?.success) throw new Error(r.data?.message || '删除失败')
    ElMessage.success('已删除')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
  }
}

const back = () => router.push('/assets')

onMounted(() => {
  load()
})
</script>

<style scoped>
.top {
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
.section {
  margin-top: 20px;
}
.attach-actions {
  margin-bottom: 12px;
}
.attach-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.attach-item {
  border-radius: 8px;
}
.attach-row {
  display: flex;
  align-items: center;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12px;
}
</style>
