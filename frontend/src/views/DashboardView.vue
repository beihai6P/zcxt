<template>
  <el-row :gutter="20">
    <el-col :span="6">
      <el-card>
        <div class="kpi">
          <div class="kpi-label">资产总数</div>
          <div class="kpi-value">{{ stats?.total ?? '-' }}</div>
        </div>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card>
        <div class="kpi">
          <div class="kpi-label">在用</div>
          <div class="kpi-value">{{ stats?.inUse ?? '-' }}</div>
        </div>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card>
        <div class="kpi">
          <div class="kpi-label">闲置</div>
          <div class="kpi-value">{{ stats?.idle ?? '-' }}</div>
        </div>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card>
        <div class="kpi">
          <div class="kpi-label">预警</div>
          <div class="kpi-value">{{ warnings.length }}</div>
        </div>
      </el-card>
    </el-col>
  </el-row>

  <el-row :gutter="20" class="mt">
    <el-col :span="16">
      <el-card>
        <div class="card-title">资产状态分布</div>
        <div ref="chartEl" class="chart" />
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card>
        <div class="card-title">
          <span>AI预警</span>
          <el-button type="primary" size="small" @click="syncAiWarnings" style="float: right;">手动同步预警</el-button>
        </div>
        <el-empty v-if="warnings.length === 0" description="暂无预警" />
        <el-timeline v-else>
          <el-timeline-item v-for="w in warnings" :key="w.warningId" :timestamp="w.warningTime">
            <div class="warn-type">{{ w.warningType }}</div>
            <div class="warn-content">{{ w.content }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </el-col>
  </el-row>
  <el-row :gutter="20" class="mt">
    <el-col :span="12">
      <el-card>
        <div class="card-title">耗材消耗预测趋势</div>
        <div ref="consumableChartEl" class="chart" />
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <div class="card-title">闲置资产调拨推荐</div>
        <el-table :data="idleRecommendations" height="360" style="width: 100%">
          <el-table-column prop="asset_name" label="资产名称" width="150" />
          <el-table-column prop="idle_days" label="闲置天数" width="100" />
          <el-table-column prop="recommendation" label="推荐操作" />
        </el-table>
      </el-card>
    </el-col>
  </el-row>

  <el-row :gutter="20" class="mt">
    <el-col :span="12">
      <el-card>
        <div class="card-title">部门资产分布</div>
        <div ref="deptChartEl" class="chart" />
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <div class="card-title">资产变动趋势</div>
        <div ref="changeChartEl" class="chart" />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { http } from '../api/http'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

type Stats = {
  total: number
  inUse: number
  idle: number
  repairing: number
  scrapped: number
}

type WarningItem = {
  warningId: string
  warningType: string
  content: string
  warningTime: string
}

type IdleRecommendationItem = {
  asset_id: string
  asset_name: string
  idle_days: number
  recommendation: string
}

type ConsumablePredictionItem = {
  date: string
  predicted_usage: number
}

type DeptStats = {
  deptId: string
  deptName: string
  total: number
}

type TrendPoint = {
  date: string
  count: number
}

const stats = ref<Stats | null>(null)
const warnings = ref<WarningItem[]>([])
const idleRecommendations = ref<IdleRecommendationItem[]>([])
const consumablePredictions = ref<ConsumablePredictionItem[]>([])
const deptStats = ref<DeptStats[]>([])
const changeTrend = ref<TrendPoint[]>([])
const chartEl = ref<HTMLDivElement | null>(null)
const consumableChartEl = ref<HTMLDivElement | null>(null)
const deptChartEl = ref<HTMLDivElement | null>(null)
const changeChartEl = ref<HTMLDivElement | null>(null)
let chart: echarts.ECharts | null = null
let consumableChart: echarts.ECharts | null = null
let deptChart: echarts.ECharts | null = null
let changeChart: echarts.ECharts | null = null

const router = useRouter()

const load = async () => {
  const s = await http.get('/api/stats/today?refresh=true')
  if (s.data?.success) stats.value = s.data.data

  const w = await http.get('/api/ai/warnings?limit=10')
  if (w.data?.success) warnings.value = w.data.data

  const ir = await http.get('/api/stats/ai/idle-recommendations')
  if (ir.data?.success) idleRecommendations.value = ir.data.data

  const cp = await http.get('/api/stats/ai/consumable-predictions')
  if (cp.data?.success) consumablePredictions.value = cp.data.data

  const ds = await http.get('/api/stats/by-dept')
  if (ds.data?.success) deptStats.value = ds.data.data

  const ct = await http.get('/api/stats/change-trend?days=30')
  if (ct.data?.success) changeTrend.value = ct.data.data
}

const syncAiWarnings = async () => {
  try {
    const r = await http.get('/api/ai/sync')
    if (r.data?.success) {
      ElMessage.success(`同步成功，新增 ${r.data.data} 条预警`)
      await load()
    } else {
      ElMessage.error(r.data?.message || '同步失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '同步失败')
  }
}

const render = () => {
  if (!chartEl.value || !stats.value) return
  if (!chart) chart = echarts.init(chartEl.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        data: [
          { name: '在用', value: stats.value.inUse, itemStyle: { color: '#4CAF50' } },
          { name: '闲置', value: stats.value.idle, itemStyle: { color: '#64B5F6' } },
          { name: '维修', value: stats.value.repairing, itemStyle: { color: '#FFC107' } },
          { name: '报废', value: stats.value.scrapped, itemStyle: { color: '#F44336' } },
        ],
      },
    ],
  })

  chart.off('click')
  chart.on('click', (params: any) => {
    if (!params?.name) return
    router.push({ path: '/assets', query: { status: params.name } })
  })
}

