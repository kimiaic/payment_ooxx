# OOPay 功能字段设计

## 1. 字段命名规范

| 规则 | 示例 |
|------|------|
| 小写下划线 | `merchant_no`, `create_time` |
| 布尔用 is/has | `is_deleted`, `has_notify` |
| 时间后缀 | `_time` 表示时间点，`_at` 也可接受 |
| 状态用 status | `pay_status`, `audit_status` |
| 类型用 type | `pay_type`, `trade_type` |
| 金额统一用分 | `amount`, `fee_amount` |

---

## 2. 核心功能字段

### 2.1 商户模块

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| merchant_no | varchar(32) | 是 | 商户编号：M + 14位数字 |
| merchant_name | varchar(128) | 是 | 商户名称 |
| merchant_type | tinyint | 是 | 类型：1-企业，2-个人 |
| contact_name | varchar(256) | 否 | 联系人（加密） |
| contact_phone | varchar(256) | 否 | 手机号（加密） |
| api_key | varchar(256) | 是 | API 标识（加密） |
| api_secret | varchar(512) | 是 | API 密钥（加密） |
| status | tinyint | 是 | 状态：0-禁用，1-正常，2-冻结 |
| audit_status | tinyint | 是 | 审核：0-待审，1-通过，2-拒绝 |
| risk_level | tinyint | 是 | 风控等级：1-低，2-中，3-高 |
| daily_limit | bigint | 否 | 日限额（分） |
| single_limit | bigint | 否 | 单笔限额（分） |
| notify_url | varchar(512) | 否 | 默认回调地址 |

### 2.2 订单模块

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| order_no | varchar(32) | 是 | 订单号：O + 日期 + 随机 |
| merchant_no | varchar(32) | 是 | 商户编号 |
| merchant_order_no | varchar(64) | 是 | 商户订单号 |
| pay_type | varchar(32) | 是 | 支付方式 |
| channel_id | bigint | 否 | 通道 ID |
| channel_order_no | varchar(64) | 否 | 通道订单号 |
| amount | bigint | 是 | 金额（分） |
| currency | varchar(8) | 是 | 币种 |
| usdt_amount | decimal(20,8) | 否 | USDT 金额 |
| usdt_rate | decimal(20,8) | 否 | 下单汇率 |
| merchant_fee | bigint | 是 | 商户手续费（分） |
| channel_fee | bigint | 是 | 通道手续费（分） |
| status | varchar(16) | 是 | 订单状态 |
| pay_status | tinyint | 是 | 支付状态 |
| expired_time | datetime | 否 | 过期时间 |
| pay_time | datetime | 否 | 支付时间 |
| notify_time | datetime | 否 | 通知时间 |
| client_ip | varchar(64) | 否 | 客户端 IP |
| subject | varchar(256) | 否 | 商品标题 |
| body | varchar(512) | 否 | 商品描述 |
| notify_url | varchar(512) | 否 | 回调地址 |
| return_url | varchar(512) | 否 | 跳转地址 |
| attach | varchar(512) | 否 | 附加数据 |

**订单状态枚举：**
| 状态值 | 说明 |
|--------|------|
| PENDING | 待支付 |
| PROCESSING | 处理中 |
| SUCCESS | 支付成功 |
| FAILED | 支付失败 |
| CLOSED | 已关闭 |
| REFUNDING | 退款中 |
| REFUNDED | 已退款 |

### 2.3 账户模块

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| account_no | varchar(32) | 是 | 账户号：A + 14位数字 |
| merchant_no | varchar(32) | 是 | 商户编号 |
| account_type | tinyint | 是 | 类型：1-余额，2-冻结 |
| currency | varchar(8) | 是 | 币种 |
| balance | bigint | 是 | 可用余额（分） |
| frozen_balance | bigint | 是 | 冻结余额（分） |
| single_in_limit | bigint | 否 | 单笔入账限额 |
| single_out_limit | bigint | 否 | 单笔出账限额 |
| daily_in_limit | bigint | 否 | 日入账限额 |
| daily_out_limit | bigint | 否 | 日出账限额 |
| status | tinyint | 是 | 状态：0-冻结，1-正常 |

### 2.4 账户流水模块

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| flow_no | varchar(32) | 是 | 流水号：F + 日期 + 随机 |
| account_no | varchar(32) | 是 | 账户号 |
| merchant_no | varchar(32) | 是 | 商户编号 |
| trade_type | tinyint | 是 | 交易类型 |
| trade_direction | tinyint | 是 | 方向：1-收入，2-支出 |
| amount | bigint | 是 | 金额（分） |
| balance | bigint | 是 | 变动后余额（分） |
| order_no | varchar(32) | 否 | 关联订单号 |
| trade_no | varchar(64) | 否 | 关联交易号 |
| status | tinyint | 是 | 状态：0-失败，1-成功 |

**交易类型枚举：**
| 类型值 | 说明 |
|--------|------|
| 1 | 充值 |
| 2 | 提现 |
| 3 | 支付 |
| 4 | 退款 |
| 5 | 手续费 |

### 2.5 通道模块

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| channel_code | varchar(32) | 是 | 通道编码 |
| channel_name | varchar(64) | 是 | 通道名称 |
| channel_type | tinyint | 是 | 类型：1-微信，2-支付宝，4-USDT |
| pay_type | varchar(32) | 是 | 支付方式 |
| config_json | json | 是 | 配置参数 |
| fee_rate | decimal(5,4) | 是 | 费率 |
| min_amount | bigint | 否 | 最小金额（分） |
| max_amount | bigint | 否 | 最大金额（分） |
| status | tinyint | 是 | 状态：0-禁用，1-启用 |
| health_status | tinyint | 是 | 健康：0-故障，1-正常，2-降级 |
| weight | int | 是 | 权重 |
| priority | int | 是 | 优先级 |

### 2.6 回调通知模块

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| notify_no | varchar(32) | 是 | 通知编号：N + 14位数字 |
| order_no | varchar(32) | 是 | 订单号 |
| merchant_no | varchar(32) | 是 | 商户编号 |
| notify_type | tinyint | 是 | 类型：1-支付，2-退款 |
| notify_url | varchar(512) | 是 | 通知地址 |
| notify_data | text | 是 | 通知内容 |
| notify_count | int | 是 | 通知次数 |
| last_notify_time | datetime | 否 | 最后通知时间 |
| response_code | int | 否 | HTTP 状态码 |
| status | tinyint | 是 | 状态：0-待通知，1-成功，2-失败 |

### 2.7 对账模块

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reconcile_date | date | 是 | 对账日期 |
| channel_id | bigint | 是 | 通道 ID |
| total_orders | int | 是 | 系统订单数 |
| total_amount | bigint | 是 | 系统金额（分） |
| channel_orders | int | 是 | 通道订单数 |
| channel_amount | bigint | 是 | 通道金额（分） |
| diff_orders | int | 是 | 差异订单数 |
| diff_amount | bigint | 是 | 差异金额（分） |
| status | tinyint | 是 | 状态：0-未对账，1-对账中，2-完成，3-差异 |

---

## 3. 通用字段

所有表必须包含：

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | bigint | auto | 主键，自增 |
| create_time | datetime | current | 创建时间 |
| update_time | datetime | current | 更新时间 |
| create_by | bigint | null | 创建人 ID |
| update_by | bigint | null | 更新人 ID |
| deleted | tinyint | 0 | 软删除：0-正常，1-删除 |
| version | int | 0 | 乐观锁版本号 |

---

*文档版本：v1.0*  
*最后更新：2025-04-09*
