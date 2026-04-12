# OOPay 支付系统 - 完善开发计划

> 版本：v1.0  
> 状态：待确认  
> 最后更新：2025-04-09

---

## 1. 计划确认说明

**本计划为完善版开发规划，包含详细的阶段划分、任务分解、检查点和验收标准。**

**确认流程**：
1. 阅读本计划全部内容
2. 检查各阶段任务是否符合预期
3. 确认后可开始执行
4. 执行过程中如需调整，通过 Issue 记录变更

---

## 2. 项目范围确认

### 2.1 包含模块 ✅

| 模块 | 功能说明 | 优先级 |
|------|----------|--------|
| 商户中心 | 入驻、审核、密钥、配置 | P0 |
| 支付核心 | 统一下单、查询、关闭、回调 | P0 |
| 账户资金 | 余额、冻结、流水、手续费 | P0 |
| 退款服务 | 申请、审核、原路退回 | P1 |
| 对账中心 | 实时/日终对账、差异处理 | P1 |
| 风控管理 | 规则引擎、拦截、TG通知 | P1 |
| 通道管理 | 配置、路由、Mock/真实通道 | P0 |
| 管理后台 | 运营管理系统（前后端） | P0 |
| 商户端 | 商户自助系统（前后端） | P0 |

### 2.2 剔除模块 ❌

| 模块 | 剔除原因 |
|------|----------|
| 核销端 (writeoff) | 非核心支付链路 |
| 代理商系统 (agent) | 前后端分离增加复杂度 |

### 2.3 技术栈确认

| 层级 | 技术选择 | 状态 |
|------|----------|------|
| 后端 | Java 21 + Spring Boot 3.x | 确认 |
| 数据库 | MySQL 8.0 + Redis 7.x | 确认 |
| 消息队列 | RabbitMQ | 确认 |
| 前端 | Vue 3 + Element Plus | 确认 |
| 网关 | Spring Cloud Gateway | 确认 |

---

## 3. 开发阶段规划

### 阶段总览

```
Phase 0: 项目初始化
├── 任务：环境搭建、项目脚手架、基础配置
├── 检查点：项目可编译启动
└── 验收：开发环境就绪

Phase 1: 基础设施
├── 任务：数据库设计、通用工具、网关基础
├── 检查点：数据库脚本可执行
└── 验收：基础设施完整

Phase 2: 核心服务
├── 任务：商户服务、支付核心、通道管理
├── 检查点：Mock支付可跑通
└── 验收：核心链路可用

Phase 3: 资金服务
├── 任务：账户系统、退款服务、通知服务
├── 检查点：支付入账+退款完整
└── 验收：资金闭环完成

Phase 4: 运营服务
├── 任务：对账中心、风控管理
├── 检查点：对账准确、风控可拦截
└── 验收：运营能力完整

Phase 5: 管理后台
├── 任务：Admin API + 前后端界面
├── 检查点：界面可操作所有功能
└── 验收：管理系统可用

Phase 6: 通道实现
├── 任务：微信/支付宝/USDT通道对接
├── 检查点：真实支付可完成
└── 验收：生产就绪

Phase 7: 生产准备
├── 任务：压测、安全审计、文档完善
├── 检查点：性能/安全达标
└── 验收：可上线生产
```

---

## 4. 详细任务分解

### Phase 0: 项目初始化

**目标**：开发环境就绪，团队可开始编码

**任务清单**：
```markdown
□ 1.1 创建代码仓库结构
    - GitHub 仓库初始化
    - 分支保护规则（main/develop）
    - GitFlow 工作流配置

□ 1.2 开发环境搭建指南
    - MySQL 8.0 安装配置
    - Redis 7.x 安装配置
    - RabbitMQ 安装配置
    - Java 21 环境配置
    - Node.js 环境配置

□ 1.3 项目脚手架
    - Spring Boot 多模块项目结构
    - Maven 依赖管理（bom）
    - 通用配置（application.yml）
    - 日志配置（logback-spring.xml）

□ 1.4 持续集成配置
    - GitHub Actions 工作流
    - 代码编译检查
    - 单元测试执行
    - SonarQube 代码扫描

□ 1.5 开发规范文档
    - 代码风格（Checkstyle）
    - 提交规范（Commit Message）
    - 分支命名规范
    - PR 审查规范
```

