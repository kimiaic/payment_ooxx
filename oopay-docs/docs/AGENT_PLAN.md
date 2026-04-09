# OOPay 多 Agent 并行开发规划

> 基于模块依赖关系设计的并行开发方案
> 不设时间预期，只规划开发顺序和 Agent 分工

---

## 1. 系统模块依赖图

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                    OOPay 支付系统                         │
                    └─────────────────────────────────────────────────────────┘
                                                  │
           ┌──────────────────────────────────────┼──────────────────────────────────────┐
           │                                      │                                      │
           ▼                                      ▼                                      ▼
┌─────────────────────┐              ┌─────────────────────┐              ┌─────────────────────┐
│   基础设施层         │              │     核心业务层       │              │    管理运营层        │
├─────────────────────┤              ├─────────────────────┤              ├─────────────────────┤
│ • 数据库基础表       │◄─────────────│ • 商户服务           │◄─────────────│ • 商户管理           │
│ • 通用工具类         │              │ • 支付核心           │              │ • 订单管理           │
│ • 配置中心           │              │ • 账户资金           │              │ • 通道管理           │
│ • 网关基础           │              │ • 退款服务           │              │ • 对账中心           │
└─────────────────────┘              │ • 通知服务           │              │ • 风控管理           │
                                     └─────────────────────┘              │ • 系统管理           │
                                              │                           └─────────────────────┘
                                              │                                      ▲
                                              │                                      │
                                              └──────────────────────────────────────┘
                                                              │
                                                              ▼
                                              ┌─────────────────────┐
                                              │    第三方通道层      │
                                              ├─────────────────────┤
                                              │ • 微信支付通道       │
                                              │ • 支付宝通道         │
                                              │ • USDT 通道          │
                                              │ • Mock 通道          │
                                              └─────────────────────┘
```

---

## 2. 模块依赖关系矩阵

### 2.1 核心依赖表

| 模块 | 依赖上游 | 被下游依赖 | 依赖强度 |
|------|---------|-----------|---------|
| 数据库基础 | 无 | 所有模块 | 🔴 强 |
| 通用工具 | 无 | 所有模块 | 🔴 强 |
| 商户服务 | 数据库、工具 | 支付、账户、管理 | 🔴 强 |
| 支付核心 | 商户、数据库 | 账户、退款、对账 | 🔴 强 |
| 账户资金 | 支付、数据库 | 退款、对账 | 🟡 中 |
| 退款服务 | 支付、账户 | 对账 | 🟡 中 |
| 通知服务 | 支付、退款 | 无 | 🟢 弱 |
| 商户管理 | 商户服务 | 无 | 🟢 弱 |
| 订单管理 | 支付核心 | 无 | 🟢 弱 |
| 通道管理 | 支付核心 | 无 | 🟢 弱 |
| 对账中心 | 支付、退款、账户 | 无 | 🟢 弱 |
| 风控管理 | 支付核心 | 无 | 🟢 弱 |
| 通道实现 | 支付核心、通道管理 | 无 | 🟢 弱 |

### 2.2 并行开发组划分

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              并行开发分组                                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  Group 0: 基础设施 (必须先完成)                                                       │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │ Agent-Infra: 数据库表 + 通用工具 + 配置中心 + 网关基础                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                         │                                           │
│                                         ▼                                           │
│  Group 1: 核心服务 (可并行，依赖 Group 0)                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                     │
│  │ Agent-Merchant  │  │  Agent-Payment  │  │ Agent-Channel   │                     │
│  │    商户服务      │  │    支付核心      │  │   通道管理       │                     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘                     │
│           │                    │                    │                              │
│           └────────────────────┼────────────────────┘                              │
│                                ▼                                                   │
│  Group 2: 资金服务 (依赖 Group 1)                                                    │
│  ┌─────────────────┐  ┌─────────────────┐                                         │
│  │  Agent-Account  │  │  Agent-Refund   │                                         │
│  │    账户资金      │  │    退款服务      │                                         │
│  └─────────────────┘  └─────────────────┘                                         │
│           │                    │                                                   │
│           └────────────────────┼────────────────────┐                              │
│                                ▼                    ▼                              │
│  Group 3: 运营服务 (依赖 Group 1/2)                                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                     │
│  │ Agent-Reconcile │  │   Agent-Risk    │  │ Agent-Notify    │                     │
│  │    对账中心      │  │    风控管理      │  │   通知服务       │                     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘                     │
│                                │                                                   │
│                                ▼                                                   │
│  Group 4: 管理后台 (依赖 Group 1/2/3，前后端可并行)                                   │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                     │
│  │  Agent-Admin    │  │  Agent-AdminUI  │  │ Agent-MerchantUI│                     │
│  │  管理后台 API   │  │  管理后台前端   │  │   商户端前端    │                     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘                     │
│                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Agent 任务分配

### 3.1 Agent-Infra (基础设施)

**任务范围**：
```yaml
数据库:
  - 12张基础表的 DDL
  - 通用字段设计（create_time/update_time/version/deleted）
  - 索引设计
  - 初始化数据脚本

