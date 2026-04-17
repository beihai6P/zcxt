# IT资产管理系统 (zcxt) - 生产环境部署说明

## 1. 项目概述

IT资产管理系统是一个基于 Spring Boot 3 + Vue 3 + Element Plus 的前后端分离系统，包含以下核心模块：
- 资产基础管理模块
- 资产变动与审批模块
- 资产盘点与耗材模块
- 系统管理与权限模块
- 大数据与 AI 辅助模块

## 2. 环境要求

### 2.1 硬件要求
- 推荐配置：4核8G内存以上服务器
- 存储空间：50GB以上

### 2.2 软件要求
- Docker 20.10.0+
- Docker Compose 1.29.0+

## 3. 部署步骤

### 3.1 准备工作

1. **克隆代码仓库**
   ```bash
   git clone <仓库地址>
   cd zcxt
   ```

2. **配置环境变量**
   - 编辑 `docker/docker-compose.yml` 文件，修改以下配置：
     - `LLM_API_KEY`：设置为实际的大模型 API 密钥
     - `APP_SECURITY_JWT_SECRET`：设置为安全的 JWT 密钥
     - `APP_CRYPTO_AESKEY`：设置为安全的加密密钥

### 3.2 构建和启动服务

1. **进入 docker 目录**
   ```bash
   cd docker
   ```

2. **构建并启动服务**
   ```bash
   docker-compose up --build -d
   ```

3. **验证服务状态**
   ```bash
   docker-compose ps
   ```

## 4. 服务访问

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://服务器IP:5173 | 系统登录页面 |
| 后端 API | http://服务器IP:8080 | 后端接口服务 |
| AI 服务 | http://服务器IP:9000 | AI 辅助服务 |
| MinIO 控制台 | http://服务器IP:9002 | 对象存储管理 |

### 4.1 默认登录信息
- 用户名：admin
- 密码：admin123

## 5. 配置说明

### 5.1 数据库配置
- MySQL 端口：3306
- 数据库名：zcxt
- 用户名：root
- 密码：root（可在 docker-compose.yml 中修改）

### 5.2 Redis 配置
- Redis 端口：6379
- 默认无密码

### 5.3 MinIO 配置
- 访问端口：9001
- 控制台端口：9002
- 用户名：minioadmin
- 密码：minioadmin（可在 docker-compose.yml 中修改）
- 存储桶：zcxt

### 5.4 环境变量配置

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | Spring 配置文件激活环境 | prod |
| APP_SECURITY_JWT_SECRET | JWT 密钥 | change-me-in-prod |
| APP_CRYPTO_AESKEY | AES 加密密钥 | change-me-in-prod |
| APP_AI_ENABLED | AI 服务启用状态 | true |
| APP_AI_BASEURL | AI 服务基础 URL | http://ai:9000 |
| LLM_API_KEY | 大模型 API 密钥 | your-api-key-here |
| LLM_API_URL | 大模型 API 地址 | https://api.openai.com/v1/chat/completions |
| LLM_MODEL | 大模型名称 | gpt-3.5-turbo |

## 6. 服务管理

### 6.1 启动服务
```bash
docker-compose up -d
```

### 6.2 停止服务
```bash
docker-compose down
```

### 6.3 查看服务日志
```bash
docker-compose logs -f <服务名>
```

### 6.4 重启服务
```bash
docker-compose restart <服务名>
```

## 7. 故障排查

### 7.1 常见问题

1. **服务启动失败**
   - 检查 Docker 服务是否正常运行
   - 检查端口是否被占用
   - 查看服务日志获取详细错误信息

2. **数据库连接失败**
   - 检查 MySQL 服务是否正常启动
   - 验证数据库连接配置是否正确

3. **前端无法访问后端 API**
   - 检查后端服务是否正常运行
   - 验证前端 API 基础 URL 配置是否正确

4. **AI 服务调用失败**
   - 检查 AI 服务是否正常运行
   - 验证 LLM_API_KEY 是否正确配置

### 7.2 日志查看

- 后端日志：`docker-compose logs -f backend`
- 前端日志：`docker-compose logs -f frontend`
- AI 服务日志：`docker-compose logs -f ai`
- MySQL 日志：`docker-compose logs -f mysql`

## 8. 数据备份与恢复

### 8.1 数据库备份
```bash
docker exec -t <mysql容器ID> mysqldump -u root -proot zcxt > backup.sql
```

### 8.2 数据库恢复
```bash
docker exec -i <mysql容器ID> mysql -u root -proot zcxt < backup.sql
```

### 8.3 MinIO 数据备份
- 定期备份 `minio_data` 卷的数据

## 9. 安全最佳实践

1. **修改默认密码**
   - 修改 MySQL root 密码
   - 修改 MinIO 访问凭证
   - 修改系统 admin 密码

2. **配置 HTTPS**
   - 使用 Nginx 或其他反向代理配置 HTTPS
   - 确保所有服务之间的通信都使用安全连接

3. **限制访问权限**
   - 配置防火墙规则，只允许必要的端口访问
   - 限制数据库和 Redis 的访问范围

4. **定期更新**
   - 定期更新 Docker 镜像
   - 定期更新系统依赖包

5. **监控与告警**
   - 配置系统监控
   - 设置关键指标告警

## 10. 扩展与维护

### 10.1 水平扩展
- 可通过修改 `docker-compose.yml` 文件中的服务配置实现水平扩展
- 例如，增加后端服务实例数量以提高处理能力

### 10.2 版本更新
1. **拉取最新代码**
   ```bash
   git pull
   ```

2. **重新构建和启动服务**
   ```bash
   docker-compose down
   docker-compose up --build -d
   ```

### 10.3 性能优化
- 根据实际负载调整服务资源限制
- 优化数据库查询性能
- 配置合适的缓存策略

## 11. 联系与支持

- 项目文档：查看项目根目录下的相关文档
- 代码仓库：<仓库地址>
- 技术支持：<联系方式>

---

**部署完成后，请务必修改默认密码和密钥，确保系统安全！**