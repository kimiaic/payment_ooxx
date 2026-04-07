# OOPay 系统功能与字段设计

## 一、运营后台 (oopay-manage)

### 1. 仪表盘

| 功能 | 字段/数据项 | 来源表 |
|-----|------------|--------|
| 今日订单数 | COUNT(pay_order) WHERE created_at >= TODAY | pay_order |
| 今日交易额 | SUM(amount) WHERE created_at >= TODAY | pay_order |
| 今日成功率 | 成功订单数/总订单数 | pay_order |
| 新增商户数 | COUNT(merchant) WHERE created_at >= TODAY | merchant |
| 厂商额度列表 | provider_name, available_quota, total_quota | provider |
| 实时交易动态 | pay_order_id, merchant_name, amount, channel_name, status | pay_order + provider_channel + merchant |

---

### 2. 厂商管理

#### 2.1 厂商列表

**查询字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| provider_code | VARCHAR(32) | 厂商编码 |
| provider_name | VARCHAR(64) | 厂商名称 |
| available_quota | DECIMAL(20,8) | 可用额度(USDT) |
| total_quota | DECIMAL(20,8) | 总额度 |
| status | TINYINT | 0-禁用 1-正常 2-暂停 |
| today_success_count | INT | 今日成功订单数 |
| today_total_count | INT | 今日总订单数 |
| created_at | TIMESTAMP | 创建时间 |

**搜索条件：**
- provider_name 模糊搜索
- status 下拉筛选
- created_at 时间范围

#### 2.2 新增/编辑厂商

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| provider_code | VARCHAR(32) | 是 | 唯一编码，如haoyun |
| provider_name | VARCHAR(64) | 是 | 显示名称 |
| usdt_address | VARCHAR(128) | 是 | TRC20充值地址 |
| contact_name | VARCHAR(32) | 否 | 联系人 |
| contact_phone | VARCHAR(20) | 否 | 联系电话 |
| total_quota | DECIMAL(20,8) | 是 | 初始额度 |
| alert_threshold | DECIMAL(20,8) | 否 | 预警阈值，默认1000 |
| exchange_rate_spread | DECIMAL(5,4) | 否 | 汇差，默认0.1 |
| tg_group_id | VARCHAR(64) | 否 | TG群组ID |
| status | TINYINT | 是 | 1-正常 |
| remark | VARCHAR(256) | 否 | 备注 |

#### 2.3 额度管理

**额度变动记录字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| change_type | TINYINT | 1-充值 2-消耗 3-冻结 4-释放 5-人工调整 |
| amount | DECIMAL(20,8) | 变动金额（正为增加，负为减少） |
| before_quota | DECIMAL(20,8) | 变动前额度 |
| after_quota | DECIMAL(20,8) | 变动后额度 |
| order_id | BIGINT | 关联订单ID（消耗/冻结时） |
| operator | VARCHAR(32) | 操作人 |
| remark | VARCHAR(256) | 备注（如链上交易哈希） |
| created_at | TIMESTAMP | 操作时间 |

**充值操作字段：**
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| provider_id | BIGINT | 是 | 厂商ID |
| amount | DECIMAL(20,8) | 是 | 充值金额(USDT) |
| tx_hash | VARCHAR(128) | 否 | 链上交易哈希 |
| remark | VARCHAR(256) | 否 | 备注 |

---

### 3. 通道管理

#### 3.1 通道列表

| 字段 | 类型 | 说明 |
|-----|------|------|
| channel_code | VARCHAR(32) | 通道编码 |
| channel_name | VARCHAR(64) | 通道名称 |
| provider_name | VARCHAR(64) | 所属厂商 |
| pay_type | VARCHAR(16) | ALIPAY_H5/WECHAT_H5等 |
| fee_rate | DECIMAL(5,4) | 费率，如0.0200 |
| min_amount | BIGINT | 单笔最小金额(分) |
| max_amount | BIGINT | 单笔最大金额(分) |
| daily_limit | BIGINT | 日限额(分) |
| status | TINYINT | 0-维护 1-在线 |
| today_count | INT | 今日订单数 |
| success_rate | DECIMAL(5,2) | 近7天成功率 |

