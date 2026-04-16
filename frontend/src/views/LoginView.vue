<template>
  <div class="wrap">
    <el-card class="card">
      <div class="title">登录</div>
      <el-form :model="form" label-width="90px" @keyup.enter="submit">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="hint">默认账号：admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

const submit = async () => {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const r = await http.post('/api/auth/login', form)
    const body = r.data
    if (!body?.success) {
      ElMessage.error(body?.message || '登录失败')
      return
    }
    auth.setSession(body.data.token, {
      userId: body.data.userId,
      username: body.data.username,
      displayName: body.data.displayName,
      roleId: body.data.roleId,
      deptId: body.data.deptId,
    })
    await router.push('/dashboard')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.card {
  width: 420px;
  border-radius: 8px;
}
.title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333333;
}
.hint {
  margin-top: 8px;
  color: #666666;
  font-size: 12px;
}
</style>

