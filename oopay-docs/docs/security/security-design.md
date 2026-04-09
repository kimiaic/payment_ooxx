# OOPay 安全设计文档

## 1. 安全架构概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              安全防御体系                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐│
│   │   接入层安全  │   │   传输层安全  │   │   应用层安全  │   │   数据层安全  ││
│   ├──────────────┤   ├──────────────┤   ├──────────────┤   ├──────────────┤│
│   │ • IP 白名单   │   │ • TLS 1.3    │   │ • 签名验签    │   │ • 加密存储    ││
│   │ • 限流熔断    │   │ • HSTS       │   │ • 防重放      │   │ • 脱敏显示    ││
│   │ • WAF        │   │ • 证书固定    │   │ • 状态机校验  │   │ • 访问控制    ││
│   │ • DDoS 防护  │   │              │   │ • 敏感词过滤  │   │ • 审计日志    ││
│   └──────────────┘   └──────────────┘   └──────────────┘   └──────────────┘│
│                                                                             │
│   ┌──────────────────────────────────────────────────────────────────────┐ │
│   │                         业务安全层                                    │ │
│   ├──────────────────────────────────────────────────────────────────────┤ │
│   │  • Saga 事务补偿    • 多级对账机制    • 风控规则引擎    • 大额确认    │ │
│   └──────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 认证与签名机制

### 2.1 HMAC-SHA256 签名（当前实现）

```
签名算法流程：

1. 参数排序：将所有业务参数按 key 字典序排序
2. 拼接字符串：key1=value1&key2=value2&...&keyN=valueN
3. 添加密钥：string + "&key=" + apiSecret
4. 计算签名：sign = HMAC-SHA256(string, apiSecret).toUpperCase()
5. 验证方式：服务端使用相同算法计算，比对 sign 是否一致
```

**请求示例：**
```http
POST /api/pay/create
Content-Type: application/json
X-Merchant-No: M12345678901234
X-Timestamp: 1712649600
X-Sign: 3A8F2B1C...

{
  "merchantOrderNo": "ORDER001",
  "amount": 10000,
  "payType": "WECHAT_NATIVE",
  "notifyUrl": "https://..."
}
```

### 2.2 签名升级建议（推荐实现）

```
增强签名方案：

Headers:
  X-Merchant-No: {merchantNo}
  X-Timestamp: {unixTimestamp}
  X-Nonce: {32位随机字符串}
  X-Sign: {HMAC-SHA256签名}

签名内容：
  StringToSign = HTTP_METHOD + "\n" +
                 URI_PATH + "\n" +
                 X-Timestamp + "\n" +
                 X-Nonce + "\n" +
                 MD5(RequestBody) + "\n"

X-Sign = HMAC-SHA256(StringToSign, API_SECRET).toBase64()

优势：
  • 防重放：Nonce 唯一性 + Timestamp 时效性
  • 防篡改：包含请求体哈希
  • 防伪造：包含 HTTP 方法和路径
```

---

## 3. 防重放攻击机制

### 3.1 多重防护策略

```
┌─────────────────────────────────────────────────────────────────────┐
│                        防重放攻击防护层                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  第一层：Timestamp 时效检查                                          │
│  ─────────────────────────────────────                              │
│  • 服务端获取当前时间 current_time                                   │
│  • 计算差值：delta = current_time - X-Timestamp                      │
│  • 阈值：|delta| <= 300 秒（5分钟）                                  │
│  • 超时返回：SIGN_EXPIRED                                           │
│                                                                     │
│  第二层：Nonce 唯一性校验                                            │
│  ─────────────────────────────────────                              │
│  • Redis Key：nonce:{merchantNo}:{nonce}                            │
│  • TTL：300 秒（与 timestamp 窗口一致）                              │
│  • 已存在返回：REPLAY_ATTACK                                          │
│                                                                     │
│  第三层：请求幂等性保证                                              │
│  ─────────────────────────────────────                              │
│  • 业务 Key：idempotent:{merchantNo}:{merchantOrderNo}              │
│  • 首次请求：生成幂等 Token，执行业务                                │
│  • 重复请求：返回首次执行结果                                        │
│  • TTL：24 小时（匹配订单有效期）                                     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 Redis Lua 脚本实现

```lua
-- 防重放检查 + 幂等性保证（原子操作）
local nonce_key = KEYS[1]
local idempotent_key = KEYS[2]
local nonce_ttl = ARGV[1]
local idempotent_ttl = ARGV[2]
local result_value = ARGV[3]