#### 3.2 新增/编辑通道

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| provider_id | BIGINT | 是 | 所属厂商 |
| channel_code | VARCHAR(32) | 是 | 唯一编码 |
| channel_name | VARCHAR(64) | 是 | 显示名称 |
| pay_type | VARCHAR(16) | 是 | 支付类型 |
| fee_rate | DECIMAL(5,4) | 是 | 费率 |
| min_amount | BIGINT | 是 | 最小金额(分) |
| max_amount | BIGINT | 是 | 最大金额(分) |
| daily_limit | BIGINT | 是 | 日限额(分) |
| config_json | TEXT | 否 | 上游接口配置(JSON) |
| status | TINYINT | 是 | 1-在线 |

**config_json 示例：**
```json
{
  "api_url": "https://api.haoyun.com/pay",
  "merchant_id": "M123456",
  "sign_type": "MD5",
  "timeout": 30
}
```

---

### 4. 产品管理

#### 4.1 产品列表

| 字段 | 类型 | 说明 |
|-----|------|------|
| product_code | VARCHAR(32) | 产品编码，如oopayalih5 |
| product_name | VARCHAR(64) | 产品名称 |
| pay_type | VARCHAR(16) | 支付类型 |
| default_fee_rate | DECIMAL(5,4) | 默认费率 |
| status | TINYINT | 0-下线 1-上线 |
| channel_count | INT | 绑定通道数 |
| merchant_count | INT | 授权商户数 |

#### 4.2 新增/编辑产品

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| product_code | VARCHAR(32) | 是 | 唯一编码 |
| product_name | VARCHAR(64) | 是 | 显示名称 |
| pay_type | VARCHAR(16) | 是 | 支付类型 |
| default_fee_rate | DECIMAL(5,4) | 是 | 默认费率 |
| description | VARCHAR(256) | 否 | 产品描述 |
| status | TINYINT | 是 | 1-上线 |

#### 4.3 路由配置

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| product_id | BIGINT | 是 | 产品ID |
| channel_id | BIGINT | 是 | 通道ID |
| weight | INT | 是 | 权重，如60 |
| priority | INT | 是 | 优先级，1开始 |
| is_backup | TINYINT | 否 | 是否备用通道 0-否 1-是 |
| status | TINYINT | 是 | 1-启用 |

**高级规则配置：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| rule_type | VARCHAR(16) | AMOUNT/TIME |
| condition | VARCHAR(64) | 条件表达式，如">5000" |
| action_type | VARCHAR(16) | PREFER_CHANNEL/EXCLUDE_CHANNEL |
| action_value | VARCHAR(32) | 目标通道编码 |

---

### 5. 商户管理

#### 5.1 商户列表

| 字段 | 类型 | 说明 |
|-----|------|------|
| merchant_code | VARCHAR(32) | 商户编码 |
| merchant_name | VARCHAR(64) | 商户名称 |
| usdt_balance | DECIMAL(20,8) | USDT余额 |
| status | TINYINT | 0-禁用 1-正常 2-冻结 |
| invite_code | VARCHAR(32) | 注册邀请码 |
| created_at | TIMESTAMP | 注册时间 |
| last_login_at | TIMESTAMP | 最后登录时间 |

#### 5.2 新增商户/邀请码生成

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| invite_code | VARCHAR(32) | 是 | 邀请码 |
| expire_days | INT | 否 | 有效期天数，默认7 |
| max_use_count | INT | 否 | 最大使用次数，默认1 |
| remark | VARCHAR(256) | 否 | 备注 |

#### 5.3 商户详情

**基础信息字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| merchant_name | VARCHAR(64) | 商户名称 |
| contact_name | VARCHAR(32) | 联系人 |
| contact_phone | VARCHAR(20) | 联系电话 |
| contact_email | VARCHAR(64) | 邮箱 |
| settlement_address | VARCHAR(128) | USDT结算地址 |
| login_pwd | VARCHAR(64) | 登录密码(加密) |
| pay_pwd | VARCHAR(64) | 支付密码(加密) |
| status | TINYINT | 1-正常 |

**授权产品字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| product_code | VARCHAR(32) | 产品编码 |
| product_name | VARCHAR(64) | 产品名称 |
| default_fee_rate | DECIMAL(5,4) | 平台默认费率 |
| merchant_fee_rate | DECIMAL(5,4) | 商户实际费率 |
| status | TINYINT | 0-未授权 1-已授权 |

**IP白名单字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| ip_address | VARCHAR(32) | IP地址 |
| remark | VARCHAR(64) | 备注 |
| created_at | TIMESTAMP | 添加时间 |

---

### 6. 订单管理

#### 6.1 订单查询