通用工具:
  - 统一响应封装（Result/PageResult）
  - 签名工具（HMAC-SHA256）
  - 加密工具（AES-256）
  - ID 生成器（雪花算法）
  - 金额转换工具（分↔元）
  - 日期时间工具
  - JSON 工具

配置中心:
  - 应用配置
  - 数据库配置
  - Redis 配置
  - RabbitMQ 配置

网关基础:
  - 全局异常处理
  - 日志拦截器
  - CORS 配置
  - 健康检查接口
```

**产出物**：
- `oopay-common/` 公共模块
- `oopay-gateway/` 网关基础
- `sql/init/` 数据库脚本

**阻塞点**：必须先完成，其他 Agent 才能开始

---

### 3.2 Agent-Merchant (商户服务)

**任务范围**：
```yaml
实体:
  - Merchant 实体
  - MerchantConfig 实体

接口:
  - 商户注册/入驻
  - 商户审核
  - 商户信息查询
  - API 密钥生成/重置
  - IP 白名单配置
  - 限额配置

数据:
  - 商户表操作
  - 商户配置表操作
```

**产出物**：
- `oopay-merchant/` 商户服务模块

**依赖**：Agent-Infra 完成后开始

---

### 3.3 Agent-Payment (支付核心)

**任务范围**：
```yaml
实体:
  - Order 实体
  - OrderLog 实体

接口:
  - 统一下单（幂等校验）
  - 订单查询
  - 订单关闭
  - 支付回调处理
  - 订单状态机

核心逻辑:
  - 订单创建流程
  - 订单状态流转
  - 超时关闭任务
  - 回调通知触发
```

**产出物**：
- `oopay-payment/` 支付核心模块

**依赖**：Agent-Infra 完成后开始，与 Agent-Merchant 并行

---

### 3.4 Agent-Channel (通道管理)

**任务范围**：
```yaml
实体:
  - PayChannel 实体
  - ChannelConfig 实体

接口:
  - 通道 CRUD
  - 通道启用/禁用
  - 费率配置
  - 商户通道绑定

抽象层:
  - 支付通道接口定义
  - 回调处理接口定义

Mock实现:
  - Mock 支付通道（用于开发测试）
```

**产出物**：
- `oopay-channel/` 通道管理模块

**依赖**：Agent-Infra 完成后开始，与 Agent-Payment 并行

---

### 3.5 Agent-Account (账户资金)

**任务范围**：
```yaml
实体:
  - Account 实体
  - AccountFlow 实体
  - FreezeRecord 实体

接口:
  - 开户
  - 余额查询
  - 资金冻结
  - 资金解冻
  - 流水查询
  - 流水导出

核心逻辑:
  - 支付入账（支付成功时）
  - 手续费扣除
  - 冻结/解冻逻辑
```

**产出物**：
- `oopay-account/` 账户服务模块

**依赖**：Agent-Payment 支付回调逻辑完成后开始

---

### 3.6 Agent-Refund (退款服务)

**任务范围**：
```yaml
实体:
  - RefundOrder 实体

接口:
  - 退款申请
  - 退款审核
  - 退款查询
  - 退款回调处理

核心逻辑:
  - 退款金额校验
  - 原路退回
  - 账户扣款
```

**产出物**：
- `oopay-refund/` 退款服务模块

**依赖**：
- Agent-Payment 订单状态机完成
- Agent-Account 账户扣款逻辑完成

---

### 3.7 Agent-Notify (通知服务)

**任务范围**：
```yaml
实体:
  - NotifyRecord 实体
  - NotifyLog 实体

接口:
  - 发送通知
  - 通知重试
  - 通知日志查询

核心逻辑:
  - 异步通知发送
  - 重试机制（1/2/4/8/16分钟）
  - 通知状态跟踪
```

**产出物**：
- `oopay-notify/` 通知服务模块

**依赖**：Agent-Payment 回调触发点完成后开始（松耦合）

---

### 3.8 Agent-Reconcile (对账中心)

**任务范围**：
```yaml
实体:
  - ReconcileTask 实体
  - ReconcileDiff 实体