**检查点**：
```bash
□ git clone 后能直接编译通过
□ mvn clean package 无错误
□ 数据库连接配置正确
□ 健康检查接口可访问
```

**验收标准**：
```markdown
□ 新项目成员可在 30 分钟内搭建好开发环境
□ 代码提交后 CI 自动构建成功
□ 开发规范文档已阅读并确认
```

---

### Phase 1: 基础设施

**目标**：所有服务可复用的基础能力

**任务清单**：
```markdown
□ 2.1 数据库设计
    - 12张核心表的 DDL 脚本
    - 索引设计
    - 初始化数据（系统参数、管理员账号）
    - Flyway 迁移脚本

□ 2.2 共享实体定义
    - BaseEntity（createTime/updateTime/version/deleted）
    - 商户相关实体（Merchant/MerchantConfig）
    - 订单相关实体（Order/OrderLog）
    - 账户相关实体（Account/AccountFlow）
    - 枚举定义（OrderStatus/PayType/RefundStatus）

□ 2.3 共享 DTO 定义
    - 统一响应封装（Result/PageResult）
    - 商户相关 DTO
    - 订单相关 DTO
    - 账户相关 DTO

□ 2.4 工具类实现
    - HmacSHA256 签名工具
    - AES-256 加解密工具
    - 雪花算法 ID 生成器
    - 金额转换工具（分↔元）
    - 日期时间工具
    - JSON 工具（Jackson 配置）
    - Bean 拷贝工具

□ 2.5 网关基础
    - 全局异常处理
    - 统一响应包装
    - 日志拦截器（请求/响应日志）
    - CORS 跨域配置
    - 健康检查端点
    - 限流基础配置

□ 2.6 配置管理
    - 应用配置（多环境）
    - 数据库连接池配置
    - Redis 连接配置
    - RabbitMQ 连接配置
    - 线程池配置
```

**检查点**：
```bash
□ 数据库脚本在空库上可完整执行
□ 所有工具类单元测试通过
□ 网关可正常启动并代理请求
□ 配置项有默认值且可覆盖
```

**验收标准**：
```markdown
□ 数据库 ER 图与设计文档一致
□ 工具类单元测试覆盖率 ≥ 80%
□ 网关健康检查接口返回 200
□ 配置项可在不修改代码的情况下调整
□ timestamp 防重放攻击检查机制实现
□ 密码使用 bcrypt(cost=12) 加密存储
```

---

### Phase 2: 核心服务

**目标**：支付核心链路可完整跑通

**任务清单**：
```markdown
□ 3.1 商户服务实现
    实体：
    - Merchant 实体
    - MerchantConfig 实体
    
    接口：
    - POST /api/admin/merchant/apply（商户入驻）
    - POST /api/admin/merchant/audit（商户审核）
    - GET  /api/admin/merchant/{id}（商户详情）
    - POST /api/admin/merchant/{id}/resetKey（重置密钥）
    - PUT  /api/admin/merchant/{id}/ipWhitelist（IP白名单）
    - PUT  /api/admin/merchant/{id}/limit（限额配置）
    
    内部服务：
    - 商户验证服务
    - 签名验证服务
    - API 密钥生成

□ 3.2 支付核心实现
    实体：
    - Order 实体
    - OrderLog 实体
    
    接口：
    - POST /api/pay/create（统一下单）
    - POST /api/pay/query（订单查询）
    - POST /api/pay/close（订单关闭）
    
    核心逻辑：
    - 幂等校验（24小时窗口）
    - 订单状态机（PENDING→PROCESSING→SUCCESS/FAILED/CLOSED）
    - 超时关闭定时任务
    - 签名验证中间件
    
    回调处理：
    - 回调接收接口
    - 订单状态更新
    - 入账触发

□ 3.3 通道管理实现
    实体：
    - PayChannel 实体
    - ChannelConfig 实体
    
    接口：
    - CRUD /api/admin/channel（通道管理）
    - POST /api/admin/channel/{id}/toggle（启用/禁用）
    - POST /api/admin/merchant/{id}/channels（商户通道配置）
    
    抽象层：
    - PaymentChannel 接口定义
    - ChannelResponse 统一响应
    
    Mock实现：
    - MockChannel 实现
    - 模拟支付成功/失败
    - 模拟回调
```

