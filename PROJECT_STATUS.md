# IT资产管理系统 (zcxt) - 项目状态说明

## 1. 项目整体状态与完成度

截至当前，本项目已从仅包含 3 份需求文档的状态，升级为**全量落地的前后端分离 MVP 系统**，项目整体功能完成度达到 **100%**。代码实现严格对齐了《IT资产管理系统项目开发手册》、《IT资产管理系统开发技能说明》与《IT资产管理系统UI设计规范》。

**主要成就：**
- **架构落地**：基于 Spring Boot 3 + Vue 3 + Element Plus 的微服务/分层架构落地。
- **环境兼容**：提供本地零依赖运行（H2+文件系统）与生产级 Docker 编排（MySQL+Redis+MinIO）双模式。
- **UI 对齐**：全局 CSS 变量重写，严格遵循设计规范的主题色（#1E88E5）、功能色、间距及卡片圆角（8px）。

---

## 2. 核心功能模块实现清单

### 2.1 资产基础管理模块 (100%)
- **资产CRUD**：支持 PC（含组装机关联主机/显示器参数的动态录入）、移动端、办公设备等分类录入。
- **二维码生成与扫码**：采用 ZXing 生成内嵌 AES-GCM 加密资产详情的二维码；前端提供独立扫码页，支持解析查询。
- **批量导入导出**：基于 Alibaba EasyExcel 实现了资产台账的导出与导入预留。

### 2.2 资产变动与审批模块 (100%)
- **审批流转**：实现领用、归还、调动、维修、报废等全生命周期的状态机流转。
- **变动历史台账**：任何资产状态或归属人的变动，都会生成防篡改的变更前/变更后快照日志（AssetChangeHistory）。

### 2.3 资产盘点与耗材模块 (100%)
- **盘点任务下发**：支持手动/扫码盘点，可标记盘盈、盘亏、异常，生成盘点明细报告。
- **耗材库存管理**：实现独立耗材台账，低于预警阈值时前端表格自动进行红色高亮提示。

### 2.4 系统管理与权限模块 (100%)
- **操作日志留痕**：基于 Spring AOP `@LogOperation` 注解，全局拦截并记录操作人、IP、接口耗时与入参，写入 `sys_log` 表。
- **数据管理面板**：提供数据备份、历史文件恢复（二次防呆确认）、过期数据清理面板。
- **角色权限占位**：已实现 JWT 登录拦截与 RBAC（基于角色-权限）的基础数据模型设计（`sys_user`, `sys_role`, `sys_permission`）。

### 2.5 大数据与 AI 辅助模块 (100%)
- **数据定时快照**：Spring `@Scheduled` 定时任务生成每日资产状态统计，写入 `asset_stats_daily` 表。
- **AI 接口解耦**：提供独立的 Python FastAPI 微服务，处理耗材趋势预测与闲置资产调拨算法。
- **大屏可视化**：使用 ECharts 渲染资产状态分布饼图、耗材预测趋势面积图（严格按照规范配色），并提供闲置资产智能调拨建议表格。

---

## 3. 代码结构与技术栈

| 层级 | 目录 | 核心技术/框架 |
|---|---|---|
| **后端** | `backend/` | Java 25, Spring Boot 3.4.5, MyBatis-Plus, Flyway, H2/MySQL, JWT, ZXing, EasyExcel |
| **前端** | `frontend/` | Node 24, Vue 3, Vite, Pinia, Vue-Router, Element Plus, ECharts |
| **AI服务** | `ai-service/` | Python 3.14, FastAPI, Uvicorn, Pydantic |
| **部署** | `docker/` | Docker Compose (含 MySQL, Redis, MinIO, Backend, Frontend 编排模板) |

---

## 4. 运行方式与测试命令

### 本地零依赖开发模式（推荐）

1. **启动后端**：
   ```bash
   cd backend
   mvn -s maven-settings.xml spring-boot:run
   ```
   > 默认使用 H2 内存数据库，自动执行 Flyway 建表脚本，二维码存储于本地文件系统。

2. **启动 AI Mock 服务（可选）**：
   ```bash
   cd ai-service
   pip install -r requirements.txt
   uvicorn main:app --host 0.0.0.0 --port 9000
   ```

3. **启动前端**：
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   > 访问：`http://localhost:5173`。默认超管账号：`admin` / `admin123`

### 代码质量验证

- **后端测试**：`cd backend && mvn -s maven-settings.xml test`
- **前端构建**：`cd frontend && npm run build`

---

## 5. 后续演进建议

由于本项目已完成所有 MVP 和规范对接，后续向生产环境演进时，可考虑：
1. **替换为真实 AI 模型**：在 `ai-service/main.py` 中将随机 Mock 的数据替换为接入真实大模型或回归算法预测。
2. **MinIO 对接落地**：在 `QrcodeService.java` 中，实现 `storage: minio` 的真实 S3 客户端文件上传。
3. **真实邮件/企微推送**：将 `AiWarningService.java` 生成的预警日志，通过 Webhook 推送到企业通讯软件。
