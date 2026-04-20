# 部署指南

## 环境配置

### 1. 开发环境 (H2数据库)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. 生产环境 (MySQL数据库)
```bash
cd docker
docker-compose --env-file .env.production up -d
```

## 数据库配置

### 生产环境变量
| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| DB_HOST | mysql | MySQL服务器地址 |
| DB_PORT | 3306 | MySQL端口 |
| DB_NAME | zcxt | 数据库名称 |
| DB_USER | root | 数据库用户名 |
| DB_PASSWORD | root | 数据库密码 |
| FLYWAY_REPAIR | false | 是否执行Flyway修复 |

## Flyway迁移问题排查

### 方案1: 自动修复
设置环境变量 `FLYWAY_REPAIR=true` 后重启服务：
```bash
FLYWAY_REPAIR=true docker-compose up -d backend
```

### 方案2: 手动修复
1. 连接数据库：
```bash
mysql -h localhost -P 3306 -u root -p zcxt
```

2. 执行清理SQL：
```sql
DELETE FROM flyway_schema_history WHERE success = 0;
UPDATE flyway_schema_history SET success = 1 WHERE success = 0;
```

3. 或使用脚本：
```bash
chmod +x flyway-repair.sh
./flyway-repair.sh
```

### 方案3: 完全重置数据库
```bash
docker-compose down -v
docker-compose up -d
```

## CI/CD 流程

### GitHub Actions 工作流

1. **CI Pipeline** (`.github/workflows/ci.yml`)
   - 触发条件: push 或 pull request 到 main, develop 分支
   - 执行: 后端构建+测试, 前端构建

2. **CD Pipeline** (`.github/workflows/cd.yml`)
   - 触发条件: push 到 main 分支或手动触发
   - 支持: staging 和 production 环境部署

### 部署前检查清单
- [ ] 数据库已创建并可访问
- [ ] 环境变量已正确配置
- [ ] Flyway迁移脚本已就绪
- [ ] SSL证书已配置（生产环境）
- [ ] 备份策略已制定