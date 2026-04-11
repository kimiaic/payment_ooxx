# OOPay 贡献指南

## 1. 代码风格规范

### 1.1 Java 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase，名词 | `OrderService`, `PaymentController`, `MerchantRepository` |
| 接口名 | PascalCase，形容词或名词 | `PaymentChannel`, `MerchantValidator` |
| 方法名 | camelCase，动词开头 | `createOrder()`, `validateSignature()`, `getMerchantById()` |
| 变量名 | camelCase | `merchantNo`, `orderAmount`, `channelList` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `ORDER_EXPIRE_MINUTES`, `DEFAULT_FEE_RATE` |
| 包名 | 全小写 | `com.oopay.payment.service` |
| 数据库表 | 小写下划线，oopay_前缀 | `oopay_order`, `oopay_merchant` |
| 数据库字段 | 小写下划线 | `merchant_no`, `create_time`, `pay_status` |

**示例代码：**

```java
/**
 * 订单服务类
 */
@Service
public class OrderService {
    
    // 常量定义
    private static final int MAX_RETRY_COUNT = 5;
    private static final long ORDER_EXPIRE_MINUTES = 30;
    private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.006");
    
    // 依赖注入
    private final OrderRepository orderRepository;
    private final MerchantService merchantService;
    
    // 构造方法注入
    public OrderService(OrderRepository orderRepository, MerchantService merchantService) {
        this.orderRepository = orderRepository;
        this.merchantService = merchantService;
    }
    
    /**
     * 创建支付订单
     * @param request 创建订单请求
     * @return 订单响应
     */
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        // 参数校验
        validateRequest(request);
        
        // 生成订单号
        String orderNo = generateOrderNo();
        
        // 计算手续费（使用常量，禁止魔法数字）
        BigDecimal fee = request.getAmount().multiply(DEFAULT_FEE_RATE);
        
        // 保存订单
        Order order = buildOrder(request, orderNo, fee);
        orderRepository.save(order);
        
        return new CreateOrderResponse(orderNo);
    }
    
    private void validateRequest(CreateOrderRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("订单金额必须大于0");
        }
    }
}
```

### 1.2 Checkstyle 配置

项目使用 Checkstyle 进行代码风格检查，配置文件位于项目根目录 `checkstyle.xml`。

**关键规则：**
- 行长度限制：120 字符
- 缩进：4 空格（禁止 Tab）
- 花括号：K&R 风格（左括号不换行）
- 导入顺序：java → javax → org → com → 其他
- Javadoc：公共类和方法必须包含

**执行检查：**
```bash
mvn checkstyle:check
```

**IDEA 配置：**
1. Settings → Editor → Code Style → Java
2. 导入项目根目录的 `checkstyle.xml`
3. 启用 "Reformat on Save"

### 1.3 禁止魔法数字

所有数值必须使用有意义的常量，特别是金额相关。

**❌ 错误示例：**
```java
// 魔法数字，无法理解含义
if (amount > 1000000) {
    throw new LimitExceededException();
}

BigDecimal fee = amount.multiply(new BigDecimal("0.006"));
```

**✅ 正确示例：**
```java
// 定义常量
private static final BigDecimal SINGLE_LIMIT_AMOUNT = new BigDecimal("10000"); // 100元 = 10000分
private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.006");    // 0.6%费率

// 使用常量
if (amount.compareTo(SINGLE_LIMIT_AMOUNT) > 0) {
    throw new LimitExceededException("单笔金额超过限制: " + SINGLE_LIMIT_AMOUNT);
}

BigDecimal fee = amount.multiply(DEFAULT_FEE_RATE);
```

**金额常量命名规范：**
| 场景 | 命名示例 | 值 |
|------|----------|-----|
| 单笔限额 | `SINGLE_LIMIT_AMOUNT` | 10000 (100元，单位分) |
| 日限额 | `DAILY_LIMIT_AMOUNT` | 1000000 (10000元) |
| 默认费率 | `DEFAULT_FEE_RATE` | 0.006 (0.6%) |
| 最小金额 | `MIN_PAY_AMOUNT` | 100 (1元) |
| 超时时间(分钟) | `ORDER_EXPIRE_MINUTES` | 30 |

---

## 2. Git 提交规范（Conventional Commits）

### 2.1 提交格式

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### 2.2 Type 枚举