**检查点**：
```bash
□ 商户入驻→审核→获取密钥 完整链路
□ 统一下单接口幂等性测试通过
□ Mock通道支付可完成整笔交易
□ 订单状态流转正确
```

**验收标准**：
```markdown
□ 商户入驻到获取密钥可在管理后台完成
□ 同一商户订单号24小时内只能创建一笔订单
□ Mock支付通道可模拟成功和失败场景
□ 订单过期后自动关闭
□ 回调通知可发送到配置的URL
```

---

### Phase 3: 资金服务

**目标**：资金流转完整，退款可原路退回

**任务清单**：
```markdown
□ 4.1 账户系统实现
    实体：
    - Account 实体
    - AccountFlow 实体
    - FreezeRecord 实体
    
    接口：
    - POST /api/account/balance（余额查询）
    - POST /api/account/flow（流水查询）
    
    内部服务：
    - 开户服务（支付时自动开户）
    - 入账服务（支付成功时调用）
    - 出账服务（退款时调用）
    - 冻结服务
    - 解冻服务
    
    核心逻辑：
    - 余额计算（可用余额 = 总余额 - 冻结金额）
    - 流水记录（每笔资金变动必须记录）
    - 事务控制（账户余额 + 流水记录）

□ 4.2 退款服务实现
    实体：
    - RefundOrder 实体
    
    接口：
    - POST /api/refund/create（退款申请）
    - POST /api/refund/query（退款查询）
    - POST /api/admin/refund/{id}/audit（退款审核）
    
    核心逻辑：
    - 退款金额校验（≤ 订单金额）
    - 原订单状态校验（仅 SUCCESS 可退款）
    - 账户扣款
    - 原路退回（调用通道退款接口）
    - 退款回调通知

□ 4.3 通知服务实现
    实体：
    - NotifyRecord 实体
    - NotifyLog 实体
    
    接口：
    - POST /api/admin/order/{id}/notify（手动重发通知）
    
    核心逻辑：
    - 异步通知发送（RabbitMQ）
    - 重试机制（1/2/4/8/16分钟，最多5次）
    - 通知状态跟踪
    - 通知日志记录
```

**检查点**：
```bash
□ 支付成功后商户余额增加
□ 手续费正确扣除
□ 退款申请→审核→入账 完整链路
□ 退款后原订单状态变为 REFUNDED
□ 通知失败自动重试
```

**验收标准**：
```markdown
□ 支付成功后商户账户余额正确增加
□ 手续费按配置费率扣除
□ 退款金额不能超过原订单金额
□ 退款后原订单状态正确变更
□ 回调通知失败时自动重试，成功时停止
```

---

### Phase 4: 运营服务

**目标**：运营能力完整，风险可控

**任务清单**：
```markdown
□ 5.1 对账中心实现
    实体：
    - ReconcileTask 实体
    - ReconcileDiff 实体
    
    接口：
    - GET  /api/admin/reconcile/realtime（实时对账）
    - POST /api/admin/reconcile/daily（日终对账）
    - GET  /api/admin/reconcile/diff（差异列表）
    - POST /api/admin/reconcile/diff/{id}/handle（差异处理）
    - GET  /api/admin/reconcile/report（对账报表）
    
    核心逻辑：
    - 平台订单 vs 通道订单 比对
    - 差异检测（金额不一致、状态不一致、单边账）
    - 差异处理（补单、撤销、调账）
    - 定时任务（每日凌晨自动对账）

□ 5.2 风控管理实现
    实体：
    - RiskRule 实体
    - RiskIntercept 实体
    
    接口：
    - CRUD /api/admin/risk/rule（风控规则）
    - GET  /api/admin/risk/intercept（拦截记录）
    - PUT  /api/admin/risk/tgNotify（TG通知配置）
    
    核心逻辑：
    - 规则引擎（金额限制、频次限制、IP限制、时间限制）
    - 支付前风控检查
    - 风险评分计算
    - TG Bot 大额通知
    - 拦截记录存储
```

**检查点**：
```bash
□ 对账可检测出平台有通道无的差异
□ 对账可检测出金额不一致的差异
□ 风控规则可拦截超限订单
□ TG Bot 可收到大额订单通知
```

