# GitHub Actions 自动化流水线

本项目使用 GitHub Actions 实现**节点完成后自动触发下一个任务**的开发流水线。

## 🚀 快速开始

### 启动整个流水线

```bash
# 触发契约冻结，启动所有 Agent
gh workflow run 00-contract-freeze.yml -f contract_version=v1.0
```

### 查看状态

```bash
# 查看所有工作流运行状态
gh run list

# 查看特定 Agent 状态
gh run list --workflow="🤖 Agent-01 - Infra Core"

# 监视实时日志
gh run watch
```

---

## 📋 工作流说明

| 工作流 | 说明 | 触发方式 |
|--------|------|---------|
| `00-contract-freeze.yml` | 契约冻结 + 启动流水线 | 手动触发 |
| `01-agent-infra.yml` | Agent-01: 基础设施核心 | 自动触发 |
| `02-agent-gateway.yml` | Agent-02: 网关 | Agent-01 完成后 |
| `03-agent-merchant.yml` | Agent-03: 商户服务 | 契约冻结后 |
| `04-agent-payment.yml` | Agent-04: 支付核心 | 契约冻结后 |
| `05-agent-channel.yml` | Agent-05: 通道管理 | 契约冻结后 |
| `06-agent-account.yml` | Agent-06: 账户资金 | Phase 1 完成后 |
| `07-agent-refund.yml` | Agent-07: 退款服务 | Phase 1 完成后 |
| `08-agent-notify.yml` | Agent-08: 通知服务 | Phase 1 完成后 |
| `09-agent-reconcile.yml` | Agent-09: 对账中心 | Phase 2 完成后 |
| `10-agent-risk.yml` | Agent-10: 风控管理 | Phase 2 完成后 |
| `11-agent-admin.yml` | Agent-11: 管理后台 | Phase 3 完成后 |
| `12-agent-frontend.yml` | Agent-12: 前端 | Phase 3 完成后 |
| `20-integration-test.yml` | 集成测试 | Phase 4 完成后 |
| `21-load-test.yml` | 压力测试 | 集成测试通过后 |
| `99-state-monitor.yml` | 状态监控器 | 每 5 分钟 |

---

## 🔄 自动化流程

```
用户触发: gh workflow run 00-contract-freeze.yml
                │
                ▼
┌─────────────────────────────┐
│ 契约冻结 + 创建 Tag         │
└─────────────┬───────────────┘
              │
    ┌─────────┼─────────┐
    │         │         │
    ▼         ▼         ▼
 Agent-01  Agent-03  Agent-04
 (Infra)   (Merchant)(Payment)
    │                   │
    │         ┌─────────┘
    │         │
    ▼         ▼
 Agent-02   Agent-05
 (Gateway)  (Channel)
    │
    ▼ (State Monitor 检测)
┌─────────────────────────────┐
│   Phase 1 全部完成?         │
└─────────────┬───────────────┘
              │ 是
    ┌─────────┼─────────┐
    │         │         │
    ▼         ▼         ▼
 Agent-06  Agent-07  Agent-08
 (Account) (Refund)  (Notify)
    │         │
    └────┬────┘
         │
         ▼ (State Monitor 检测)
┌─────────────────────────────┐
│   Phase 2 全部完成?         │
└─────────────┬───────────────┘
              │ 是
    ┌─────────┴─────────┐
    │                   │
    ▼                   ▼
 Agent-09            Agent-10
 (Reconcile)         (Risk)
    │
    └────────────────────┐
                         │
                         ▼ (State Monitor 检测)
                ┌─────────────────┐
                │ Phase 3 完成?   │
                └────────┬────────┘
                         │ 是
              ┌──────────┼──────────┐
              │          │          │
              ▼          ▼          ▼
           Agent-11   Agent-12   集成测试
           (Admin)    (Frontend)
              │          │
              └────┬─────┘
                   │
                   ▼
              压力测试
                   │
                   ▼
              🎉 发布完成
```

---

## 📊 状态监控

State Monitor (`99-state-monitor.yml`) 每 5 分钟运行一次：

1. 检查各 Phase 的 Agent 完成状态
2. 自动触发下一个 Phase
3. 更新进度文件 (`.github/progress/status.json`)
4. 发送通知 (可选)

---

## 🔔 通知配置

### 飞书通知

在仓库 Settings → Secrets → Actions 中添加：

- `FEISHU_WEBHOOK_URL`: 飞书机器人 Webhook 地址

### 钉钉通知

- `DINGTALK_WEBHOOK_URL`: 钉钉机器人 Webhook 地址
- `DINGTALK_SECRET`: 钉钉机器人密钥

### Slack 通知

- `SLACK_WEBHOOK_URL`: Slack Incoming Webhook

---

## 🛠️ 故障排除

### 工作流未触发

```bash
# 检查工作流是否存在
gh workflow list

# 手动触发特定 Agent
gh workflow run 04-agent-payment.yml -f contract_version=v1.0
```

### State Monitor 未检测

```bash
# 手动运行状态监控
gh workflow run 99-state-monitor.yml
```

### 查看失败日志

```bash
# 查看最近失败的运行
gh run list --status failure

# 查看特定运行的日志
gh run view <run-id> --log
```

---

## 📈 进度追踪

进度文件位于 `.github/progress/status.json`，包含：

- 契约版本
- 各 Phase 状态
- 各 Agent 进度
- 开始/完成时间

---

## 📝 手动干预

如果需要手动跳过某个 Agent 或强制触发：

```bash
# 强制标记 Agent 完成 (修改 progress 文件后提交)
echo '{"agent-01": {"status": "success", "progress": 100}}' > .github/progress/override.json
git add .github/progress/override.json
git commit -m "chore: 手动标记 Agent-01 完成"
git push

# 手动触发下游 Agent
gh workflow run 02-agent-gateway.yml -f contract_version=v1.0 -f trigger_source=manual
```
