<template>
  <el-card>
    <div class="title">扫码查询</div>
    <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="onFile">
      <el-button type="primary">上传二维码图片</el-button>
    </el-upload>

    <div v-if="decoded" class="section">
      <div class="label">识别结果</div>
      <el-input v-model="decoded" type="textarea" :rows="3" readonly />
      <el-button class="mt" type="primary" :loading="loading" @click="resolve">解析并查询</el-button>
    </div>

    <div v-if="resolved" class="section">
      <div class="label">资产信息</div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="资产编号">{{ resolved.asset?.assetId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ resolved.asset?.assetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ resolved.asset?.assetType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ resolved.asset?.status || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="mt">
        <div class="label">最近变动</div>
        <el-table :data="resolved.history || []">
          <el-table-column prop="changeTime" label="时间" width="180" />
          <el-table-column prop="changeType" label="类型" width="120" />
          <el-table-column prop="reason" label="原因" />
        </el-table>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'
import { BrowserQRCodeReader } from '@zxing/library'
import { http } from '../api/http'

const decoded = ref('')
const loading = ref(false)
const resolved = ref<any>(null)

const onFile = async (file: UploadFile) => {
  resolved.value = null
  decoded.value = ''
  const raw = file.raw
  if (!raw) return
  try {
    const dataUrl = await readAsDataUrl(raw)
    const text = await decodeFromDataUrl(dataUrl)
    decoded.value = text
  } catch (e: any) {
    ElMessage.error(e?.message || '识别失败')
  }
}

const resolve = async () => {
  if (!decoded.value) return
  loading.value = true
  try {
    const r = await http.post('/api/public/qrcode/resolve', { content: decoded.value })
    if (!r.data?.success) throw new Error(r.data?.message || '解析失败')
    resolved.value = r.data.data
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '解析失败')
  } finally {
    loading.value = false
  }
}

const readAsDataUrl = (raw: File) =>
  new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = () => reject(new Error('读取图片失败'))
    reader.readAsDataURL(raw)
  })

const decodeFromDataUrl = async (dataUrl: string) => {
  const img = new Image()
  img.src = dataUrl
  await new Promise<void>((resolve, reject) => {
    img.onload = () => resolve()
    img.onerror = () => reject(new Error('图片加载失败'))
  })

  const reader = new BrowserQRCodeReader()
  const result = await reader.decodeFromImageElement(img)
  return result.getText()
}
</script>

<style scoped>
.title {
  font-size: 18px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 16px;
}
.section {
  margin-top: 16px;
}
.label {
  margin-bottom: 8px;
  color: #333333;
  font-weight: 600;
}
.mt {
  margin-top: 12px;
}
</style>