| Type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(payment): add unified order creation API` |
| `fix` | Bug 修复 | `fix(account): correct balance calculation error` |
| `docs` | 文档更新 | `docs: update API reference for v1.0` |
| `refactor` | 代码重构 | `refactor(merchant): simplify audit flow logic` |
| `test` | 测试相关 | `test(payment): add unit tests for order service` |
| `chore` | 构建/工具 | `chore: upgrade Spring Boot to 3.2.5` |
| `style` | 代码格式 | `style: fix indentation in OrderController` |
| `perf` | 性能优化 | `perf(order): optimize query with index` |
| `security` | 安全修复 | `security: fix SQL injection in merchant query` |

### 2.3 Scope 枚举

| Scope | 说明 | 示例 |
|-------|------|------|
| `phase0` ~ `phase7` | 开发阶段标记 | `feat(phase0): init project structure` |
| `gateway` | 网关模块 | `fix(gateway): fix rate limiter config` |
| `merchant` | 商户服务 | `feat(merchant): add merchant audit API` |
| `payment` | 支付核心 | `feat(payment): implement order status machine` |
| `account` | 账户资金 | `fix(account): fix concurrent balance update` |
| `notify` | 通知服务 | `feat(notify): add retry mechanism` |
| `admin` | 管理后台 | `feat(admin): add dashboard statistics` |
| `common` | 共享模块 | `refactor(common): extract base entity` |
| `db` | 数据库 | `chore(db): add order table index` |
| `ci` | CI/CD | `chore(ci): add GitHub Actions workflow` |

### 2.4 提交示例

**功能开发：**
```bash
feat(payment): add unified order creation API

- Implement OrderService.createOrder() with idempotency check
- Add OrderController endpoint POST /api/pay/create
- Add request/response DTOs with validation
- Implement 24-hour idempotency window using Redis

Closes #123
```

**Bug 修复：**
```bash
fix(account): prevent negative balance in concurrent withdrawal

Add optimistic locking with @Version field to prevent race condition
when multiple threads update account balance simultaneously.

Fixes #456
```

**文档更新：**
```bash
docs(phase0): add development environment setup guide

- Add Java 21 installation steps
- Add Docker commands for MySQL/Redis/RabbitMQ
- Add Maven configuration with Aliyun mirror
```

**数据库变更：**
```bash
chore(db): add index on oopay_order table

- Add composite index idx_merchant_create_time for query optimization
- Reduce query time from 200ms to 20ms
```

### 2.5 提交频率

- **单个功能**：1-3 个 commit
- **Bug 修复**：1 个 commit
- **重构**：按模块拆分，每个模块 1 个 commit
- **禁止**：无意义的批量提交（如 "update", "fix bug", "修改"）

---

## 3. 分支命名规范

### 3.1 主分支

| 分支 | 用途 | 保护规则 |
|------|------|----------|
| `main` | 生产环境代码 | 禁止直接 push，只接受 PR |
| `develop` | 开发主分支 | 禁止直接 push，只接受 PR |

### 3.2 临时分支

| 分支类型 | 命名格式 | 示例 | 说明 |
|----------|----------|------|------|
| 功能分支 | `feature/模块-描述` | `feature/payment-core` | 新功能开发 |
| 修复分支 | `fix/模块-描述` | `fix/order-status-bug` | Bug 修复 |
| 热修复 | `hotfix/描述` | `hotfix/security-patch` | 生产紧急修复 |
| 发布分支 | `release/版本号` | `release/1.0.0` | 版本发布准备 |

### 3.3 命名规则

- **全小写**：统一使用小写字母
- **连字符分隔**：使用 `-` 分隔单词
- **简洁明确**：描述不超过 5 个单词
- **关联 Issue**：可附加 Issue 编号，如 `feature/payment-#123-refund`

### 3.4 分支示例

```bash
# 功能开发
feature/payment-core           # 支付核心功能
feature/merchant-audit         # 商户审核功能
feature/account-balance-query  # 账户余额查询

# Bug 修复
fix/order-status-bug           # 修复订单状态问题
fix/concurrent-balance-update  # 修复并发余额更新
fix/gateway-timeout            # 修复网关超时

# 热修复（从 main 分支切出）
hotfix/security-patch          # 安全补丁
hotfix/payment-callback        # 支付回调修复
```

### 3.5 分支生命周期

