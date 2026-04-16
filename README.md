# zcxt - IT资产管理系统（MVP）

本仓库原始内容仅包含项目说明文档；为满足文档要求，本仓库已补充一个可本地运行的 MVP（前后端 + 数据库迁移 + 二维码 + 基础大屏 + AI预警对接占位）。

## 目录

- backend：Spring Boot 后端（默认 dev 使用 H2，可切换 prod 连接 MySQL/Redis/MinIO）
- frontend：Vue3 前端（Element Plus + Pinia + Router + ECharts + ZXing）
- ai-service：Python FastAPI（AI预警演示服务）

## 本地运行（无 Docker）

### 1) 启动后端

```bash
cd backend
mvn -q -s maven-settings.xml -DskipTests=false test
mvn -q -s maven-settings.xml spring-boot:run
```

- Swagger UI：http://localhost:8080/swagger-ui/index.html
- H2 Console：http://localhost:8080/h2-console

默认登录账号：

- admin / admin123

### 2) 启动 AI 服务（可选）

```bash
cd ai-service
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 9000
```

开启后端 AI 对接：

```bash
export SPRING_PROFILES_ACTIVE=dev
export APP_AI_ENABLED=true
export APP_AI_BASEURL=http://localhost:9000
cd backend
mvn -q -s maven-settings.xml spring-boot:run
```

### 3) 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问：http://localhost:5173

## 生产模式（有 Docker / 外部依赖）

当前环境缺少 Docker，仓库提供 docker-compose 模板用于你在有 Docker 的机器上启动：

```bash
cd docker
docker compose up -d --build
```

- 后端 profiles：
  - dev：H2 + 本地二维码存储（./data/qrcodes）
  - prod：按 application-prod.yml 连接 MySQL/Redis，并将二维码存储切换到 MinIO
