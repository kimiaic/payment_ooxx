# GitHub Projects 设置指南

> 在 GitHub 仓库中建立可视化的并行进度看板

---

## 步骤 1: 创建 Project

1. 进入 GitHub 仓库主页: `https://github.com/[username]/oopay-docs`
2. 点击顶部导航栏 **Projects** 标签
3. 点击绿色按钮 **New project**
4. 选择 **Board** 视图
5. 项目名称: `OOPay 并行开发看板`
6. 描述: `12 Agent 极致并行开发进度跟踪`

---

## 步骤 2: 设置看板列

创建以下列 (从左到右):

| 列名 | 用途 | 自动化规则 |
|------|------|-----------|
| 📋 Backlog | 待分配任务 | - |
| 🎯 契约定义 | Day 1 接口契约 | - |
| 🏗️ In Progress | 开发中 | PR 打开时自动移动 |
| 👀 Code Review | 代码审查中 | PR 标记 Ready for Review |
| 🧪 Testing | 测试中 | - |
| ✅ Done | 已完成 | PR 合并时自动移动 |
| 🔗 Integration | 集成中 | Mock 替换阶段 |
| 🚀 Released | 已发布 | 集成测试通过 |

---

## 步骤 3: 创建自定义字段

点击 Project 设置 → **Fields** → **New field**

添加以下字段:

### 1. Agent (单选)
```
类型: Single select
选项:
- Agent-01: Infra-Core
- Agent-02: Infra-Gateway
- Agent-03: Merchant
- Agent-04: Payment
- Agent-05: Channel
- Agent-06: Account
- Agent-07: Refund
- Agent-08: Notify
- Agent-09: Reconcile
- Agent-10: Risk
- Agent-11: Admin API
- Agent-12: Frontend
```

### 2. 优先级 (单选)
```
类型: Single select
选项:
- 🔴 P0 - Critical
- 🟡 P1 - High
- 🟢 P2 - Medium
- ⚪ P3 - Low
```

### 3. 进度 (数字)
```
类型: Number
范围: 0-100
单位: %
```

### 4. 开始日期 (日期)
```
类型: Date
```

### 5. 预计完成 (日期)
```
类型: Date
```

### 6. 阻塞项 (文本)
```
类型: Text
```

---

## 步骤 4: 创建 Issues (每个 Agent 一个)

为每个 Agent 创建一个 Issue，格式如下:

### Issue 标题格式
```
[Agent-XX] [任务名称] - [状态]

示例:
[Agent-01] Infra-Core - 🏗️ In Progress
[Agent-03] Merchant Service - 📋 Backlog
```

### Issue 模板

```markdown
## Agent-XX: [任务名称]

### 任务描述
[简要描述该 Agent 的职责]

### 交付物
- [ ] 交付物1
- [ ] 交付物2
- [ ] 交付物3

### 依赖
- 上游: [Agent-XX] 的 [接口名]
- 下游: [Agent-XX] 等待本 Agent

### Mock 策略
[描述如何使用 Mock 解耦依赖]

### 进度追踪
| 日期 | 进度 | 备注 |
|------|------|------|
| 04-11 | 0% | 契约冻结，开始开发 |
| 04-12 | 20% | - |
| 04-13 | 50% | - |
| 04-14 | 80% | - |
| 04-15 | 100% | 完成 |

### 阻塞问题
- [ ] 无阻塞
- [ ] 等待 [Agent-XX]: [问题描述]
```

---

## 步骤 5: 创建视图

### 视图 1: 甘特图 (Gantt)
```
视图名称: 甘特图
布局: Roadmap
分组: Agent
字段: 开始日期, 预计完成, 进度
```

### 视图 2: 按 Agent 分组
```
视图名称: Agent 分组
布局: Board
分组: Agent
排序: 优先级
```

### 视图 3: 燃尽图数据
```
视图名称: 进度追踪
布局: Table
字段: Agent, 进度, 开始日期, 预计完成, 阻塞项
筛选: 进度 < 100
```

---

## 步骤 6: 设置自动化

进入 Project 设置 → **Workflows**

### 自动化 1: PR 自动移动
```
当: Pull request opened
做: 移动 Issue 到 "👀 Code Review"
```

### 自动化 2: PR 合并自动完成
```
当: Pull request merged
做: 移动 Issue 到 "✅ Done"
添加标签: completed
```

### 自动化 3: Issue 分配自动设置 Agent
```
当: Issue assigned to [username]
做: 设置字段 "Agent" = [对应 Agent]
```

---

## 步骤 7: 创建里程碑

进入仓库 → **Issues** → **Milestones** → **New milestone**

| 里程碑 | 截止日期 | 描述 |
|--------|---------|------|
| 🎯 契约冻结 | 2025-04-11 | 所有接口定义完成 |
| ✅ Mock 完成 | 2025-04-12 | Mock 实现可运行 |
| 🏗️ 基础设施 | 2025-04-16 | Infra-Core + Gateway 完成 |
| 💳 核心服务 | 2025-04-21 | Merchant + Payment + Channel 完成 |
| 💰 资金服务 | 2025-04-23 | Account + Refund + Notify 完成 |
| 🔗 首次集成 | 2025-04-26 | 商户→支付→回调 全流程 |
| 🚀 压力测试 | 2025-04-29 | 1000 TPS 压测通过 |

---

## 步骤 8: 每日同步脚本 (可选)

创建一个 GitHub Actions 工作流，自动生成每日进度报告:

```yaml
# .github/workflows/daily-sync.yml
name: Daily Progress Sync

on:
  schedule:
    - cron: '0 9 * * *'  # 每天上午 9 点
  workflow_dispatch:

jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v3
      
      - name: Update Gantt Chart
        run: |
          # 读取 Project 数据，更新 GANTT_CHART.md
          # 提交更新
      
      - name: Post Daily Report
        uses: actions/github-script@v6
        with:
          script: |
            // 生成每日进度报告到 Discussion 或 Issue
```

---

## 看板使用流程

### 晨会 (每日 9:00)
1. 打开 GitHub Project 看板
2. 每个 Agent 汇报:
   - 昨日完成的卡片
   - 今日计划的卡片
   - 是否有卡片被阻塞
3. 更新卡片进度字段

### 开发中
1. 开始任务: 将卡片移动到 "🏗️ In Progress"
2. 提交 PR: 卡片自动移动到 "👀 Code Review"
3. PR 合并: 卡片自动移动到 "✅ Done"

### 集成阶段
1. 当依赖的 Agent 都完成后，卡片移动到 "🔗 Integration"
2. Mock 替换为真实服务
3. 集成测试通过后，卡片移动到 "🚀 Released"

---

## 快速检查清单

- [ ] GitHub Project 已创建
- [ ] 8 个看板列已设置
- [ ] 6 个自定义字段已添加
- [ ] 12 个 Agent Issue 已创建
- [ ] 3 个视图已配置
- [ ] 自动化规则已启用
- [ ] 7 个里程碑已创建
- [ ] 团队成员已邀请

---

## 参考链接

- [GitHub Projects 文档](https://docs.github.com/en/issues/planning-and-tracking-with-projects)
- [Project 自动化](https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project)
- [Mermaid 甘特图语法](https://mermaid.js.org/syntax/gantt.html)

---

*设置完成时间: 预计 30 分钟*
