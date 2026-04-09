# 开发规范 (Contributing Guide)

> OOPay 支付系统开发规范

## 1. 开发流程

### 1.1 分支管理

```
main        - 主分支，稳定版本
  └── develop   - 开发分支
       └── feature/*  - 功能分支
       └── fix/*      - 修复分支
```

### 1.2 提交规范

**Commit Message 格式**：
```
<type>(scope): subject

body

footer
```

**Type 类型**：
| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 |
| docs | 文档 |
| style | 格式（不影响代码运行） |
| refactor | 重构 |
| perf | 性能优化 |
| test | 测试 |
| chore | 构建/工具 |

**示例**：
```
feat(payment): 添加 USDT 支付通道支持

- 实现 TRC20 转账查询
- 添加汇率缓存机制
- 集成大额风控检查

Closes #123
```

## 2. 代码规范

### 2.1 代码风格

**Java 代码**：
- 遵循 Google Java Style Guide
- 缩进：4 个空格
- 最大行宽：120 字符
- 类注释必须包含 @author 和 @since

**SQL 代码**：
- 关键字大写
- 表名：小写 + 下划线
- 字段名：小写 + 下划线
- 必须包含注释

### 2.2 关键规范

**金额处理**：
```java
// ✅ CNY 使用 BIGINT（分）
private Long amount;  // 单位：分

// ✅ USDT 使用 DECIMAL(20,8)
private BigDecimal usdtAmount;

// ❌ 禁止使用 Double/Float
```

**异常处理**：
```java
// ✅ 业务异常使用自定义异常
throw new PaymentException(ErrorCode.ORDER_NOT_FOUND);

// ✅ 异常必须记录日志
catch (Exception e) {
    log.error("支付处理失败, orderNo={}", orderNo, e);
    throw new PaymentException(ErrorCode.SYSTEM_ERROR);
}
```

**并发控制**：
```java
// ✅ 使用乐观锁
@Version
private Integer version;

// ✅ Redis 分布式锁
String lockKey = "lock:order:" + orderNo;
RLock lock = redissonClient.getLock(lockKey);
```

## 3. 安全检查清单

### 3.1 P0 项（上线前必须）

- [ ] 所有接口都有 HMAC-SHA256 签名验证
- [ ] 时间戳重放保护（5分钟窗口）
- [ ] Nonce 唯一性校验（Redis）
- [ ] 敏感数据 AES-256 加密存储
- [ ] 密码 bcrypt 哈希（cost≥12）
- [ ] SQL 注入防护（使用参数化查询）
- [ ] XSS 防护（输出转义）

### 3.2 P1 项（高优先级）

- [ ] 接口限流实现
- [ ] 敏感操作审计日志
- [ ] 订单状态机校验
- [ ] 数据库乐观锁
- [ ] Saga 事务补偿

### 3.3 代码审查重点

**每次 PR 必须检查**：
1. 是否包含单元测试？
2. 是否更新了相关文档？
3. 是否遵循金额处理规范？
4. 是否处理了所有异常路径？
5. 是否添加了必要的日志？

## 4. 测试规范

### 4.1 测试覆盖要求

| 类型 | 覆盖率要求 |
|------|-----------|
| 单元测试 | ≥ 80% |
| 集成测试 | 核心流程必须覆盖 |
| 接口测试 | 所有 API 端点 |

### 4.2 测试命名规范

```java
// 类名
OrderServiceTest
OrderControllerTest

// 方法名
shouldCreateOrderSuccessfully()
shouldThrowExceptionWhenOrderNotFound()
shouldRejectInvalidSignature()
```

### 4.3 测试数据

```java
// 使用 TestDataFactory 创建测试数据
Order order = TestDataFactory.createOrder()
    .withAmount(10000L)  // 100元
    .withPayType(PayType.WECHAT)
    .build();
```

## 5. 文档规范

### 5.1 代码注释

```java
/**
 * 创建支付订单
 * 
 * @param request 支付请求
 * @return 订单信息
 * @throws PaymentException 参数校验失败或系统错误
 * @author developer
 * @since 1.0.0
 */
public Order createOrder(PayRequest request) {
    // 实现
}
```

### 5.2 API 文档

所有接口必须包含：
- 接口功能描述
- 请求/响应参数说明
- 错误码列表
- 示例

## 6. 数据库规范

### 6.1 命名规范

| 对象 | 规范 | 示例 |
|------|------|------|
| 表名 | 小写 + 下划线 | `oopay_order` |
| 字段名 | 小写 + 下划线 | `create_time` |
| 索引名 | `idx_` + 表名 + 字段 | `idx_order_merchant_id` |
| 主键 | `id` BIGINT | - |

### 6.2 字段规范

所有表必须包含：
```sql
create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
create_by       BIGINT      DEFAULT NULL COMMENT '创建人 ID',
update_by       BIGINT      DEFAULT NULL COMMENT '更新人 ID',
deleted         TINYINT     NOT NULL DEFAULT 0 COMMENT '软删除标志',
version         INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
```

### 6.3 索引规范

```sql
-- 必须添加索引的场景
-- 1. 外键字段
-- 2. WHERE 条件常用字段
-- 3. ORDER BY 字段
-- 4. 联合查询字段

-- 示例
INDEX idx_status (status),
INDEX idx_create_time (create_time),
UNIQUE KEY uk_merchant_order (merchant_no, merchant_order_no, deleted)
```

## 7. 发布流程

### 7.1 发布检查清单

- [ ] 所有单元测试通过
- [ ] 代码审查通过
- [ ] 安全扫描通过
- [ ] 数据库迁移脚本已准备
- [ ] 回滚方案已准备
- [ ] 监控告警已配置

### 7.2 版本号规范

遵循 [SemVer](https://semver.org/lang/zh-CN/)：

```
主版本号.次版本号.修订号

1.0.0    # 初始版本
1.1.0    # 新增功能（USDT支持）
1.1.1    # 修复 bug
2.0.0    # 不兼容改动
```

## 8. 故障处理

### 8.1 问题分级

| 级别 | 定义 | 响应时间 | 处理时间 |
|------|------|----------|----------|
| P0 | 核心支付不可用 | 5分钟 | 30分钟 |
| P1 | 部分功能异常 | 15分钟 | 2小时 |
| P2 | 性能下降 | 30分钟 | 4小时 |
| P3 | 一般问题 | 2小时 | 1天 |

### 8.2 故障处理流程

1. **发现** - 监控告警或用户反馈
2. **确认** - 快速确认影响范围
3. **止损** - 立即采取措施止损
4. **定位** - 分析问题根因
5. **修复** - 实施修复方案
6. **验证** - 确认问题已解决
7. **复盘** - 记录并总结经验

---

**参考文档**：
- [安全设计](./docs/security/security-design.md)
- [运营手册](./docs/ops/operations.md)