const renderConsumableChart = () => {
  if (!consumableChartEl.value || consumablePredictions.value.length === 0) return
  if (!consumableChart) consumableChart = echarts.init(consumableChartEl.value)
  
  const dates = consumablePredictions.value.map(p => p.date)
  const usages = consumablePredictions.value.map(p => p.predicted_usage)

  consumableChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: usages,
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(30, 136, 229, 0.5)' },
            { offset: 1, color: 'rgba(30, 136, 229, 0.0)' }
          ])
        },
        itemStyle: { color: '#1E88E5' }
      }
    ]
  })
}

const renderDeptChart = () => {
  if (!deptChartEl.value || deptStats.value.length === 0) return
  if (!deptChart) deptChart = echarts.init(deptChartEl.value)
  const names = deptStats.value.map(d => d.deptName)
  const values = deptStats.value.map(d => d.total)
  deptChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: names, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: values, itemStyle: { color: '#1E88E5' } }],
  })
  deptChart.off('click')
  deptChart.on('click', (params: any) => {
    const idx = params?.dataIndex
    if (idx == null) return
    const dept = deptStats.value[idx]
    router.push({ path: '/assets', query: { deptId: dept.deptId } })
  })
}

const renderChangeChart = () => {
  if (!changeChartEl.value || changeTrend.value.length === 0) return
  if (!changeChart) changeChart = echarts.init(changeChartEl.value)
  const dates = changeTrend.value.map(d => d.date)
  const counts = changeTrend.value.map(d => d.count)
  changeChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, data: counts, itemStyle: { color: '#FFC107' } }],
  })
}

onMounted(async () => {
  await load()
  render()
  renderConsumableChart()
  renderDeptChart()
  renderChangeChart()
})

watch(stats, () => render())
watch(consumablePredictions, () => renderConsumableChart())
watch(deptStats, () => renderDeptChart())
watch(changeTrend, () => renderChangeChart())
</script>

<style scoped>
.mt {
  margin-top: 20px;
}
.kpi {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.kpi-label {
  color: #666666;
}
.kpi-value {
  font-size: 24px;
  color: #333333;
}
.chart {
  height: 360px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 16px;
}
.warn-type {
  color: #333333;
  font-weight: 600;
}
.warn-content {
  color: #666666;
}
</style>
