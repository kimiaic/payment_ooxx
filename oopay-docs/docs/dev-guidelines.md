# OOPay 开发规范文档

## 1. 代码风格

### 1.1 基础规范

- **编码**: UTF-8
- **缩进**: 4 空格（禁止使用 Tab）
- **换行**: LF (Unix 风格)
- **行长**: 最大 120 字符
- **花括号**: K&R 风格，左括号不换行

### 1.2 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | `OrderService`, `PayChannel` |
| 接口名 | PascalCase | `PaymentChannel`, `MerchantRepository` |
| 方法名 | camelCase | `createOrder()`, `validateSignature()` |
| 变量名 | camelCase | `merchantNo`, `orderAmount` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `ORDER_EXPIRE_MINUTES` |
| 包名 | 全小写 | `com.oopay.payment.service` |
| 数据库表 | 小写下划线 | `oopay_order`, `oopay_merchant` |
| 数据库字段 | 小写下划线 | `merchant_no`, `create_time` |

### 1.3 类结构顺序

```java
public class OrderService {
    
    // 1. 静态常量
    private static final int MAX_RETRY = 5;
    
    // 2. 依赖注入（构造函数注入优先）
    private final OrderRepository orderRepository;
    
    // 3. 构造方法
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    // 4. 公共方法（按调用顺序排列）
    public Order createOrder(CreateOrderRequest request) { ... }
    
    // 5. 私有方法
    private void validateRequest(CreateOrderRequest request) { ... }
    
    // 6. 内部类
    private static class OrderBuilder { ... }
}
```

### 1.4 注释规范

```java
/**
 * 订单服务
 * 负责订单的创建、查询、关闭等核心业务逻辑
 *
 * @author OOPay Team
 * @since 1.0.0
 */
public class OrderService {
    
    /**
     * 创建支付订单
     *
     * @param request 创建订单请求参数
     * @return 创建的订单信息
     * @throws BusinessException 当商户不存在或金额超限时
     */
    public Order createOrder(CreateOrderRequest request) {
        // 1. 参数校验
        validateRequest(request);
        
        // 2. 幂等校验（24小时窗口）
        checkIdempotency(request);
        
        // TODO: 需要补充风控检查
        
        return doCreateOrder(request);
    }
}
```

---

## 2. 提交规范

### 2.1 Commit Message 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 2.2 Type 类型

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(payment): add order creation API` |
| `fix` | 修复 Bug | `fix(account): correct balance calculation` |
| `docs` | 文档更新 | `docs: update API reference` |
| `style` | 代码格式 | `style: fix indentation` |
| `refactor` | 重构 | `refactor(merchant): simplify audit flow` |
| `test` | 测试相关 | `test(payment): add unit tests` |
| `chore` | 构建/工具 | `chore: upgrade Spring Boot to 3.2.5` |
| `perf` | 性能优化 | `perf(order): optimize query with index` |
| `security` | 安全修复 | `security: fix SQL injection vulnerability` |

### 2.3 提交示例

```bash
# 功能开发
feat(payment): implement order creation with idempotency check

- Add CreateOrderRequest/Response DTOs
- Implement OrderService.createOrder() with 24h idempotency window
- Add OrderController endpoint POST /api/pay/create

Closes #123

# Bug 修复
fix(account): prevent negative balance after concurrent withdrawals

Add optimistic locking with version field to prevent race condition.

Fixes #456
```

### 2.4 提交频率

- **单个功能**: 1-3 个 commit
- **Bug 修复**: 1 个 commit
- **重构**: 按模块拆分多个 commit
- **禁止**: 批量无意义提交（如 "update", "fix"）

---

## 3. 分支命名规范

### 3.1 分支类型

| 分支 | 命名格式 | 示例 |
|------|----------|------|
| 主分支 | `main` | - |
| 开发分支 | `develop` | - |
| 功能分支 | `feature/<模块>-<描述>` | `feature/payment-idempotency` |
| 修复分支 | `fix/<模块>-<描述>` | `fix/account-balance-race` |
| 热修复 | `hotfix/<描述>` | `hotfix/security-patch` |
| 发布分支 | `release/<版本>` | `release/1.0.0` |

### 3.2 命名规则

- 全小写
- 使用连字符 `-` 分隔
- 描述简洁明确（不超过 5 个单词）
- 关联 Issue 时附加编号: `feature/payment-#123-refund`

---

## 4. PR 审查规范

### 4.1 PR 模板

```markdown
## 描述
<!-- 描述这个 PR 做了什么 -->

## 变更类型
- [ ] 新功能
- [ ] Bug 修复
- [ ] 文档更新
- [ ] 代码重构
- [ ] 性能优化
- [ ] 测试补充

## 检查清单
- [ ] 代码编译通过
- [ ] 单元测试通过
- [ ] 代码符合规范（Checkstyle）
- [ ] 提交信息符合规范
- [ ] 文档已更新（如需要）

## 相关 Issue
Fixes #123
Relates to #456

## 截图（如适用）
```

