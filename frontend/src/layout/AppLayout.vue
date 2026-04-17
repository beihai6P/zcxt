<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">IT资产管理系统</div>
      <div class="spacer" />
      <el-dropdown>
        <span class="user">{{ auth.user?.displayName || auth.user?.username }}</span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-header>

    <el-container>
      <el-aside class="aside" width="220px">
        <el-menu :default-active="route.path" class="menu" router>
          <el-menu-item v-if="auth.hasPerm('stats:view')" index="/dashboard">大屏概览</el-menu-item>
          <el-menu-item v-if="auth.hasPerm('asset:read')" index="/assets">资产管理</el-menu-item>
          <el-menu-item v-if="auth.hasPerm('approval:read')" index="/approvals">审批管理</el-menu-item>
          <el-menu-item v-if="auth.hasPerm('inventory:manage')" index="/inventory">资产盘点</el-menu-item>
          <el-menu-item v-if="auth.hasPerm('consumable:manage')" index="/consumables">耗材管理</el-menu-item>
          <el-menu-item v-if="auth.hasPerm('sys:manage')" index="/syslog">系统日志</el-menu-item>
          <el-menu-item v-if="auth.hasPerm('user:manage')" index="/users">用户管理</el-menu-item>
          <el-menu-item v-if="auth.hasPerm('sys:manage')" index="/system">系统设置</el-menu-item>
          <el-menu-item index="/scan">扫码查询</el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const logout = () => {
  auth.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid #eeeeee;
}
.brand {
  font-size: 20px;
  font-weight: 600;
  color: #1e88e5;
}
.spacer {
  flex: 1;
}
.user {
  color: #333333;
  cursor: pointer;
}
.aside {
  background: #ffffff;
  border-right: 1px solid #eeeeee;
}
.menu {
  border-right: 0;
}
.main {
  padding: 20px;
}
</style>
