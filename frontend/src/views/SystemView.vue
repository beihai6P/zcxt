<template>
  <el-card>
    <template #header>
      <div class="title">系统数据管理</div>
    </template>

    <div class="section">
      <h3>数据备份与恢复</h3>
      <p class="desc">支持手动备份与自动定时备份双重模式。数据备份包含所有数据表数据与文件存储内容。</p>
      
      <div class="actions">
        <el-button type="primary" :loading="backingUp" @click="handleBackup">执行数据备份</el-button>
        <el-button type="warning" @click="openRestore">数据恢复</el-button>
      </div>

      <el-divider />

      <h3>数据清理</h3>
      <p class="desc">清理过期数据（如报废资产超过1年的明细记录），清理前需提交清理申请，由超级管理员审批。</p>
      
      <div class="actions">
        <el-button type="danger" @click="handleClean">执行数据清理</el-button>
      </div>
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" title="数据恢复" width="500px">
    <el-alert
      title="高风险操作"
      type="warning"
      description="数据恢复后会覆盖现有数据，该操作不可逆，请确认是否继续？"
      show-icon
      :closable="false"
      style="margin-bottom: 20px"
    />
    <el-form label-width="100px">
      <el-form-item label="选择备份文件">
        <el-select v-model="selectedBackup" placeholder="请选择历史备份文件" style="width: 100%">
          <el-option v-for="b in backups" :key="b" :label="b" :value="b" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="danger" :disabled="!selectedBackup" :loading="restoring" @click="handleRestore">确认恢复</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../api/http'

const backingUp = ref(false)
const dialogVisible = ref(false)
const restoring = ref(false)
const backups = ref<string[]>([])
const selectedBackup = ref('')

const loadBackups = async () => {
  const r = await http.get('/api/system/backups')
  if (r.data?.success) backups.value = r.data.data
}

const openRestore = async () => {
  selectedBackup.value = ''
  dialogVisible.value = true
  try {
    await loadBackups()
  } catch (e) {
  }
}

const handleBackup = async () => {
  backingUp.value = true
  try {
    const r = await http.post('/api/system/backup')
    if (r.data?.success) {
      const fileName = r.data.data?.fileName
      ElMessage.success('数据备份成功')
      if (fileName) {
        const blob = await http.get(`/api/system/backups/${encodeURIComponent(fileName)}`, { responseType: 'blob' })
        const url = URL.createObjectURL(blob.data)
        const a = document.createElement('a')
        a.href = url
        a.download = fileName
        a.click()
        setTimeout(() => URL.revokeObjectURL(url), 10000)
      }
    } else {
      throw new Error(r.data?.message || '备份失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '备份失败')
  } finally {
    backingUp.value = false
  }
}

const handleRestore = async () => {
  restoring.value = true
  try {
    const r = await http.post('/api/system/restore', { fileName: selectedBackup.value })
    if (r.data?.success) {
      ElMessage.success('数据恢复成功')
      dialogVisible.value = false
    } else {
      throw new Error(r.data?.message || '恢复失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '恢复失败')
  } finally {
    restoring.value = false
  }
}

const handleClean = () => {
  ElMessageBox.confirm(
    '此操作将永久清理过期数据, 是否继续?',
    '二次确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const r = await http.post('/api/system/clean?keep=10')
      if (r.data?.success) {
        ElMessage.success(`数据清理成功，已清理备份文件数量：${r.data.data}`)
      } else {
        throw new Error(r.data?.message || '清理失败')
      }
    } catch (e: any) {
      ElMessage.error(e?.message || '清理失败')
    }
  }).catch(() => {
    ElMessage.info('已取消清理')
  })
}
</script>

<style scoped>
.title {
  font-size: 18px;
  font-weight: 600;
  color: #333333;
}
.section {
  padding: 10px 0;
}
h3 {
  font-size: 16px;
  color: #333333;
  margin-bottom: 8px;
}
.desc {
  font-size: 14px;
  color: #666666;
  margin-bottom: 16px;
}
.actions {
  margin-bottom: 24px;
}
</style>
