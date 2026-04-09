# OOPay 文档索引

> 快速导航到所需文档

## 按角色导航

### 产品经理 / 业务人员
| 文档 | 说明 |
|------|------|
| [PRD-v1.0.md](./prd/PRD-v1.0.md) | 产品需求主文档，业务流程、规则定义 |
| [feature-list.md](./prd/feature-list.md) | 58个功能点完整清单 |
| [ui-design.md](./ui/ui-design.md) | 界面原型、设计规范 |

### 架构师 / 技术负责人
| 文档 | 说明 |
|------|------|
| [architecture.md](./architecture/architecture.md) | 系统架构、服务依赖、时序图、部署方案 |
| [security-design.md](./security/security-design.md) | 安全架构、P0/P1/P2 实施清单 |
| [db-schema.md](./db/db-schema.md) | 数据库设计、DDL、规范 |

### 后端开发工程师
| 文档 | 说明 |
|------|------|
| [api-reference.md](./api/api-reference.md) | API 接口规范、签名算法、错误码 |
| [db-schema.md](./db/db-schema.md) | 数据库表结构、字段规范 |
| [feature-fields.md](./prd/feature-fields.md) | 功能字段详细设计 |
| [security-design.md](./security/security-design.md) | 安全机制实现指南 |

### 测试工程师
| 文档 | 说明 |
|------|------|
| [PRD-v1.0.md](./prd/PRD-v1.0.md) | 业务规则、验收标准 |
| [api-reference.md](./api/api-reference.md) | 接口测试参考 |
| [security-design.md](./security/security-design.md) | 安全测试 Checklist |

### 运维工程师
| 文档 | 说明 |
|------|------|
| [operations.md](./ops/operations.md) | 日常运维、监控告警、故障处理 |
| [architecture.md](./architecture/architecture.md) | 部署架构、服务依赖 |
| [security-design.md](./security/security-design.md) | 安全配置指南 |

## 按开发阶段导航

### 需求阶段
```
PRD-v1.0.md → feature-list.md → feature-fields.md
```

### 设计阶段
```
architecture.md → db-schema.md → security-design.md → ui-design.md
```

### 开发阶段
```
api-reference.md → db-schema.md → security-design.md
```

### 测试阶段
```
PRD-v1.0.md (验收标准) → api-reference.md → security-design.md (测试清单)
```

### 部署阶段
```
architecture.md (部署架构) → operations.md → db-schema.md (初始化脚本)
```

### 项目管理
```
DEV_PLAN.md → 开发计划、里程碑、风险控制
```

## 项目管理文档

| 文档 | 说明 |
|------|------|
| [DEV_PLAN.md](./DEV_PLAN.md) | 开发计划（16周分阶段交付） |
| [api-alignment-report.md](./api/api-alignment-report.md) | 前后端对齐检查报告 |

## 快速参考

### 金额处理规范
| 币种 | 类型 | 说明 |
|------|------|------|
| CNY | `BIGINT` | 单位：分 |
| USDT | `DECIMAL(20,8)` | 8位小数 |

### 安全等级
| 等级 | 要求 | 状态 |
|------|------|------|
| P0 | 上线前必须完成 | 🔴 强制 |
| P1 | 高优先级 | 🟡 重要 |
| P2 | 增强功能 | 🟢 可选 |

### 关键配置
| 配置项 | 值 | 说明 |
|--------|-----|------|
| 订单有效期 | 30 分钟 | 支付超时 |
| 幂等窗口 | 24 小时 | 同商户订单号 |
| 签名超时 | 5 分钟 | 时间戳窗口 |
| 通知重试 | 5 次 | 间隔 1/2/4/8/16 分钟 |

---

*最后更新：2025-04-09*