```
feature/xxx 或 fix/xxx
        │
        ▼
   开发完成
        │
        ▼
  提交 PR 到 develop
        │
        ▼
   Code Review
        │
        ▼
   合并到 develop
        │
        ▼
   发布时合并到 main
```

---

## 4. PR 审查规范

### 4.1 PR 模板

创建 PR 时必须使用以下模板：

```markdown
## 描述
<!-- 简述这个 PR 做了什么，解决了什么问题 -->

## 变更类型
- [ ] 新功能 (feat)
- [ ] Bug 修复 (fix)
- [ ] 文档更新 (docs)
- [ ] 代码重构 (refactor)
- [ ] 性能优化 (perf)
- [ ] 测试补充 (test)
- [ ] 其他 (chore)

## 测试方式
<!-- 如何验证这个变更是否正确 -->
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试步骤：...

## 关联 Issue
<!-- 关联的 Issue 或任务 -->
Fixes #123
Relates to #456
Phase: Phase 2 - 核心服务

## 检查清单
- [ ] 代码编译通过 `mvn clean compile`
- [ ] 单元测试通过 `mvn test`
- [ ] Checkstyle 检查通过
- [ ] 提交信息符合规范
- [ ] 文档已更新（如需要）

## 截图（如适用）
<!-- API 响应、界面截图等 -->
```

### 4.2 PR 创建规范

1. **必须关联 Issue 或 Phase 任务**
   - 在 PR 描述中明确关联的 Issue 编号
   - 如果是 Phase 任务，注明所属阶段

2. **必须通过 CI 才能合并**
   - GitHub Actions 必须全部通过
   - 代码覆盖率不能下降

3. **必须至少 1 个 Reviewer 批准**
   - 核心业务代码需要 2 个 Reviewer

### 4.3 审查流程

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  创建 PR    │────>│  CI 检查    │────>│  代码审查   │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                              ┌────────────────┴────────────────┐
                              ▼                                 ▼
                        ┌───────────┐                   ┌───────────┐
                        │ 需要修改  │──────────────────>│  已批准   │
                        └───────────┘   修改后重新请求审查  └─────┬─────┘
                                                                  │
                                                                  ▼
                                                           ┌─────────────┐
                                                           │  合并到目标分支 │
                                                           └─────────────┘
```

### 4.4 审查检查点

| 检查项 | 说明 | 优先级 |
|--------|------|--------|
| 功能正确性 | 代码是否实现了预期功能 | P0 |
| 边界处理 | 异常、空值、极限值处理 | P0 |
| 安全性 | SQL 注入、XSS、敏感信息泄露 | P0 |
| 并发安全 | 多线程、分布式锁、幂等性 | P0 |
| 性能 | N+1 查询、循环内 RPC、大对象 | P1 |
| 可读性 | 命名清晰、注释充分、逻辑简洁 | P1 |
| 测试覆盖 | 核心逻辑是否有单元测试 | P1 |

### 4.5 审查用语

**需要修改（Request Changes）：**
- "这里存在并发安全问题，建议使用乐观锁 @Version"
- "缺少对金额参数的校验，需要添加 @Min(1)"
- "请补充单元测试覆盖这个分支逻辑"
- "SQL 拼接存在注入风险，请使用参数绑定"

**建议优化（Approve with Suggestions）：**
- "建议提取这个魔法数为常量"
- "Nit: 变量名可以更具体一些，如 `orderNo` 改为 `merchantOrderNo`"
- "建议添加注释说明这里的业务逻辑"

**批准（Approve）：**
- "代码清晰，测试完整，可以合并"
- "LGTM (Looks Good To Me)"

---

## 5. 快速参考

### 5.1 提交前自检

```bash
# 1. 代码格式检查
mvn checkstyle:check

# 2. 编译检查
mvn clean compile

# 3. 单元测试
mvn test

# 4. 查看变更文件
git status

# 5. 查看提交信息格式
git log -1 --format=%B
```

### 5.2 常见命令

```bash
# 创建功能分支
git checkout -b feature/payment-core develop

# 提交代码
git add .
git commit -m "feat(payment): add unified order creation API"

# 推送到远程
git push -u origin feature/payment-core

# 创建 PR 后合并到 develop
git checkout develop
git pull origin develop
git merge --no-ff feature/payment-core
git push origin develop
```

---

*最后更新：2026-04-11*
