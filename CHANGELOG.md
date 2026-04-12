# 更新日志 (Changelog)

> 所有对 OOPay 支付系统的重要变更都将记录在此文件

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [SemVer](https://semver.org/lang/zh-CN/)。

---

## [Unreleased]

### 规划中

- [ ] Phase 1.3 Mapper XML 接口定义
- [ ] Phase 1.4 Service 接口定义
- [ ] Phase 1.5 网关过滤器完善
- [ ] Phase 1.6 配置中心对接

### 已完成的整理工作

- [x] 删除根目录废弃 `docs/` 目录，统一使用 `oopay-docs/`
- [x] 修复 Issue #1 中开发计划链接路径错误
- [x] Phase 1 验收标准补充安全要求（timestamp 防重放 + bcrypt）

---

## [1.0.1] - 2026-04-12

### Phase 1 基础设施（进行中）

#### 代码

- **数据库脚本** - `oopay-common/src/main/resources/db/migration/V1__init_schema.sql`
  - 12张核心表 DDL
  - 初始化数据：默认 admin 账号、系统基础参数

- **共享实体类** (`oopay-common/src/main/java/com/oopay/common/entity/`)
  - `BaseEntity.java` - 抽象基类
  - `Merchant.java` - 商户实体
  - `MerchantChannel.java` - 商户通道配置
  - `PaymentOrder.java` - 支付订单
  - `Account.java` - 账户资金
  - `AccountTransaction.java` - 账户流水
  - `NotifyRecord.java` - 通知记录
  - `SystemConfig.java` - 系统配置

- **共享 DTO** (`oopay-common/src/main/java/com/oopay/common/dto/`)
  - `PageRequest.java` - 分页请求
  - `PageResult.java` - 分页结果
  - `Result.java` - 统一响应

- **枚举类** (`oopay-common/src/main/java/com/oopay/common/enums/`)
  - `AccountFlowType.java` - 账户流水类型
  - `NotifyStatus.java` - 通知状态
  - `OrderStatus.java` - 订单状态
  - `PayType.java` - 支付类型
  - `RefundStatus.java` - 退款状态

- **工具类** (`oopay-common/src/main/java/com/oopay/common/util/`)
  - `AesUtil.java` - AES-256 加密
  - `AmountUtil.java` - 金额转换（分↔元）
  - `HmacSha256Util.java` - HMAC 签名
  - `SignUtil.java` - 请求签名
  - `SnowflakeIdUtil.java` - 分布式 ID 生成

- **网关基础** (`oopay-gateway/src/main/java/com/oopay/gateway/`)
  - `RateLimitConfig.java` - 限流配置
  - `RequestLogFilter.java` - 请求日志过滤器
  - `BusinessException.java` - 业务异常
  - `GlobalExceptionHandler.java` - 全局异常处理

#### 文档

- **协作规则** - `AGENT_RULES.md`
  - AI 协作规范 v1.0
  - Git 提交规范
  - 任务执行规范

- **待处理事项** - `TODO.md`
  - 当前任务跟踪
  - 优先级管理

### 安全要求强化

- Phase 1 验收标准新增：
  - □ timestamp 防重放攻击检查机制实现
  - □ 密码使用 bcrypt(cost=12) 加密存储

---

## [1.0.0] - 2025-04-09

### 设计完成 🎉

OOPay 支付系统 v1.0 设计阶段完成，包含完整的文档体系。

### 文档

#### 新增

- **产品需求文档** (`oopay-docs/docs/prd/`)
  - `PRD-v1.0.md` - 产品需求主文档，包含业务规则、流程设计
  - `feature-list.md` - 58个功能点完整清单，按模块分类
  - `feature-fields.md` - 功能字段设计，命名规范

- **架构设计** (`oopay-docs/docs/architecture/`)
  - `architecture.md` - 系统架构、服务依赖、时序图、部署方案

- **数据库设计** (`oopay-docs/docs/db/`)
  - `db-schema.md` - 12张核心表 DDL，含金额处理规范

- **安全设计** (`oopay-docs/docs/security/`)
  - `security-design.md` - 安全架构、P0/P1/P2 实施清单

- **API 文档** (`oopay-docs/docs/api/`)
  - `api-reference.md` - 完整接口规范、签名算法

- **运营手册** (`oopay-docs/docs/ops/`)
  - `operations.md` - 日常运维、监控告警、故障处理

- **UI 设计** (`oopay-docs/docs/ui/`)
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
