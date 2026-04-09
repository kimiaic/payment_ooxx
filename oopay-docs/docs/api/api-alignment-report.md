# OOPay 前后端对齐检查报告

> 检查日期：2026-04-09  
> 检查范围：UI 设计 ↔ API 接口 ↔ 数据库字段

---

## 总体评估

| 维度 | 状态 | 说明 |
|------|------|------|
| 数据模型 | ✅ 已对齐 | 数据库字段、API 参数、UI 展示一致 |
| 核心支付 | ✅ 已对齐 | 下单/查询/关闭/退款接口完整 |
| 账户资金 | ✅ 已对齐 | 余额/流水接口完整 |
| 管理后台 | ⚠️ 部分缺失 | 运营/风控/对账 API 待补充 |
| 异步通知 | ✅ 已对齐 | 回调规范完整 |

---

## 1. 已对齐部分 ✅

### 1.1 订单模块

| 功能 | UI 页面 | API 接口 | 数据库表 | 状态 |
|------|---------|----------|----------|------|
| 统一下单 | - | `POST /api/pay/create` | oopay_order | ✅ |
| 订单查询 | 订单列表/详情 | `POST /api/pay/query` | oopay_order | ✅ |
| 订单关闭 | 订单操作 | `POST /api/pay/close` | oopay_order | ✅ |

**字段对齐检查**：
- API 请求参数 `merchantOrderNo`, `amount`, `currency`, `payType` ↔ 数据库字段一致
- API 响应 `orderNo`, `status`, `payData` ↔ UI 展示字段一致
- 金额单位：API `long` ↔ 数据库 `BIGINT`（分）✅

### 1.2 退款模块

| 功能 | UI 页面 | API 接口 | 数据库表 | 状态 |
|------|---------|----------|----------|------|
| 申请退款 | 退款按钮 | `POST /api/refund/create` | oopay_refund | ✅ |
| 退款查询 | 退款记录 | `POST /api/refund/query` | oopay_refund | ✅ |

### 1.3 账户模块

| 功能 | UI 页面 | API 接口 | 数据库表 | 状态 |
|------|---------|----------|----------|------|
| 余额查询 | 账户概览 | `POST /api/account/balance` | oopay_account | ✅ |
| 流水查询 | 资金流水 | `POST /api/account/flow` | oopay_account_flow | ✅ |

### 1.4 异步通知

| 场景 | 规范 | 状态 |
|------|------|------|
| 支付通知 | `POST {notifyUrl}` + 签名 | ✅ |
| 退款通知 | `POST {notifyUrl}` + 签名 | ✅ |
| 响应要求 | 返回 `SUCCESS` | ✅ |

---

## 2. 待补充部分 ⚠️

### 2.1 商户中心 API（管理后台）

| 功能 | 功能清单 | UI 页面 | 缺失 API | 优先级 |
|------|----------|---------|----------|--------|
| 商户入驻 | M-01 | 商户入驻页 | `POST /api/admin/merchant/apply` | P0 |
| 商户审核 | M-02 | 审核管理 | `POST /api/admin/merchant/audit` | P0 |
| 商户列表 | M-03 | 商户列表 | `GET /api/admin/merchant/list` | P0 |
| 商户详情 | M-03 | 商户详情 | `GET /api/admin/merchant/{id}` | P0 |
| API 密钥 | M-04 | 密钥管理 | `POST /api/admin/merchant/resetKey` | P0 |
| IP 白名单 | M-05 | 安全配置 | `POST /api/admin/merchant/ipWhitelist` | P1 |
| 通道配置 | M-07 | 通道配置 | `POST /api/admin/merchant/channel` | P0 |
| 费率查看 | M-08 | 费率页 | `GET /api/admin/merchant/fee` | P1 |

### 2.2 运营管理 API

| 功能 | 功能清单 | UI 页面 | 缺失 API | 优先级 |
|------|----------|---------|----------|--------|
| 通道管理 | O-01 | 通道列表 | `CRUD /api/admin/channel` | P0 |
| 通道监控 | O-02 | 监控看板 | `GET /api/admin/channel/health` | P1 |
| 费率配置 | O-03 | 费率设置 | `POST /api/admin/channel/fee` | P0 |
| 订单监控 | O-04 | 异常订单 | `GET /api/admin/order/abnormal` | P1 |
| 运营报表 | O-05 | 报表中心 | `GET /api/admin/report/operation` | P2 |

### 2.3 对账中心 API

| 功能 | 功能清单 | UI 页面 | 缺失 API | 优先级 |
|------|----------|---------|----------|--------|
| 实时对账 | R-01 | 对账看板 | `GET /api/admin/reconcile/realtime` | P1 |
| 日终对账 | R-02 | 对账任务 | `POST /api/admin/reconcile/daily` | P1 |
| 差异查看 | R-03 | 差异列表 | `GET /api/admin/reconcile/diff` | P1 |
| 差异处理 | R-04 | 差异处理 | `POST /api/admin/reconcile/handle` | P1 |
| 对账报表 | R-05 | 报表导出 | `GET /api/admin/reconcile/report` | P2 |