**查询条件：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| pay_order_id | VARCHAR(32) | 平台订单号 |
| mch_order_no | VARCHAR(64) | 商户订单号 |
| merchant_id | BIGINT | 商户ID |
| channel_id | BIGINT | 通道ID |
| status | TINYINT | 订单状态 |
| amount_min | BIGINT | 最小金额(分) |
| amount_max | BIGINT | 最大金额(分) |
| created_start | TIMESTAMP | 开始时间 |
| created_end | TIMESTAMP | 结束时间 |

**列表字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| pay_order_id | VARCHAR(32) | 平台订单号 |
| merchant_name | VARCHAR(64) | 商户名称 |
| amount | BIGINT | 金额(分) |
| product_name | VARCHAR(64) | 支付产品 |
| channel_name | VARCHAR(64) | 上游通道 |
| status | TINYINT | 0-生成 1-支付中 2-成功 3-完成 4-关闭 |
| pay_succ_time | TIMESTAMP | 支付成功时间 |
| created_at | TIMESTAMP | 创建时间 |

#### 6.2 订单详情

| 字段 | 类型 | 说明 |
|-----|------|------|
| pay_order_id | VARCHAR(32) | 平台订单号 |
| mch_order_no | VARCHAR(64) | 商户订单号 |
| merchant_name | VARCHAR(64) | 商户名称 |
| app_id | VARCHAR(32) | 应用ID |
| product_name | VARCHAR(64) | 支付产品 |
| channel_name | VARCHAR(64) | 实际通道 |
| amount | BIGINT | 金额(分) |
| currency | VARCHAR(8) | CNY |
| status | TINYINT | 订单状态 |
| client_ip | VARCHAR(32) | 客户端IP |
| subject | VARCHAR(128) | 商品标题 |
| notify_url | VARCHAR(256) | 回调地址 |
| pay_succ_time | TIMESTAMP | 支付成功时间 |
| platform_status | VARCHAR(16) | 平台侧状态 |
| upstream_status | VARCHAR(16) | 上游侧状态（查询获取） |
| notify_status | VARCHAR(16) | 回调商户状态 |
| platform_profit | DECIMAL(20,8) | 平台利润(USDT) |
| created_at | TIMESTAMP | 创建时间 |

#### 6.3 异常处理操作

**手动补单字段：**
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| order_id | BIGINT | 是 | 订单ID |
| reason | VARCHAR(256) | 是 | 补单原因 |
| operator | VARCHAR(32) | 是 | 操作人 |

**强制关单字段：**
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| order_id | BIGINT | 是 | 订单ID |
| reason | VARCHAR(256) | 是 | 关单原因 |
| operator | VARCHAR(32) | 是 | 操作人 |

---

### 7. 资金管理

#### 7.1 资金流水

**查询条件：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| account_type | TINYINT | 1-商户 2-厂商 |
| owner_id | BIGINT | 商户ID或厂商ID |
| change_type | TINYINT | 变动类型 |
| created_start | TIMESTAMP | 开始时间 |
| created_end | TIMESTAMP | 结束时间 |

**列表字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| created_at | TIMESTAMP | 变动时间 |
| owner_name | VARCHAR(64) | 账户名称 |
| account_type | TINYINT | 1-商户 2-厂商 |
| change_type | TINYINT | 1-订单收入 2-手续费 3-提现 4-人工调整 5-充值 |
| amount | DECIMAL(20,8) | 变动金额 |
| before_balance | DECIMAL(20,8) | 变动前余额 |
| after_balance | DECIMAL(20,8) | 变动后余额 |
| order_id | BIGINT | 关联订单ID |
| operator | VARCHAR(32) | 操作人 |
| remark | VARCHAR(256) | 备注 |

#### 7.2 手动调账

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| account_type | TINYINT | 是 | 1-商户 2-厂商 |
| owner_id | BIGINT | 是 | 商户ID或厂商ID |
| amount | DECIMAL(20,8) | 是 | 调整金额（正为增加，负为减少） |
| reason | VARCHAR(256) | 是 | 调账原因 |
| operator | VARCHAR(32) | 是 | 操作人 |

#### 7.3 商户结算审批

| 字段 | 类型 | 说明 |
|-----|------|------|
| settlement_no | VARCHAR(32) | 结算单号 |
| merchant_name | VARCHAR(64) | 商户名称 |
| amount | DECIMAL(20,8) | 结算金额(USDT) |
| settlement_address | VARCHAR(128) | 结算地址 |
| status | TINYINT | 0-待审核 1-处理中 2-完成 3-失败 |
| apply_time | TIMESTAMP | 申请时间 |
| tx_hash | VARCHAR(128) | 链上交易哈希 |

