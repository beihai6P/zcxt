<template>
  <div class="users-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
        </div>
      </template>
      
      <el-table :data="users" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="displayName" label="显示名称" width="180" />
        <el-table-column prop="roleId" label="角色" width="180">
          <template #default="{ row }">
            {{ getRoleName(row.roleId) }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="updateUserStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteUser(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建用户对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增用户" width="500px">
      <el-form :model="createForm" :rules="rules" ref="createFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="createForm.displayName" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="createForm.roleId" placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createUser">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="editForm.password" type="password" placeholder="留空表示不修改" />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="editForm.displayName" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="editForm.roleId" placeholder="请选择角色">
            <el-option v-for="role in roles" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateUser">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http as axios } from '../api/http'

interface User {
  userId: string
  username: string
  displayName: string
  roleId: string
  deptId: string | null
  email: string | null
  enabled: boolean
  createTime: string
  updateTime: string
}

interface Role {
  roleId: string
  roleName: string
  createTime: string
  updateTime: string
}

const users = ref<User[]>([])
const roles = ref<Role[]>([])
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const createFormRef = ref()
const editFormRef = ref()
const currentUser = ref<User | null>(null)

const createForm = ref({
  username: '',
  password: '',
  displayName: '',
  roleId: '',
  email: ''
})

const editForm = ref({
  username: '',
  password: '',
  displayName: '',
  roleId: '',
  email: '',
  enabled: true
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleName = (roleId: string) => {
  const role = roles.value.find(r => r.roleId === roleId)
  return role ? role.roleName : roleId
}

const loadUsers = async () => {
  try {
    const response = await axios.get('/api/users')
    users.value = response.data.data
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  }
}

const loadRoles = async () => {
  try {
    const response = await axios.get('/api/users/roles')
    roles.value = response.data.data
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  }
}

const openCreateDialog = () => {
  createForm.value = {
    username: '',
    password: '',
    displayName: '',
    roleId: '',
    email: ''
  }
  createDialogVisible.value = true
}

const openEditDialog = (user: User) => {
  currentUser.value = user
  editForm.value = {
    username: user.username,
    password: '',
    displayName: user.displayName,
    roleId: user.roleId,
    email: user.email || '',
    enabled: user.enabled
  }
  editDialogVisible.value = true
}

const createUser = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    await axios.post('/api/users', createForm.value)
    ElMessage.success('用户创建成功')
    createDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('用户创建失败')
  }
}

const updateUser = async () => {
  if (!editFormRef.value || !currentUser.value) return
  
  try {
    await editFormRef.value.validate()
    await axios.put(`/api/users/${currentUser.value.userId}`, editForm.value)
    ElMessage.success('用户更新成功')
    editDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('用户更新失败')
  }
}

const updateUserStatus = async (user: User) => {
  try {
    await axios.put(`/api/users/${user.userId}`, {
      displayName: user.displayName,
      roleId: user.roleId,
      email: user.email,
      enabled: user.enabled
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    user.enabled = !user.enabled // 恢复原来的状态
  }
}

const deleteUser = async (user: User) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.delete(`/api/users/${user.userId}`)
    ElMessage.success('用户删除成功')
    loadUsers()
  } catch (error) {
    // 取消删除操作
  }
}

onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>

<style scoped>
.users-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
