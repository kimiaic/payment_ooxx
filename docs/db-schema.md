# OOPay 数据库设计草案

## 表清单

### 1. 上游管理
| 表名 | 说明 |
|-----|------|
| `provider` | 上游厂商信息（好运支付、好来支付等） |
| `provider_channel` | 上游通道（厂商下的支付方式） |
| `provider_quota_log` | 厂商额度变动记录 |

### 2. 产品管理
| 表名 | 说明 |
|-----|------|
| `payment_product` | 支付产品（oopayalih5等） |
| `product_channel_mapping` | 产品与通道的映射关系（含权重、优先级） |

### 3. 商户管理
| 表名 | 说明 |
|-----|------|
| `merchant` | 商户基本信息 |
| `merchant_app` | 商户应用（一个商户可有多个应用） |
| `merchant_product` | 商户授权的产品及独立费率 |
| `ip_whitelist` | 商户IP白名单 |

### 4. 订单核心
| 表名 | 说明 |
|-----|------|
| `pay_order` | 支付订单主表 |
| `pay_order_log` | 订单状态变更日志 |
| `notify_log` | 回调商户记录 |

### 5. 资金管理
| 表名 | 说明 |
|-----|------|
| `account` | 账户余额（商户+厂商） |
| `account_log` | 资金流水明细 |
| `settlement` | 结算/提现申请记录 |

### 6. 风控与配置
| 表名 | 说明 |
|-----|------|
| `blacklist` | 黑名单（IP、商户、用户） |
| `exchange_rate` | OKX汇率记录 |
| `sys_config` | 系统配置（超时时间、阈值等） |
| `operation_log` | 后台操作审计日志 |

### 7. TG Bot
| 表名 | 说明 |
|-----|------|
| `tg_group` | 绑定的Telegram群组 |
| `tg_message_log` | 消息发送记录 |
| `tg_command_log` | Bot指令执行记录 |

---

## 核心表字段设计

### provider（上游厂商）
```sql
- id: BIGINT PK
- provider_code: VARCHAR(32) UNIQUE -- 厂商编码 haoyun
- provider_name: VARCHAR(64) -- 厂商名称 好运支付
- usdt_address: VARCHAR(128) -- USDT充值地址
- total_quota: DECIMAL(20,8) -- 总额度
- available_quota: DECIMAL(20,8) -- 可用额度
- frozen_quota: DECIMAL(20,8) -- 冻结额度
- exchange_rate_spread: DECIMAL(5,4) -- 汇差 默认0.1
- status: TINYINT -- 0-禁用 1-正常 2-暂停
- alert_threshold: DECIMAL(20,8) -- 预警阈值
- created_at/updated_at
```

### provider_channel（上游通道）
```sql
- id: BIGINT PK
- provider_id: BIGINT FK
- channel_code: VARCHAR(32) -- 通道编码 haoyun_alih5
- channel_name: VARCHAR(64) -- 通道名称 好运支付-支付宝H5
- pay_type: VARCHAR(16) -- 支付类型 ALIPAY_H5/WECHAT_H5
- fee_rate: DECIMAL(5,4) -- 费率 0.025
- min_amount/max_amount: BIGINT -- 单笔限额(分)
- daily_limit: BIGINT -- 日限额(分)
- status: TINYINT -- 0-维护 1-在线
- created_at/updated_at
```

### payment_product（支付产品）
```sql
- id: BIGINT PK
- product_code: VARCHAR(32) UNIQUE -- oopayalih5
- product_name: VARCHAR(64) -- OOpay支付宝H5
- pay_type: VARCHAR(16) -- ALIPAY_H5
- default_fee_rate: DECIMAL(5,4) -- 默认费率 0.025
- status: TINYINT -- 0-下线 1-上线
- created_at/updated_at
```

### product_channel_mapping（产品通道映射）
```sql
- id: BIGINT PK
- product_id: BIGINT FK
- channel_id: BIGINT FK
- weight: INT -- 权重 60
- priority: INT -- 优先级 1
- is_backup: TINYINT -- 是否备用通道
- status: TINYINT
```

### merchant（商户）
```sql
- id: BIGINT PK
- merchant_code: VARCHAR(32) UNIQUE -- M001
- merchant_name: VARCHAR(64)
- email/phone
- login_pwd/pay_pwd
- invite_code: VARCHAR(32) -- 注册用的邀请码
- status: TINYINT -- 0-禁用 1-正常 2-冻结
- created_at/updated_at
```