---

### 8. 风控管理

#### 8.1 黑名单

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| blacklist_type | TINYINT | 是 | 1-IP 2-商户 3-用户ID 4-设备指纹 |
| blacklist_value | VARCHAR(128) | 是 | 黑名单值 |
| reason | VARCHAR(256) | 否 | 拉黑原因 |
| expire_time | TIMESTAMP | 否 | 过期时间，null表示永久 |
| operator | VARCHAR(32) | 是 | 操作人 |

#### 8.2 风控规则

| 字段 | 类型 | 说明 |
|-----|------|------|
| rule_name | VARCHAR(64) | 规则名称 |
| rule_type | VARCHAR(16) | AMOUNT_LIMIT/FREQUENCY_LIMIT/TIME_LIMIT |
| condition | VARCHAR(256) | 规则条件(JSON) |
| action | TINYINT | 1-拦截 2-告警 3-人工审核 |
| status | TINYINT | 0-禁用 1-启用 |

---

### 9. 系统配置

#### 9.1 基础配置

| 配置Key | 默认值 | 说明 |
|--------|--------|------|
| order.timeout | 30 | 订单超时时间(分钟) |
| order.query.interval | 5 | 兜底查询间隔(分钟) |
| order.query.max_time | 120 | 最大查询时长(分钟) |
| notify.retry.times | 3 | 回调重试次数 |
| notify.retry.delays | 1,5,15 | 重试延迟(分钟) |
| quota.alert.threshold | 1000 | 额度预警阈值(CNY) |
| exchange.rate.interval | 5 | 汇率更新间隔(分钟) |

#### 9.2 TG群组绑定

| 字段 | 类型 | 说明 |
|-----|------|------|
| group_type | TINYINT | 1-厂商合作群 2-商户合作群 3-运营内部群 4-管理指令群 |
| group_id | VARCHAR(64) | Telegram群组ID |
| group_name | VARCHAR(64) | 群组名称 |
| bind_type | TINYINT | 1-厂商 2-商户 |
| bind_id | BIGINT | 厂商ID或商户ID |
| status | TINYINT | 0-禁用 1-启用 |

---

## 二、商户系统 (oopay-merchant)

### 1. 首页

| 数据项 | 来源字段 | 说明 |
|-------|---------|------|
| 可用余额 | account.usdt_balance | 当前可用 |
| 冻结金额 | account.usdt_frozen | 冻结中 |
| 今日收入 | SUM(account_log.amount) WHERE change_type=1 AND created_at>=TODAY | 今日订单收入 |
| 今日订单数 | COUNT(pay_order) | 今日总订单 |
| 今日成功数 | COUNT(pay_order WHERE status=2) | 今日成功订单 |
| 今日成功率 | 成功数/总订单数 | 百分比 |

### 2. 基础信息

| 字段 | 类型 | 说明 |
|-----|------|------|
| merchant_name | VARCHAR(64) | 商户名称（可修改） |
| contact_name | VARCHAR(32) | 联系人 |
| contact_phone | VARCHAR(20) | 联系电话 |
| contact_email | VARCHAR(64) | 邮箱 |
| settlement_address | VARCHAR(128) | USDT结算地址（TRC20） |

### 3. 安全中心

| 字段 | 类型 | 说明 |
|-----|------|------|
| app_id | VARCHAR(32) | 应用ID |
| api_key | VARCHAR(64) | API Key |
| secret_key | VARCHAR(64) | Secret Key（脱敏显示） |
| ip_address | VARCHAR(32) | 白名单IP |
| login_time | TIMESTAMP | 登录时间 |
| login_ip | VARCHAR(32) | 登录IP |
| login_location | VARCHAR(64) | 登录地点 |

### 4. 订单管理（商户视角）

| 字段 | 类型 | 说明 |
|-----|------|------|
| pay_order_id | VARCHAR(32) | 平台订单号 |
| mch_order_no | VARCHAR(64) | 商户订单号 |
| amount | BIGINT | 金额(分) |
| product_name | VARCHAR(64) | 支付产品 |
| status | TINYINT | 0-生成 1-支付中 2-成功 3-完成 4-关闭 |
| pay_data | VARCHAR(512) | 支付数据（待支付时显示） |
| pay_succ_time | TIMESTAMP | 支付成功时间 |
| created_at | TIMESTAMP | 创建时间 |

### 5. 结算管理

