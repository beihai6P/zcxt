<template>
  <el-card>
    <div class="header">
      <div class="title">系统日志</div>
      <div class="spacer" />
      <el-button @click="load">刷新</el-button>
    </div>

    <el-form :inline="true" class="filters">
      <el-form-item label="操作人">
        <el-input v-model="query.username" placeholder="请输入操作人" style="width: 160px" clearable />
      </el-form-item>
      <el-form-item label="操作内容">
        <el-input v-model="query.operation" placeholder="请输入操作内容" style="width: 220px" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" row-key="logId">
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="operation" label="操作内容" width="180" />
      <el-table-column prop="method" label="请求方法" min-width="200" show-overflow-tooltip />
      <el-table-column prop="params" label="请求参数" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP地址" width="140" />
      <el-table-column prop="time" label="耗时(ms)" width="100" />
      <el-table-column prop="createTime" label="操作时间" width="180" />
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
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { http } from '../api/http'

type SysLog = {
  logId: string
  userId: string
  username: string
  operation: string
  method: string
  params: string
  time: number
  ip: string
  createTime: string
}

const rows = ref<SysLog[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const query = reactive({ username: '', operation: '' })

const load = async () => {
  const r = await http.get('/api/system/logs', {
    params: {
      page: page.value,
      size: size.value,
      username: query.username || undefined,
      operation: query.operation || undefined,
    },
  })
  if (r.data?.success) {
    rows.value = r.data.data.records
    total.value = r.data.data.total
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