接口:
  - 实时对账
  - 日终对账任务
  - 差异查询
  - 差异处理
  - 对账报表

核心逻辑:
  - 平台订单 vs 通道订单 比对
  - 差异检测算法
  - 差异处理流程
```

**产出物**：
- `oopay-reconcile/` 对账服务模块

**依赖**：
- Agent-Payment 订单数据完整
- Agent-Refund 退款数据完整
- Agent-Account 账户流水完整

---

### 3.9 Agent-Risk (风控管理)

**任务范围**：
```yaml
实体:
  - RiskRule 实体
  - RiskIntercept 实体

接口:
  - 风控规则 CRUD
  - 拦截记录查询
  - 风控等级配置
  - TG Bot 配置

核心逻辑:
  - 规则引擎
  - 支付前风控检查
  - 大额确认通知
```

**产出物**：
- `oopay-risk/` 风控服务模块

**依赖**：Agent-Payment 下单接口完成后开始（在下单时拦截）

---

### 3.10 Agent-Admin (管理后台 API)

**任务范围**：
```yaml
接口汇总:
  商户管理:
    - 商户列表/详情/审核/状态管理
  订单管理:
    - 订单列表/详情/关闭/重发通知
  退款管理:
    - 退款列表/详情/审核
  通道管理:
    - 通道 CRUD/状态切换/商户配置
  对账管理:
    - 对账任务/差异处理/报表
  风控管理:
    - 规则管理/拦截记录/TG配置
  系统管理:
    - 参数配置/操作日志/权限管理
  Dashboard:
    - 统计数据/趋势图/通道状态
```

**产出物**：
- `oopay-admin/` 管理后台服务

**依赖**：
- 所有业务服务（Merchant/Payment/Account/Refund/Channel/Reconcile/Risk）完成后开始

---

### 3.11 Agent-AdminUI (管理后台前端)

**任务范围**：
```yaml
页面:
  - 登录页面
  - Dashboard 首页
  - 商户管理：列表/详情/审核
  - 订单管理：列表/详情
  - 退款管理：列表/审核
  - 通道管理：列表/配置
  - 对账管理：看板/差异
  - 风控管理：规则/拦截记录
  - 系统管理：参数/日志/权限

组件:
  - 状态标签组件
  - 金额显示组件
  - 分页组件
  - 图表组件
```

**产出物**：
- `oopay-admin-web/` 管理后台前端

**依赖**：
- Agent-Admin API 接口定义完成即可并行开发
- 使用 Mock 数据先行

---

### 3.12 Agent-MerchantUI (商户端前端)

**任务范围**：
```yaml
页面:
  - 登录/注册页面
  - Dashboard 首页
  - 订单管理：列表/详情
  - 账户中心：余额/流水
  - API 文档页面
  - 密钥管理页面

组件:
  - 支付结果展示
  - 流水列表
```

**产出物**：
- `oopay-merchant-web/` 商户端前端

**依赖**：
- 商户相关 API 定义完成即可并行开发
- 使用 Mock 数据先行

---

## 4. 开发顺序与依赖关系

### 4.1 阶段划分（无时间预期）

```
Phase 0: 基础设施
├─ Agent-Infra
│   └─ 产出：数据库脚本、通用工具、网关基础
│   └─ 阻塞：必须完成后其他 Agent 才能开始
│
Phase 1: 核心服务（可并行）
├─ Agent-Merchant ──┐
├─ Agent-Payment ───┼──► 并行开发
├─ Agent-Channel ───┘
│   └─ 产出：商户服务、支付核心、通道管理
│   └─ 阻塞：Phase 2 依赖 Phase 1
│
Phase 2: 资金服务（可并行）
├─ Agent-Account ───┐
├─ Agent-Refund ────┼──► 并行开发
├─ Agent-Notify ────┘
│   └─ 产出：账户、退款、通知服务
│   └─ 阻塞：Phase 3 依赖 Phase 2
│
Phase 3: 运营服务（可并行）
├─ Agent-Reconcile ─┐
├─ Agent-Risk ──────┼──► 并行开发
└─ Agent-Notify ────┘
    └─ 产出：对账、风控、通知完整版
    └─ 阻塞：Phase 4 依赖 Phase 3
│
Phase 4: 管理后台（前后端可并行）
├─ Agent-Admin ─────┐
├─ Agent-AdminUI ───┼──► 并行开发
└─ Agent-MerchantUI─┘
    └─ 产出：管理后台、商户端
│
Phase 5: 通道实现
└─ Agent-Channel-Impl
    └─ 产出：微信/支付宝/USDT 通道实现
    └─ 说明：可与前面并行，Mock 通道已支持开发
