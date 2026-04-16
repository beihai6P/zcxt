import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AppLayout from '../layout/AppLayout.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import AssetsView from '../views/AssetsView.vue'
import AssetDetailView from '../views/AssetDetailView.vue'
import ScanView from '../views/ScanView.vue'
import InventoryView from '../views/InventoryView.vue'
import ConsumablesView from '../views/ConsumablesView.vue'
import SysLogView from '../views/SysLogView.vue'
import ApprovalView from '../views/ApprovalView.vue'
import SystemView from '../views/SystemView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: DashboardView },
        { path: 'assets', component: AssetsView },
        { path: 'assets/:assetId', component: AssetDetailView, props: true },
        { path: 'scan', component: ScanView },
        { path: 'inventory', component: InventoryView },
        { path: 'consumables', component: ConsumablesView },
        { path: 'syslog', component: SysLogView },
        { path: 'system', component: SystemView },
        { path: 'approvals', component: ApprovalView },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path !== '/login' && !auth.isAuthed) return '/login'
  if (to.path === '/login' && auth.isAuthed) return '/dashboard'
  return true
})

export default router