**申请结算字段：**
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| amount | DECIMAL(20,8) | 是 | 结算金额，不能超过可用余额 |
| settlement_address | VARCHAR(128) | 是 | 结算地址（默认带出，可修改） |
| pay_pwd | VARCHAR(64) | 是 | 支付密码验证 |

**结算记录字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| settlement_no | VARCHAR(32) | 结算单号 |
| amount | DECIMAL(20,8) | 结算金额 |
| settlement_address | VARCHAR(128) | 结算地址 |
| status | TINYINT | 0-待审核 1-处理中 2-完成 3-失败 |
| tx_hash | VARCHAR(128) | 链上交易哈希 |
| apply_time | TIMESTAMP | 申请时间 |
| complete_time | TIMESTAMP | 完成时间 |

---

## 三、支付网关 (oopay-pay)

### 1. 统一下单接口

**请求字段：**
| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| mchId | VARCHAR(32) | 是 | 商户号 |
| appId | VARCHAR(32) | 是 | 应用ID |
| productId | VARCHAR(32) | 是 | 支付产品编码 |
| mchOrderNo | VARCHAR(64) | 是 | 商户订单号（唯一） |
| currency | VARCHAR(8) | 是 | CNY |
| amount | LONG | 是 | 金额（分） |
| clientIp | VARCHAR(32) | 是 | 客户端IP |
| device | VARCHAR(64) | 否 | 设备信息 |
| subject | VARCHAR(128) | 是 | 商品标题 |
| body | VARCHAR(256) | 否 | 商品描述 |
| notifyUrl | VARCHAR(256) | 是 | 异步通知地址 |
| returnUrl | VARCHAR(256) | 否 | 同步跳转地址 |
| param1 | VARCHAR(128) | 否 | 扩展参数1 |
| param2 | VARCHAR(128) | 否 | 扩展参数2 |
| sign | VARCHAR(64) | 是 | 签名 |

**响应字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| retCode | VARCHAR(16) | SUCCESS/FAIL |
| retMsg | VARCHAR(128) | 返回信息 |
| sign | VARCHAR(64) | 签名 |
| payOrderId | VARCHAR(32) | 平台订单号 |
| status | TINYINT | 订单状态 |
| payData | VARCHAR(512) | 支付数据（JSON） |

### 2. 回调通知

**通知字段：**
| 字段 | 类型 | 说明 |
|-----|------|------|
| payOrderId | VARCHAR(32) | 平台订单号 |
| mchOrderNo | VARCHAR(64) | 商户订单号 |
| mchId | VARCHAR(32) | 商户号 |
| appId | VARCHAR(32) | 应用ID |
| productId | VARCHAR(32) | 支付产品编码 |
| currency | VARCHAR(8) | CNY |
| amount | LONG | 金额（分） |
| status | TINYINT | 2-支付成功 |
| paySuccTime | LONG | 支付成功时间戳（秒） |
| backType | TINYINT | 1-前端跳转 2-后台通知 |
| sign | VARCHAR(64) | 签名 |

---

## 四、定时任务 (oopay-task)

### 1. 任务配置

| 任务名称 | 执行频率 | 配置Key |
|---------|---------|---------|
| 兜底查询 | 每5分钟 | task.query.interval=5 |
| 超时关单 | 每小时 | task.close.interval=60 |
| 账单生成 | 每日00:30 | task.bill.time=00:30 |
| 汇率更新 | 每5分钟 | task.rate.interval=5 |
| 额度巡检 | 每30分钟 | task.quota.interval=30 |
| 通道探测 | 每5分钟 | task.probe.interval=5 |
| 数据归档 | 每日02:00 | task.archive.time=02:00 |

### 2. TG Bot 配置

| 字段 | 类型 | 说明 |
|-----|------|------|
| bot_token | VARCHAR(128) | Bot Token |
| webhook_url | VARCHAR(256) | Webhook地址 |
| command_prefix | VARCHAR(8) | 指令前缀，默认"/" |
| admin_users | VARCHAR(512) | 管理员用户ID列表（逗号分隔） |
| confirm_threshold | DECIMAL(20,8) | 大额确认阈值，默认10000 |

---

## 字段汇总统计

| 来源 | 表/接口数 | 字段数 |
|-----|----------|--------|
| 运营后台 | 15+ | 150+ |
| 商户系统 | 10 | 80+ |
| 支付网关 | 3 | 40+ |
| 定时任务 | 2 | 20+ |
| **合计** | **30+** | **290+** |