```

### 4.2 详细依赖图

```
┌─────────────┐
│ Agent-Infra │────────────────────────────────────────────────────────────┐
└──────┬──────┘                                                            │
       │                                                                   │
       ├──►┌─────────────────┐                                             │
       │   │ Agent-Merchant  │◄────────────────────────────────────────────┤
       │   └────────┬────────┘                                             │
       │            │                                                       │
       ├──►┌─────────────────┐    ┌─────────────────┐                      │
       │   │ Agent-Payment   │◄───│ Agent-Channel   │                      │
       │   └────────┬────────┘    └─────────────────┘                      │
       │            │                                                       │
       │            ├──►┌─────────────────┐    ┌─────────────────┐         │
       │            │   │ Agent-Account   │    │ Agent-Risk      │         │
       │            │   └────────┬────────┘    └─────────────────┘         │
       │            │            │                                          │
       │            ├──►┌─────────────────┐                                │
       │            │   │ Agent-Refund    │                                │
       │            │   └────────┬────────┘                                │
       │            │            │                                         │
       │            ├──►┌─────────────────┐                                │
       │            │   │ Agent-Notify    │                                │
       │            │   └─────────────────┘                                │
       │            │                                                       │
       │            │     ┌─────────────────┐                               │
       │            └────►│ Agent-Reconcile │                               │
       │                  └─────────────────┘                               │
       │                                                                   │
       │     ┌─────────────────┐    ┌─────────────────┐    ┌──────────────┐│
       └────►│   Agent-Admin   │◄───│  Agent-AdminUI  │    │Agent-Merchant││
             └─────────────────┘    └─────────────────┘    └──────────────┘│
                                                                          │
       ┌──────────────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────┐
│ Agent-Channel-Impl  │
│ (微信/支付宝/USDT)  │
└─────────────────────┘
```

---

## 5. 模块间契约定义

### 5.1 服务间调用契约

```yaml
# Agent-Merchant 提供服务
MerchantService:
  validateMerchant:
    input: merchantNo
    output: MerchantDTO
    
  validateApiKey:
    input: apiKey, sign
    output: boolean
    
  getMerchantConfig:
    input: merchantNo
    output: MerchantConfigDTO

# Agent-Payment 提供服务  
PaymentService:
  createOrder:
    input: CreateOrderRequest
    output: OrderDTO
    
  getOrder:
    input: orderNo
    output: OrderDTO
    
  closeOrder:
    input: orderNo
    output: boolean
    
  onPaySuccess:
    input: orderNo, channelOrderNo, payTime
    output: boolean
    # 触发 Agent-Account 入账

# Agent-Account 提供服务
AccountService:
  createAccount:
    input: merchantNo, currency
    output: accountNo
    
  credit:
    input: accountNo, amount, orderNo
    output: flowNo
    
  debit:
    input: accountNo, amount, orderNo
    output: flowNo
    
  freeze:
    input: accountNo, amount, orderNo
    output: freezeNo
    
  unfreeze:
    input: freezeNo
    output: boolean

# Agent-Channel 提供服务
ChannelService:
  getChannel:
    input: payType, amount
    output: ChannelDTO
    
  callPay:
    input: channelId, orderDTO
    output: PayResponse
    
  handleCallback:
    input: channelId, callbackData
    output: CallbackResult
```

### 5.2 事件驱动契约

```yaml
# 支付成功事件
OrderPaidEvent:
  producer: Agent-Payment
  consumers:
    - Agent-Account: 入账
    - Agent-Notify: 发送回调
  payload:
    orderNo: string
    merchantNo: string
    amount: long
    payTime: datetime

# 退款成功事件  
RefundSuccessEvent:
  producer: Agent-Refund
  consumers:
    - Agent-Account: 扣款
    - Agent-Notify: 发送回调
  payload:
    refundNo: string
    orderNo: string
    amount: long
    refundTime: datetime

# 风控拦截事件
RiskInterceptEvent:
  producer: Agent-Risk
  consumers:
    - Agent-Notify: TG通知
  payload:
    orderNo: string
    merchantNo: string
    ruleId: long
    riskScore: int
```

---

## 6. 并行开发协调机制

### 6.1 接口先行原则

```
1. 先定义接口（API 契约）
2. 再实现 Mock 版本
3. 各 Agent 基于 Mock 并行开发
4. 最后替换为真实实现
```

### 6.2 每日同步机制

```yaml
晨会（15分钟）:
  - 昨日完成
  - 今日计划
  - 阻塞问题

接口变更通知:
  - 契约变更必须在群里通知
  - 变更需经相关 Agent 确认