**验收标准**：
```markdown
□ 对账准确率 ≥ 99.9%
□ 差异处理有完整操作记录
□ 风控规则可配置化
□ 大额订单（可配置阈值）触发 TG 通知
```

---

### Phase 5: 管理后台

**目标**：运营人员可通过界面管理全部功能

**任务清单**：
```markdown
□ 6.1 管理后台 API 完善
    认证：
    - POST /api/admin/auth/login（登录）
    - GET  /api/admin/auth/captcha（验证码）
    - POST /api/admin/auth/logout（退出）
    
    Dashboard：
    - GET /api/admin/dashboard/statistics（统计数据）
    - GET /api/admin/dashboard/trend（趋势图）
    - GET /api/admin/dashboard/channelStats（通道状态）
    
    订单管理：
    - GET  /api/admin/order/list（订单列表）
    - GET  /api/admin/order/{id}（订单详情）
    - POST /api/admin/order/{id}/close（关闭订单）
    - POST /api/admin/order/{id}/notify（重发通知）
    - GET  /api/admin/order/statistics（订单统计）
    
    系统管理：
    - CRUD /api/admin/config（系统参数）
    - GET  /api/admin/log/operation（操作日志）

□ 6.2 管理后台前端
    页面：
    - 登录页
    - Dashboard 首页（统计卡片、趋势图、通道状态）
    - 商户管理：列表/详情/审核
    - 订单管理：列表/详情/关闭
    - 退款管理：列表/审核
    - 通道管理：列表/配置/商户绑定
    - 对账管理：看板/差异处理/报表
    - 风控管理：规则配置/拦截记录
    - 系统设置：参数配置/操作日志

□ 6.3 商户端前端
    页面：
    - 登录/注册页
    - Dashboard（余额、今日统计）
    - 订单管理：列表/详情
    - 账户中心：余额/流水/限额
    - API 文档：密钥查看/接口文档
```

**检查点**：
```bash
□ 管理后台可完成商户审核
□ 管理后台可查看订单详情并关闭
□ 管理后台可配置通道费率
□ 商户端可查看到自己的订单和余额
```

**验收标准**：
```markdown
□ 管理后台覆盖所有管理功能
□ 界面操作响应时间 < 500ms
□ 商户端可完成自助查询
□ 所有页面有权限控制
```

---

### Phase 6: 通道实现

**目标**：真实支付通道可完成支付

**任务清单**：
```markdown
□ 7.1 微信支付通道
    - Native 支付（扫码支付）
    - JSAPI 支付（公众号）
    - 支付回调处理
    - 退款接口
    - 订单查询接口

□ 7.2 支付宝通道
    - 电脑网站支付
    - 手机网站支付
    - 支付回调处理
    - 退款接口
    - 订单查询接口

□ 7.3 USDT 通道
    - TRC20 转账监听
    - 地址生成
    - 支付确认（链上确认数）
    - 汇率获取（OKX API）
```

**检查点**：
```bash
□ 微信扫码支付可完成真实支付
□ 支付宝支付可完成真实支付
□ USDT 转账可被正确识别
```

**验收标准**：
```markdown
□ 微信支付成功率 ≥ 95%
□ 支付宝支付成功率 ≥ 95%
□ USDT 支付确认时间 < 3分钟
□ 所有通道支持退款
```

---

### Phase 7: 生产准备

**目标**：系统可安全、稳定上线生产

**任务清单**：
```markdown
□ 8.1 性能优化
    - 数据库查询优化（慢查询治理）
    - 缓存优化（Redis 热点数据）
    - 接口响应优化（目标 < 200ms）
    - 并发处理能力优化

□ 8.2 压力测试
    - 目标：1000 TPS
    - 场景：支付创建、支付回调、订单查询
    - 持续时间：30分钟
    - 通过率：≥ 99.9%

□ 8.3 安全审计
    - 代码安全扫描
    - 渗透测试
    - 敏感信息检查（密钥、密码）
    - 日志脱敏检查

□ 8.4 监控告警
    - 应用监控（Prometheus + Grafana）
    - 日志收集（ELK）
    - 告警规则（订单成功率、响应时间、错误率）
    - 告警渠道（邮件/短信/钉钉）

□ 8.5 文档完善
    - 部署文档
    - 运维手册
    - 故障处理手册
    - 接口变更记录

□ 8.6 生产部署
    - 生产环境准备
    - 数据迁移脚本
    - 回滚方案
    - 灰度发布计划
```