### 2.4 风控管理 API

| 功能 | 功能清单 | UI 页面 | 缺失 API | 优先级 |
|------|----------|---------|----------|--------|
| 规则配置 | K-01 | 规则管理 | `CRUD /api/admin/risk/rule` | P1 |
| 拦截记录 | K-02 | 风控日志 | `GET /api/admin/risk/intercept` | P1 |
| 风控等级 | K-03 | 商户风控 | `POST /api/admin/merchant/riskLevel` | P1 |
| 大额确认 | K-04 | TG 配置 | `POST /api/admin/risk/tgNotify` | P1 |

### 2.5 系统管理 API

| 功能 | 功能清单 | UI 页面 | 缺失 API | 优先级 |
|------|----------|---------|----------|--------|
| 参数配置 | S-01 | 系统设置 | `CRUD /api/admin/config` | P2 |
| 操作日志 | S-02 | 日志查询 | `GET /api/admin/log/operation` | P2 |
| 权限管理 | S-03 | 角色/权限 | `CRUD /api/admin/permission` | P2 |

---

## 3. 字段对齐详情

### 3.1 订单状态枚举 ✅

| 枚举值 | 数据库 | API | UI 展示 | 说明 |
|--------|--------|-----|---------|------|
| PENDING | `status='PENDING'` | `"PENDING"` | 待支付 | ✅ 一致 |
| PROCESSING | `status='PROCESSING'` | `"PROCESSING"` | 处理中 | ✅ 一致 |
| SUCCESS | `status='SUCCESS'` | `"SUCCESS"` | 支付成功 | ✅ 一致 |
| FAILED | `status='FAILED'` | `"FAILED"` | 支付失败 | ✅ 一致 |
| CLOSED | `status='CLOSED'` | `"CLOSED"` | 已关闭 | ✅ 一致 |
| REFUNDING | `status='REFUNDING'` | `"REFUNDING"` | 退款中 | ✅ 一致 |
| REFUNDED | `status='REFUNDED'` | `"REFUNDED"` | 已退款 | ✅ 一致 |

### 3.2 金额处理规范 ✅

| 币种 | 数据库 | API | UI | 状态 |
|------|--------|-----|-----|------|
| CNY | `BIGINT` | `long` | 显示为元 | ✅ |
| USDT | `DECIMAL(20,8)` | `string` | 保留8位 | ✅ |

**转换规则**：
- 存储：CNY 使用分（乘以100）
- 传输：API 使用长整数（分）
- 展示：UI 转换为元（除以100）

---

## 4. 建议补充的 API 列表

### 4.1 管理后台基础接口

```yaml
# 商户管理
GET    /api/admin/merchant/list          # 商户列表（分页）
GET    /api/admin/merchant/{id}          # 商户详情
POST   /api/admin/merchant/audit         # 审核商户
POST   /api/admin/merchant/resetKey      # 重置密钥
PUT    /api/admin/merchant/{id}/status   # 修改状态

# 订单管理
GET    /api/admin/order/list             # 订单列表
GET    /api/admin/order/{id}             # 订单详情
GET    /api/admin/order/statistics       # 订单统计

# 通道管理
GET    /api/admin/channel/list           # 通道列表
POST   /api/admin/channel                # 创建通道
PUT    /api/admin/channel/{id}           # 更新通道
DELETE /api/admin/channel/{id}           # 删除通道
POST   /api/admin/channel/{id}/toggle    # 启用/禁用

# 对账管理
GET    /api/admin/reconcile/daily        # 日终对账
GET    /api/admin/reconcile/diff         # 差异列表
POST   /api/admin/reconcile/{id}/handle  # 处理差异

# 风控管理
GET    /api/admin/risk/rule/list         # 规则列表
POST   /api/admin/risk/rule              # 创建规则
GET    /api/admin/risk/intercept/list    # 拦截记录

# 系统管理
GET    /api/admin/config/list            # 参数列表
PUT    /api/admin/config/{key}           # 更新参数
GET    /api/admin/log/operation          # 操作日志
```

---

## 5. 总结

### 已对齐 ✅
- 核心业务 API（支付、退款、账户）
- 数据模型（数据库 ↔ API ↔ UI）
- 状态枚举定义
- 金额处理规范
- 异步通知机制

### 待补充 ⚠️
- 管理后台 API（商户/订单/通道/对账/风控/系统）
- 部分字段校验规则文档化

### 建议
1. **优先补充 P0 级管理 API**：商户审核、通道配置
2. **统一响应格式**：管理接口遵循现有 `code/message/data` 格式
3. **分页规范**：列表接口统一使用 `page/pageSize/total/list`

---

*报告生成时间：2026-04-09*  
*基于文档版本：v1.0*