集成检查:
  - 每日下班前确保代码可编译
  - 每周五进行集成测试
```

### 6.3 代码组织

```
oopay/
├── oopay-common/           # Agent-Infra 产出，所有模块依赖
│   ├── src/main/java/
│   │   ├── entity/         # 共享实体
│   │   ├── dto/            # 共享 DTO
│   │   ├── enums/          # 共享枚举
│   │   ├── utils/          # 工具类
│   │   └── constants/      # 常量
│   └── pom.xml
│
├── oopay-gateway/          # Agent-Infra 产出
├── oopay-merchant/         # Agent-Merchant
├── oopay-payment/          # Agent-Payment
├── oopay-account/          # Agent-Account
├── oopay-refund/           # Agent-Refund
├── oopay-channel/          # Agent-Channel
├── oopay-reconcile/        # Agent-Reconcile
├── oopay-risk/             # Agent-Risk
├── oopay-notify/           # Agent-Notify
├── oopay-admin/            # Agent-Admin
│
├── oopay-admin-web/        # Agent-AdminUI
├── oopay-merchant-web/     # Agent-MerchantUI
│
└── sql/
    └── init/               # Agent-Infra 产出
```

---

## 7. 开发检查清单

### 7.1 每个 Agent 的交付标准

```yaml
代码:
  - 单元测试覆盖率 ≥ 80%
  - 核心逻辑有注释
  - 无 SonarQube 严重问题
  - 代码审查通过

文档:
  - 接口文档更新
  - 数据库变更记录
  - README 说明

集成:
  - 与上游模块集成测试通过
  - 与下游模块契约匹配
  - Mock 数据可运行
```

### 7.2 阶段验收标准

```yaml
Phase 0 (基础设施):
  - 数据库脚本可执行
  - 项目可编译启动
  - 健康检查接口可用

Phase 1 (核心服务):
  - 商户可注册/审核
  - 可创建订单
  - Mock 通道可返回结果

Phase 2 (资金服务):
  - 支付成功可入账
  - 可发起退款
  - 回调可发送

Phase 3 (运营服务):
  - 对账可检测差异
  - 风控可拦截订单
  - 通知可重试

Phase 4 (管理后台):
  - 管理后台可管理商户/订单/通道
  - 商户端可查订单/余额
  - 所有功能可端到端运行

Phase 5 (通道实现):
  - 微信/支付宝/USDT 通道可真实支付
```

---

## 8. 风险与应对

| 风险 | 影响 Agent | 应对策略 |
|------|-----------|---------|
| 接口契约频繁变更 | 所有 | 接口冻结机制，变更需审批 |
| 某个 Agent 延期 | 下游 | 延长 Mock 支持，并行开发 |
| 数据库设计变更 | 所有 | Schema 版本控制，兼容升级 |
| 性能不达标 | Agent-Payment/Account | 提前压测，预留优化时间 |
| 通道对接延迟 | Agent-Channel-Impl | Mock 通道支持全流程开发 |

---

## 9. 总结

### Agent 分工总览

| Agent | 职责 | 并行组 | 产出 |
|-------|------|--------|------|
| Agent-Infra | 基础设施 | Phase 0 | common, gateway, sql |
| Agent-Merchant | 商户服务 | Phase 1 | oopay-merchant |
| Agent-Payment | 支付核心 | Phase 1 | oopay-payment |
| Agent-Channel | 通道管理 | Phase 1 | oopay-channel |
| Agent-Account | 账户资金 | Phase 2 | oopay-account |
| Agent-Refund | 退款服务 | Phase 2 | oopay-refund |
| Agent-Notify | 通知服务 | Phase 2 | oopay-notify |
| Agent-Reconcile | 对账中心 | Phase 3 | oopay-reconcile |
| Agent-Risk | 风控管理 | Phase 3 | oopay-risk |
| Agent-Admin | 管理后台 API | Phase 4 | oopay-admin |
| Agent-AdminUI | 管理后台前端 | Phase 4 | oopay-admin-web |
| Agent-MerchantUI | 商户端前端 | Phase 4 | oopay-merchant-web |
| Agent-Channel-Impl | 通道实现 | Phase 5 | channel plugins |

### 最大并行度

```
Phase 0: 1 个 Agent
Phase 1: 3 个 Agent 并行
Phase 2: 3 个 Agent 并行
Phase 3: 3 个 Agent 并行
Phase 4: 3 个 Agent 并行
Phase 5: 1 个 Agent
```

**理论最大并行数：3 个 Agent 同时开发**

---

*规划版本：v1.0*  
*最后更新：2025-04-09*
