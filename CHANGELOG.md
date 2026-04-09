# 更新日志 (Changelog)

> 所有对 OOPay 支付系统的重要变更都将记录在此文件

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [SemVer](https://semver.org/lang/zh-CN/)。

---

## [Unreleased]

### 规划中

- [ ] 微信支付通道接入
- [ ] 支付宝支付通道接入
- [ ] 基础订单管理功能
- [ ] 商户入驻与管理

---

## [1.0.0] - 2025-04-09

### 设计完成 🎉

OOPay 支付系统 v1.0 设计阶段完成，包含完整的文档体系。

### 文档

#### 新增

- **产品需求文档** (`docs/prd/`)
  - `PRD-v1.0.md` - 产品需求主文档，包含业务规则、流程设计
  - `feature-list.md` - 58个功能点完整清单，按模块分类
  - `feature-fields.md` - 功能字段设计，命名规范

- **架构设计** (`docs/architecture/`)
  - `architecture.md` - 系统架构、服务依赖、时序图、部署方案

- **数据库设计** (`docs/db/`)
  - `db-schema.md` - 12张核心表 DDL，含金额处理规范

- **安全设计** (`docs/security/`)
  - `security-design.md` - 安全架构、P0/P1/P2 实施清单

- **API 文档** (`docs/api/`)
  - `api-reference.md` - 完整接口规范、签名算法

- **运营手册** (`docs/ops/`)
  - `operations.md` - 日常运维、监控告警、故障处理

- **UI 设计** (`docs/ui/`)
  - `ui-design.md` - 界面原型、设计规范

### 核心设计决策

#### 范围界定
- ✅ 包含：商户中心、支付核心、账户资金、对账中心、风控管理
- ❌ 剔除：核销端 (writeoff)、代理商系统 (agent)

#### 技术规范
- CNY 金额使用 `BIGINT`（分），避免浮点精度问题
- USDT 金额使用 `DECIMAL(20,8)`
- 密码使用 `bcrypt`（cost=12）
- 敏感数据使用 `AES-256-GCM` 加密

#### 安全机制
- HMAC-SHA256 请求签名
- 时间戳 + Nonce 防重放
- 多级限流（网关/商户/接口/IP）
- Saga 事务补偿
- 多级对账（实时/定时/日终）

### 变更

- 优化文档结构，统一使用 `oopay-docs/` 目录
- 添加开发规范 `CONTRIBUTING.md`
- 添加更新日志 `CHANGELOG.md`

### 参考

- 参考项目：[xxpay4pro](https://github.com/xxpaypro/xxpay4pro)

---

## 版本历史概览

| 版本 | 日期 | 状态 | 主要变更 |
|------|------|------|----------|
| 1.0.0 | 2025-04-09 | 设计中 | 初始版本，完整设计文档 |

---

## 版本对比

### v1.0 (当前) vs xxpay4pro

| 特性 | OOPay v1.0 | xxpay4pro |
|------|------------|-----------|
| 核销端 | ❌ 剔除 | ✅ 包含 |
| 代理商系统 | ❌ 剔除 | ✅ 包含 |
| USDT 支付 | ✅ 支持 | ✅ 支持 |
| 智能路由 | ✅ 增强 | ✅ 基础 |
| 安全等级 | ✅ 金融级 | ✅ 企业级 |
| 文档完整度 | ✅ 详细 | ⚠️ 一般 |

---

## 未来规划

### v1.1 (计划 2025-Q2)

- [ ] 微信支付通道接入
- [ ] 支付宝支付通道接入
- [ ] 基础商户入驻流程
- [ ] 订单管理功能

### v1.2 (计划 2025-Q3)

- [ ] USDT 支付通道
- [ ] 退款功能
- [ ] TG Bot 通知
- [ ] 基础风控规则

### v2.0 (计划 2025-Q4)

- [ ] 多通道智能路由
- [ ] 高级对账功能
- [ ] 运营报表系统
- [ ] 管理后台完整功能

---

## 贡献者

感谢以下人员对 OOPay 的贡献：

- 架构设计、需求分析、文档编写

---

[Unreleased]: https://github.com/kimiaic/payment_ooxx/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/kimiaic/payment_ooxx/releases/tag/v1.0.0