### 4.2 审查流程

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   提交 PR   │───>│  CI 检查    │───>│  代码审查   │
└─────────────┘    └─────────────┘    └──────┬──────┘
                                             │
                              ┌──────────────┴──────────────┐
                              ▼                              ▼
                        ┌─────────┐                   ┌─────────┐
                        │ 需要修改 │──────────────────>│  已批准  │
                        └─────────┘   修改后重新请求审查  └────┬────┘
                                                               │
                                                               ▼
                                                        ┌─────────────┐
                                                        │  合并到 develop │
                                                        └─────────────┘
```

### 4.3 审查检查点

| 检查项 | 说明 |
|--------|------|
| 功能正确性 | 代码是否实现了预期功能 |
| 边界处理 | 异常、空值、极限值处理 |
| 安全性 | SQL 注入、XSS、敏感信息泄露 |
| 性能 | N+1 查询、循环内 RPC、大对象 |
| 可读性 | 命名清晰、注释充分、逻辑简洁 |
| 测试覆盖 | 核心逻辑是否有单元测试 |

### 4.4 审查用语

**Request Changes**:
- "这里存在并发安全问题，建议使用乐观锁"
- "缺少对金额参数的校验，需要添加 @Min(1)"
- "请补充单元测试覆盖这个分支"

**Approve with Suggestions** (非阻塞):
- "建议提取这个魔数为常量"
- "Nit: 变量名可以更具体一些"

---

## 5. 项目结构规范

### 5.1 包结构

```
com.oopay.{模块}
├── controller          # REST API 控制器
│   ├── request         # 请求 DTO
│   └── response        # 响应 DTO
├── service             # 业务逻辑层
│   ├── impl            # 实现类
│   └── dto             # 内部 DTO
├── repository          # 数据访问层
│   ├── entity          # JPA 实体
│   └── mapper          # MyBatis Mapper（如使用）
├── config              # 配置类
├── util                # 工具类（模块内）
├── exception           # 自定义异常
├── enums               # 枚举（模块内）
└── job                 # 定时任务
```

### 5.2 分层规则

| 层级 | 职责 | 禁止 |
|------|------|------|
| Controller | 参数校验、调用 Service、返回结果 | 业务逻辑、直接操作数据库 |
| Service | 业务逻辑、事务控制、跨服务调用 | HTTP 请求处理、SQL 拼接 |
| Repository | 数据访问、简单查询封装 | 业务逻辑、事务控制 |

---

## 6. 数据库规范

### 6.1 建表规范

```sql
CREATE TABLE oopay_order (
    -- 主键
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    
    -- 业务字段
    order_no            VARCHAR(32)     NOT NULL COMMENT '订单号',
    merchant_no         VARCHAR(32)     NOT NULL COMMENT '商户号',
    amount              BIGINT          NOT NULL COMMENT '金额（分）',
    
    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    update_by           BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除：0-正常，1-删除',
    version             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    
    -- 约束
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_merchant_no (merchant_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';
```

### 6.2 索引规范

- 主键：单字段自增 BIGINT
- 唯一索引：`uk_{表名}_{字段}`
- 普通索引：`idx_{表名}_{字段}`
- 联合索引：按区分度高低排序
- 禁止：冗余索引、过多索引（单表 < 5 个）

---

## 7. 安全检查清单

### 7.1 代码提交前自检

- [ ] 密码使用 bcrypt 加密存储
- [ ] 敏感数据（手机号、密钥）使用 AES-256-GCM 加密
- [ ] SQL 使用参数绑定（防止 SQL 注入）
- [ ] 用户输入做 XSS 过滤
- [ ] 接口有防重放保护（timestamp + nonce）
- [ ] 关键操作有签名验证
- [ ] 日志不包含敏感信息
- [ ] 密钥不硬编码在代码中

### 7.2 敏感操作日志

```java
// 必须记录日志的操作：
// 1. 登录/登出
// 2. 密钥重置
// 3. 资金变动（支付、退款、提现）
// 4. 配置变更
// 5. 权限变更

log.info("[SECURITY] User {} performed {} on merchant {}", 
    userId, action, merchantNo);
```

---

## 8. 工具配置

### 8.1 Checkstyle 配置

文件位置：`config/checkstyle.xml`

关键规则：
- 行长度 <= 120
- 缩进 4 空格
- 导入顺序检查
- Javadoc 必填（公共类和方法）

### 8.2 IDE 配置

**IntelliJ IDEA**:
1. `Settings -> Editor -> Code Style -> Java`
2. 导入 `config/intellij-java-style.xml`
3. 启用 `Reformat on Save`

**VS Code**:
安装插件：
- Checkstyle for Java
- Java Extension Pack

---

## 9. 参考文档

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c)