-- 1. 检查 nonce 是否已存在
if redis.call('exists', nonce_key) == 1 then
    return {err = 'REPLAY_ATTACK'}
end

-- 2. 检查幂等 key
local existing = redis.call('get', idempotent_key)
if existing then
    return {ok = existing}  -- 返回已存在的结果
end

-- 3. 设置 nonce（标记已使用）
redis.call('setex', nonce_key, nonce_ttl, '1')

-- 4. 设置幂等 key（预留结果位置）
redis.call('setex', idempotent_key, idempotent_ttl, result_value)

return {ok = 'SUCCESS'}
```

---

## 4. IP 白名单与访问控制

### 4.1 多级 IP 控制

```sql
-- 商户级 IP 白名单配置
CREATE TABLE oopay_merchant_ip (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_no     VARCHAR(32) NOT NULL COMMENT '商户编号',
    ip_address      VARCHAR(64) NOT NULL COMMENT 'IP 地址，支持 CIDR 如 192.168.1.0/24',
    ip_type         TINYINT NOT NULL DEFAULT 1 COMMENT '1-白名单，2-黑名单',
    description     VARCHAR(256),
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 限流策略

| 层级 | 策略 | 阈值 | 实现 |
|------|------|------|------|
| 网关层 | 全局 QPS 限流 | 10000 req/s | Nginx/Redis |
| 商户层 | 单商户 QPS | 100 req/s | Redis 令牌桶 |
| 接口层 | 单接口限流 | 30 req/s | AOP 注解 |
| 用户层 | 单 IP 限流 | 60 req/min | Redis 滑动窗口 |

**令牌桶限流 Lua 脚本：**
```lua
-- 令牌桶限流
local key = KEYS[1]
local rate = tonumber(ARGV[1])      -- 每秒产生令牌数
local capacity = tonumber(ARGV[2])  -- 桶容量
local now = tonumber(ARGV[3])       -- 当前时间戳（毫秒）
local requested = tonumber(ARGV[4]) -- 请求令牌数

local bucket = redis.call('hmget', key, 'tokens', 'last_time')
local tokens = tonumber(bucket[1]) or capacity
local last_time = tonumber(bucket[2]) or now

-- 计算新增令牌
local delta = math.max(0, now - last_time)
local new_tokens = math.min(capacity, tokens + delta * rate / 1000)

-- 判断是否能够满足请求
local allowed = new_tokens >= requested
local remaining = new_tokens

if allowed then
    new_tokens = new_tokens - requested
    remaining = new_tokens
end

-- 更新桶状态
redis.call('hmset', key, 'tokens', new_tokens, 'last_time', now)
redis.call('expire', key, 60)

return {allowed and 1 or 0, remaining}
```

---

## 5. 数据一致性保障

### 5.1 Saga 事务补偿机制

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Saga 事务执行流程                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   正向流程：                                                         │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    │
│   │ 创建订单 │───►│ 冻结资金 │───►│ 调用通道 │───►│ 更新状态 │    │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘    │
│        │               │               │               │           │
│        │               │               │               │           │
│        ▼               ▼               ▼               ▼           │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    │
│   │ 记录日志 │    │ 记录日志 │    │ 记录日志 │    │ 记录日志 │    │
│   └──────────┘    └──────────┘    └──────────┘    └──────────┘    │
│                                                                     │
│   失败补偿：                                                         │
│   ┌──────────┐    ┌──────────┐    ┌──────────┐                     │
│   │ 更新失败 │◄───│ 解冻资金 │◄───│ 关闭订单 │◄── 任一环节失败     │
│   └──────────┘    └──────────┘    └──────────┘                     │
│                                                                     │
│   补偿执行器（异步）：                                               │
│   • 定时扫描未完成 Saga 事务                                         │
│   • 根据当前状态执行相应补偿操作                                     │
│   • 补偿失败进入死信队列人工介入                                     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 状态机校验

```java
// 订单状态机定义
public enum OrderStatus {
    PENDING,      // 待支付
    PROCESSING,   // 处理中
    SUCCESS,      // 支付成功
    FAILED,       // 支付失败
    CLOSED,       // 已关闭
    REFUNDING,    // 退款中
    REFUNDED      // 已退款
}

// 状态流转规则（只允许以下流转）
static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
    PENDING,     Set.of(PROCESSING, CLOSED),
    PROCESSING,  Set.of(SUCCESS, FAILED),
    SUCCESS,     Set.of(REFUNDING),
    REFUNDING,   Set.of(REFUNDED, SUCCESS),
    FAILED,      Set.of(),
    CLOSED,      Set.of(),
    REFUNDED,    Set.of()
);

// 状态变更检查
public void transition(OrderStatus from, OrderStatus to) {
    if (!TRANSITIONS.get(from).contains(to)) {
        throw new IllegalStateException(
            "Invalid transition: " + from + " -> " + to
        );
    }
}
```

### 5.3 数据库乐观锁

```sql
-- 账户扣减（带乐观锁）
UPDATE oopay_account 
SET balance = balance - #{amount},
    version = version + 1
WHERE account_no = #{accountNo}
  AND balance >= #{amount}
  AND version = #{version}

-- 影响行数为 0 表示：余额不足或版本冲突
```

---

## 6. 多级对账机制

### 6.1 对账架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                         多级对账体系                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  实时对账（分钟级）                                                   │
│  ─────────────────                                                  │
│  • 订单状态主动查询通道                                               │
│  • 支付结果与通道结果实时比对                                         │
│  • 异常立即告警                                                       │
│                                                                     │
│  定时对账（小时级）                                                   │
│  ─────────────────                                                  │
│  • 每小时扫描前一小时订单                                             │
│  • 批量查询通道订单状态                                               │
│  • 差异订单标记待处理                                                 │
│                                                                     │
│  日终对账（每日凌晨）                                                 │
│  ─────────────────                                                  │
│  • 下载通道对账文件                                                   │
│  • 全量订单逐笔比对                                                   │
│  • 生成差异报告                                                       │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.2 对账差异处理

| 差异类型 | 处理方式 | 自动修复 |
|----------|----------|----------|
| 系统有，通道无 | 订单超时关闭 | ✅ 是 |
| 通道有，系统无 | 补单处理 | ✅ 是 |
| 金额不符 | 标记人工审核 | ❌ 否 |
| 状态不符 | 以通道为准修正 | ✅ 是 |
| 重复支付 | 自动退款 | ✅ 是 |

---

## 7. TG Bot 大额二次确认

### 7.1 确认流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                    大额支付 TG Bot 确认流程                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   触发条件（任一满足）：                                              │
│   • 单笔金额 > 10,000 USDT                                          │
│   • 单笔金额 > 100,000 CNY                                          │
│   • 商户风控等级 = 高                                                │
│   • 新商户首笔交易                                                   │
│                                                                     │
│   流程：                                                             │
│   ┌─────────────┐                                                   │
│   │ 订单创建成功 │                                                   │
│   └──────┬──────┘                                                   │
│          │                                                          │
│          ▼                                                          │
│   ┌─────────────┐     发送 TG 消息     ┌─────────────┐              │
│   │ 风控检查    │─────────────────────▶│ TG Bot      │              │
│   │ 命中大额规则│                      │             │              │
│   └──────┬──────┘                      └─────────────┘              │
│          │                                                          │
│          │ 订单状态=PENDING_CONFIRM                                  │
│          ▼                                                          │
│   ┌─────────────┐                                                   │
│   │ 等待确认    │◄──── 超时 30 分钟自动取消                          │
│   │ (阻断支付)  │                                                   │
│   └──────┬──────┘                                                   │
│          │                                                          │
│     ┌────┴────┐                                                     │
│     ▼         ▼                                                     │
│ ┌───────┐ ┌───────┐                                                 │
│ │ 确认  │ │ 拒绝  │                                                 │
│ └───┬───┘ └───┬───┘                                                 │
│     │         │                                                     │
│     ▼         ▼                                                     │
│ ┌─────────┐ ┌─────────┐                                             │
│ │继续支付 │ │关闭订单 │                                             │
│ └─────────┘ └─────────┘                                             │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 7.2 TG Bot 消息格式

```
🚨 大额支付待确认

商户：{merchantName}
订单号：{orderNo}
金额：{amount} {currency}
支付方式：{payType}
时间：{createTime}

风险评估：{riskLevel}

[✅ 确认支付] [❌ 拒绝支付]

⚠️ 此订单将在 30 分钟后自动取消
```

---

## 8. 安全实施优先级

### 8.1 P0 - 必须实现（上线前）

| 项 | 说明 | 验证方式 |
|----|------|----------|
| HMAC-SHA256 签名 | 所有接口强制验签 | 单元测试覆盖 |
| Timestamp 防重放 | 5分钟窗口 | 集成测试 |
| IP 白名单 | 商户级配置 | 配置验证 |
| 限流熔断 | 网关层实现 | 压测验证 |
| TLS 1.3 | 强制 HTTPS | SSL 扫描 |
| 数据加密 | 敏感字段 AES-256 | 代码审计 |
| Saga 事务 | 支付核心流程 | 异常测试 |
| 状态机校验 | 订单状态流转 | 单元测试 |
| 乐观锁 | 资金操作 | 并发测试 |

### 8.2 P1 - 重要（上线后 2 周内）

| 项 | 说明 | 验证方式 |
|----|------|----------|
| Nonce 唯一性校验 | 增强防重放 | 集成测试 |
| 请求体签名 | 包含 body hash | 单元测试 |
| 多级对账 | 实时/定时/日终 | 对账测试 |
| TG Bot 确认 | 大额交易 | 流程测试 |
| 风控规则引擎 | 基础规则集 | 规则验证 |
| 操作审计日志 | 全量记录 | 日志审计 |
| SQL 注入防护 | 预编译语句 | 渗透测试 |
| XSS 防护 | 输出转义 | 渗透测试 |

### 8.3 P2 - 建议（上线后 1 个月内）

| 项 | 说明 | 验证方式 |
|----|------|----------|
| 敏感词过滤 | 交易备注等 | 功能测试 |
| 设备指纹 | 风险识别 | 效果评估 |
| 行为分析 | 异常检测 | 效果评估 |
| 证书固定 | 移动端 | 安全测试 |
| 数据脱敏 | 日志/展示 | 代码审计 |
| 密钥轮换 | 定期更新 | 流程验证 |
| 安全扫描 | 定期漏洞扫描 | 扫描报告 |

---

## 9. 安全测试 Checklist

### 9.1 认证与授权测试

- [ ] 缺少签名参数被拒绝
- [ ] 签名错误被拒绝
- [ ] 签名过期被拒绝（调 timestamp）
- [ ] 重复 nonce 被拒绝
- [ ] 未授权 IP 被拒绝
- [ ] Token 泄露后能否撤销

### 9.2 数据安全测试

- [ ] 敏感字段加密存储（数据库直接查询）
- [ ] 敏感字段脱敏展示（API 响应）
- [ ] 日志中无敏感信息
- [ ] SQL 注入尝试失败
- [ ] XSS  payload 被转义

### 9.3 业务安全测试

- [ ] 并发支付只成功一笔
- [ ] 余额不足支付失败
- [ ] 超时下订单自动关闭
- [ ] 重复回调只处理一次
- [ ] 状态非法流转被拒绝
- [ ] 负数金额被拒绝
- [ ] 超大金额被拒绝

### 9.4 性能与稳定性测试

- [ ] 限流阈值正确触发
- [ ] 熔断后自动恢复
- [ ] 高并发下数据一致
- [ ] 对账差异正确识别

---

*文档版本：v1.0*  
*最后更新：2025-04-09*