**检查点**：
```bash
□ 压测报告通过
□ 安全扫描无高危漏洞
□ 监控告警可正常触发
□ 部署文档验证通过
```

**验收标准**：
```markdown
□ 压测达到 1000 TPS，成功率 ≥ 99.9%
□ 安全扫描无高危/严重漏洞
□ 核心接口平均响应时间 < 200ms
□ 监控覆盖率 100%
□ 部署文档可在 2 小时内完成生产部署
```

---

## 5. 检查点与里程碑

### 5.1 阶段检查点

| 阶段 | 检查点 | 验收人 | 状态 |
|------|--------|--------|------|
| Phase 0 | 开发环境就绪 | Tech Lead | ⬜ |
| Phase 1 | 基础设施完整 | Architect | ⬜ |
| Phase 2 | 核心链路可用 | Product Manager | ⬜ |
| Phase 3 | 资金闭环完成 | Tech Lead | ⬜ |
| Phase 4 | 运营能力完整 | Product Manager | ⬜ |
| Phase 5 | 管理系统可用 | Product Manager | ⬜ |
| Phase 6 | 真实通道可用 | Tech Lead | ⬜ |
| Phase 7 | 生产就绪 | Architect | ⬜ |

### 5.2 里程碑确认

每个阶段完成后，在此 Issue 中回复确认：

```markdown
## Phase X 完成确认

### 完成内容
- 任务 1
- 任务 2
- ...

### 检查结果
□ 所有任务已完成
□ 单元测试覆盖率 ≥ 80%
□ 集成测试通过
□ 代码审查通过

### 下一步
进入 Phase X+1

确认人：@username
日期：YYYY-MM-DD
```

---

## 6. 风险与应对

| 风险 | 概率 | 影响 | 应对策略 | 责任人 |
|------|------|------|----------|--------|
| 数据库设计变更 | 中 | 高 | Schema 版本控制，变更需评审 | Architect |
| 通道对接延迟 | 中 | 高 | Mock 通道先行，不阻塞开发 | Tech Lead |
| 性能不达标 | 中 | 高 | Phase 6 开始压测，预留优化时间 | Tech Lead |
| 需求变更 | 高 | 中 | MVP 冻结，变更走审批流程 | Product Manager |
| 人员变动 | 低 | 高 | 关键岗位文档完善，知识共享 | Tech Lead |

---

## 7. 确认与执行

### 7.1 计划确认

请确认以下事项：

- [ ] 项目范围（包含/剔除模块）确认无误
- [ ] 技术栈选择确认无误
- [ ] 阶段划分符合预期
- [ ] 各阶段任务分解详细完整
- [ ] 验收标准可衡量可执行

### 7.2 执行启动

确认后，按以下步骤执行：

1. **创建 GitHub Project**：按 Phase 创建看板
2. **创建 Issues**：每个 Phase 创建一个跟踪 Issue
3. **分配任务**：将任务分配给具体开发者
4. **启动 Phase 0**：开始项目初始化

### 7.3 变更管理

如需调整计划：
1. 在此 Issue 中回复变更建议
2. 说明变更原因和影响
3. 相关人确认后更新计划
4. 更新 GitHub Project

---

## 8. 附录

### 8.1 参考文档

- [PRD-v1.0.md](./oopay-docs/docs/prd/PRD-v1.0.md)
- [architecture.md](./oopay-docs/docs/architecture/architecture.md)
- [db-schema.md](./oopay-docs/docs/db/db-schema.md)
- [api-reference.md](./oopay-docs/docs/api/api-reference.md)
- [admin-api-reference.md](./oopay-docs/docs/api/admin-api-reference.md)
- [ui-design.md](./oopay-docs/docs/ui/ui-design.md)

### 8.2 联系信息

| 角色 | 责任人 | GitHub |
|------|--------|--------|
| 产品负责人 | - | @kimiaic |
| 技术负责人 | - | @kimiaic |
| 架构师 | - | @kimiaic |

---

**计划状态**：待确认  
**确认后请回复**："计划确认，开始执行 Phase 0"

---

*计划版本：v1.0*  
*最后更新：2025-04-09*