### merchant_app（商户应用）
```sql
- id: BIGINT PK
- merchant_id: BIGINT FK
- app_id: VARCHAR(32) UNIQUE -- APP001
- app_name: VARCHAR(64)
- api_key/secret_key
- notify_url: VARCHAR(256)
- status: TINYINT
```

### merchant_product（商户产品授权）
```sql
- id: BIGINT PK
- merchant_id: BIGINT FK
- product_id: BIGINT FK
- fee_rate: DECIMAL(5,4) -- 独立费率，NULL表示继承默认
- status: TINYINT -- 是否对该商户开放
```

### pay_order（支付订单）
```sql
- id: BIGINT PK
- pay_order_id: VARCHAR(32) UNIQUE -- OO20240407123456
- merchant_id: BIGINT FK
- app_id: BIGINT FK
- product_id: BIGINT FK
- channel_id: BIGINT FK -- 实际使用的上游通道
- mch_order_no: VARCHAR(64) -- 商户订单号
- amount: BIGINT -- 金额(分)
- currency: VARCHAR(8) -- CNY
- status: TINYINT -- 0-生成 1-支付中 2-成功 3-完成 4-关闭
- pay_succ_time: TIMESTAMP
- notify_url/return_url
- client_ip: VARCHAR(32)
- subject/body
- param1/param2 -- 扩展参数
- frozen_quota: DECIMAL(20,8) -- 冻结的额度
- platform_profit: DECIMAL(20,8) -- 平台利润(USDT)
- created_at/updated_at
```

### account（账户余额）
```sql
- id: BIGINT PK
- account_type: TINYINT -- 1-商户 2-厂商
- owner_id: BIGINT -- 商户ID或厂商ID
- usdt_balance: DECIMAL(20,8) -- USDT余额
- usdt_frozen: DECIMAL(20,8) -- 冻结金额
- created_at/updated_at
```

### account_log（资金流水）
```sql
- id: BIGINT PK
- account_id: BIGINT FK
- order_id: BIGINT -- 关联订单
- change_type: TINYINT -- 1-订单收入 2-手续费 3-提现 4-人工调整
- amount: DECIMAL(20,8) -- 变动金额(正收入负支出)
- before_balance/after_balance
- remark: VARCHAR(256)
- created_at
```

### exchange_rate（汇率记录）
```sql
- id: BIGINT PK
- base_currency: VARCHAR(8) -- USDT
- target_currency: VARCHAR(8) -- CNY
- source: VARCHAR(16) -- OKX
- rate: DECIMAL(10,4) -- 汇率 7.25
- spread: DECIMAL(5,4) -- 汇差 0.1
- final_rate: DECIMAL(10,4) -- 最终汇率 7.35
- fetched_at: TIMESTAMP
```

### sys_config（系统配置）
```sql
- id: BIGINT PK
- config_key: VARCHAR(64) UNIQUE
- config_value: VARCHAR(512)
- description: VARCHAR(256)
```

关键配置项：
- `order.timeout` - 订单超时时间(分钟) 默认30
- `order.query.interval` - 兜底查询间隔(分钟) 默认5
- `order.query.max_time` - 最大查询时长(小时) 默认2
- `notify.retry.times` - 回调重试次数 默认3
- `quota.alert.threshold` - 额度预警阈值(CNY) 默认1000
- `exchange.rate.source` - 汇率来源 OKX
- `exchange.rate.interval` - 汇率更新间隔(分钟) 默认5

---

## 索引设计

### 高频查询索引
```sql
-- 订单查询
CREATE INDEX idx_pay_order_merchant ON pay_order(merchant_id, created_at);
CREATE INDEX idx_pay_order_mch_no ON pay_order(merchant_id, mch_order_no);
CREATE INDEX idx_pay_order_status ON pay_order(status, created_at);

-- 资金流水
CREATE INDEX idx_account_log_account ON account_log(account_id, created_at);
CREATE INDEX idx_account_log_order ON account_log(order_id);

-- 额度变动
CREATE INDEX idx_quota_log_provider ON provider_quota_log(provider_id, created_at);
```

### 唯一约束
```sql
-- 商户订单号唯一（商户维度）
UNIQUE(merchant_id, mch_order_no)

-- 产品编码唯一
UNIQUE(product_code)

-- 厂商编码唯一
UNIQUE(provider_code)
```
