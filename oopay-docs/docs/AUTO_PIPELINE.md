# OOPay 自动化流水线开发方案

> 节点完成后自动触发下一个任务，零人工干预的 CI/CD 式开发流水线

---

## 1. 核心思想

```
传统开发: 人工分配 → 人工检查 → 人工触发下一个
自动化开发: 代码提交 → 自动检测 → 自动触发下游

就像 CI/CD 流水线：
Commit → Build → Test → Deploy

OOPay 开发流水线：
契约冻结 → Agent-01 → Agent-02 → ... → 集成测试 → 压力测试
```

---

## 2. 自动化架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         GitHub Actions 自动化引擎                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐                                                            │
│  │  Contract   │──► 触发所有并行 Agent                                      │
│  │   Freeze    │    (Agent-01~12 同时收到 webhook)                          │
│  └──────┬──────┘                                                            │
│         │                                                                   │
│         ├──►┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│         │   │  Agent-01   │  │  Agent-03   │  │  Agent-04   │              │
│         │   │   Infra     │  │   Merchant  │  │   Payment   │              │
│         │   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │          │                 │                 │                    │
│         │          │  完成后自动触发  │                 │                    │
│         │          ▼                 ▼                 ▼                    │
│         │   ┌─────────────────────────────────────────────────────┐        │
│         │   │              状态检测服务 (State Monitor)             │        │
│         │   │  • 检测所有 Phase 1 Agent 是否完成                   │        │
│         │   │  • 触发 Phase 2 集成流水线                           │        │
│         │   └──────────────────────┬──────────────────────────────┘        │
│         │                          │                                        │
│         │                          ▼                                        │
│         │                   ┌─────────────┐                                 │
│         │                   │  集成测试    │                                 │
│         │                   │ Integration │                                 │
│         │                   └──────┬──────┘                                 │
│         │                          │                                        │
│         │                          ▼                                        │
│         │                   ┌─────────────┐                                 │
│         │                   │  压力测试    │                                 │
│         │                   │ Load Test   │                                 │
│         │                   └──────┬──────┘                                 │
│         │                          │                                        │
│         │                          ▼                                        │
│         │                   ┌─────────────┐                                 │
│         │                   │  发布完成    │                                 │
│         │                   │   Released  │                                 │
│         │                   └─────────────┘                                 │
│         │                                                                   │
│  ┌──────┴──────────────────────────────────────────────────────────────┐   │
│  │                     状态看板同步 (Projects API)                      │   │
│  │  • 自动更新 Issue 状态                                               │   │
│  │  • 自动更新进度字段                                                  │   │
│  │  • 自动发送通知 (飞书/钉钉/邮件)                                     │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. GitHub Actions 工作流设计

### 3.1 工作流结构

```yaml
# .github/workflows/
├── 00-contract-freeze.yml      # 契约冻结触发器
├── 01-agent-infra.yml          # Agent-01: 基础设施
├── 02-agent-gateway.yml        # Agent-02: 网关
├── 03-agent-merchant.yml       # Agent-03: 商户服务
├── 04-agent-payment.yml        # Agent-04: 支付核心
├── 05-agent-channel.yml        # Agent-05: 通道管理
├── 06-agent-account.yml        # Agent-06: 账户资金
├── 07-agent-refund.yml         # Agent-07: 退款服务
├── 08-agent-notify.yml         # Agent-08: 通知服务
├── 09-agent-reconcile.yml      # Agent-09: 对账中心
├── 10-agent-risk.yml           # Agent-10: 风控管理
├── 11-agent-admin.yml          # Agent-11: 管理后台
├── 12-agent-frontend.yml       # Agent-12: 前端
├── 20-integration-test.yml     # 集成测试
├── 21-load-test.yml            # 压力测试
└── 99-state-monitor.yml        # 状态监控器
```

### 3.2 契约冻结触发器 (00-contract-freeze.yml)

```yaml
name: 🎯 Phase 0 - Contract Freeze

on:
  workflow_dispatch:  # 手动触发契约冻结
    inputs:
      contract_version:
        description: '契约版本号'
        required: true
        default: 'v1.0'

jobs:
  freeze-contract:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Validate Contract
        run: |
          echo "🔒 冻结接口契约版本: ${{ github.event.inputs.contract_version }}"
          # 验证契约文档完整性
          test -f docs/api/api-reference.md
          test -f docs/api/admin-api-reference.md
          echo "✅ 契约文档验证通过"
      
      - name: Create Contract Tag
        run: |
          git tag "contract-${{ github.event.inputs.contract_version }}"
          git push origin "contract-${{ github.event.inputs.contract_version }}"
      
      - name: Trigger All Agents
        run: |
          # 并行触发所有 12 个 Agent
          for agent in 01 02 03 04 05 06 07 08 09 10 11 12; do
            echo "🚀 触发 Agent-$agent"
            curl -X POST \
              -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
              -H "Accept: application/vnd.github.v3+json" \
              https://api.github.com/repos/${{ github.repository }}/actions/workflows/0${agent}-agent-*.yml/dispatches \
              -d '{"ref":"main","inputs":{"contract_version":"${{ github.event.inputs.contract_version }}"}}'
          done
      
      - name: Update Project Status
        run: |
          # 更新 GitHub Projects 看板
          echo "📊 更新看板状态: Contract Frozen"
```

### 3.3 单个 Agent 工作流模板 (XX-agent-xxx.yml)

```yaml
name: 🤖 Agent-XX - [Agent名称]

on:
  workflow_dispatch:  # 由上游触发
    inputs:
      contract_version:
        description: '契约版本'
        required: true
      trigger_source:
        description: '触发来源'
        required: false
        default: 'contract-freeze'
  
  # 可选：PR 合并后自动触发下游
  # push:
  #   branches: [main]
  #   paths: ['oopay/oopay-*/**']

jobs:
  # ============ 阶段 1: 环境准备 ============
  setup:
    runs-on: ubuntu-latest
    outputs:
      agent_id: "XX"
      agent_name: "[Agent名称]"
      start_time: ${{ steps.time.outputs.time }}
    steps:
      - name: Checkout
        uses: actions/checkout@v4
        with:
          ref: contract-${{ github.event.inputs.contract_version }}
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Record Start Time
        id: time
        run: echo "time=$(date -Iseconds)" >> $GITHUB_OUTPUT
      
      - name: Update Project - In Progress
        run: |
          # 调用 GitHub Projects API 更新状态
          echo "🏗️ Agent-XX 开始开发"

  # ============ 阶段 2: 代码生成/开发 ============
  develop:
    needs: setup
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Run Agent Task
        run: |
          echo "🤖 Agent-${{ needs.setup.outputs.agent_id }} 开始执行任务"
          echo "任务: 生成 ${{ needs.setup.outputs.agent_name }} 代码"
          
          # 这里调用实际的 Agent 开发逻辑
          # 可以是调用外部 API、运行脚本等
          
          echo "✅ 开发完成"

  # ============ 阶段 3: 代码质量检查 ============
  quality-check:
    needs: develop
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Compile
        run: |
          cd oopay
          mvn clean compile -pl oopay-[模块名] -am
      
      - name: Unit Tests
        run: |
          cd oopay
          mvn test -pl oopay-[模块名]
      
      - name: Code Coverage
        run: |
          echo "📊 代码覆盖率检查"
          # 可以集成 JaCoCo
      
      - name: SonarQube Scan
        run: |
          echo "🔍 静态代码分析"
          # 可以集成 SonarQube

  # ============ 阶段 4: 契约测试 ============
  contract-test:
    needs: quality-check
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Start Mock Services
        run: |
          echo "🎭 启动 Mock 服务"
          # 启动依赖的 Mock 服务
      
      - name: Contract Tests
        run: |
          echo "🧪 执行契约测试"
          # 验证实现是否符合契约
          # 使用 Spring Cloud Contract 或 Pact
      
      - name: Stop Mock Services
        run: |
          echo "🛑 停止 Mock 服务"

  # ============ 阶段 5: 集成到主分支 ============
  integrate:
    needs: contract-test
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Create Integration PR
        run: |
          echo "📤 创建集成 PR"
          # 自动创建 PR 合并到 main
      
      - name: Auto Merge
        run: |
          echo "✅ 自动合并 PR"
          # 测试通过后自动合并

  # ============ 阶段 6: 触发下游 ============
  trigger-downstream:
    needs: integrate
    runs-on: ubuntu-latest
    if: success()
    strategy:
      matrix:
        downstream: ["YY", "ZZ"]  # 下游 Agent 列表
    steps:
      - name: Trigger Downstream Agent
        run: |
          echo "🚀 触发下游 Agent-${{ matrix.downstream }}"
          curl -X POST \
            -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
            -H "Accept: application/vnd.github.v3+json" \
            https://api.github.com/repos/${{ github.repository }}/actions/workflows/0${{ matrix.downstream }}-agent-*.yml/dispatches \
            -d '{"ref":"main","inputs":{"contract_version":"${{ github.event.inputs.contract_version }}","trigger_source":"agent-XX"}}'

  # ============ 阶段 7: 更新状态看板 ============
  update-status:
    needs: [setup, develop, quality-check, contract-test, integrate]
    runs-on: ubuntu-latest
    if: always()
    steps:
      - name: Calculate Progress
        run: |
          # 根据 job 结果计算进度
          if [ "${{ needs.integrate.result }}" == "success" ]; then
            PROGRESS=100
            STATUS="✅ Done"
          elif [ "${{ needs.contract-test.result }}" == "success" ]; then
            PROGRESS=80
            STATUS="🧪 Testing"
          elif [ "${{ needs.quality-check.result }}" == "success" ]; then
            PROGRESS=60
            STATUS="👀 Code Review"
          elif [ "${{ needs.develop.result }}" == "success" ]; then
            PROGRESS=40
            STATUS="🏗️ Developing"
          else
            PROGRESS=20
            STATUS="📋 Backlog"
          fi
          echo "PROGRESS=$PROGRESS" >> $GITHUB_ENV
          echo "STATUS=$STATUS" >> $GITHUB_ENV
      
      - name: Update GitHub Project
        run: |
          echo "📊 更新 GitHub Projects: Agent-XX"
          echo "进度: ${{ env.PROGRESS }}%"
          echo "状态: ${{ env.STATUS }}"
          # 调用 GitHub Projects GraphQL API
      
      - name: Send Notification
        run: |
          if [ "${{ needs.integrate.result }}" == "success" ]; then
            echo "📢 发送完成通知"
            # 可以发送到飞书/钉钉/Slack
          fi
```

---

## 4. 状态监控器 (State Monitor)

### 4.1 状态流转规则

```yaml
# .github/workflows/99-state-monitor.yml
name: 📊 State Monitor

on:
  schedule:
    - cron: '*/5 * * * *'  # 每 5 分钟检查一次
  workflow_dispatch:

jobs:
  monitor:
    runs-on: ubuntu-latest
    steps:
      - name: Check All Agent Status
        run: |
          echo "🔍 检查所有 Agent 状态"
          
          # 获取所有工作流运行状态
          AGENTS=("01" "02" "03" "04" "05" "06" "07" "08" "09" "10" "11" "12")
          
          COMPLETED=0
          for agent in "${AGENTS[@]}"; do
            STATUS=$(curl -s \
              -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
              -H "Accept: application/vnd.github.v3+json" \
              https://api.github.com/repos/${{ github.repository }}/actions/workflows | \
              jq -r '.workflows[] | select(.name | contains("Agent-'$agent'")) | .state')
            
            if [ "$STATUS" == "active" ]; then
              echo "Agent-$agent: ✅ 完成"
              ((COMPLETED++))
            else
              echo "Agent-$agent: 🏗️ 进行中"
            fi
          done
          
          echo "完成进度: $COMPLETED/12"
          
          # 如果所有 Agent 完成，触发集成测试
          if [ $COMPLETED -eq 12 ]; then
            echo "🎉 所有 Agent 完成，触发集成测试"
            curl -X POST \
              -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
              -H "Accept: application/vnd.github.v3+json" \
              https://api.github.com/repos/${{ github.repository }}/actions/workflows/20-integration-test.yml/dispatches \
              -d '{"ref":"main"}'
          fi
```

### 4.2 阶段门控 (Phase Gate)

```yaml
name: 🚪 Phase Gate Controller

on:
  workflow_run:
    workflows: 
      - "🤖 Agent-01 - Infra"
      - "🤖 Agent-03 - Merchant"
      - "🤖 Agent-04 - Payment"
      - "🤖 Agent-05 - Channel"
    types:
      - completed

jobs:
  check-phase-completion:
    runs-on: ubuntu-latest
    steps:
      - name: Check Phase 1 Completion
        run: |
          # 检查 Phase 1 的所有 Agent 是否都成功
          REQUIRED_AGENTS=("01" "03" "04" "05")
          ALL_SUCCESS=true
          
          for agent in "${REQUIRED_AGENTS[@]}"; do
            # 查询该 Agent 的最新运行状态
            RESULT=$(curl -s \
              -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
              https://api.github.com/repos/${{ github.repository }}/actions/runs | \
              jq -r '.workflow_runs[] | select(.name | contains("Agent-'$agent'")) | .conclusion' | head -1)
            
            if [ "$RESULT" != "success" ]; then
              ALL_SUCCESS=false
              echo "Agent-$agent: 未完成"
            fi
          done
          
          if [ "$ALL_SUCCESS" = true ]; then
            echo "🎉 Phase 1 完成，触发 Phase 2"
            # 触发 Phase 2 Agent (06, 07, 08)
            for agent in "06" "07" "08"; do
              curl -X POST \
                -H "Authorization: token ${{ secrets.GITHUB_TOKEN }}" \
                https://api.github.com/repos/${{ github.repository }}/actions/workflows/0${agent}-agent-*.yml/dispatches \
                -d '{"ref":"main"}'
            done
          fi
```

---

## 5. 依赖关系配置

### 5.1 依赖矩阵

```yaml
# .github/config/agent-dependencies.yml
agents:
  agent-01:
    name: "Infra-Core"
    dependencies: []
    downstream: ["agent-02"]
    phase: 0
    
  agent-02:
    name: "Infra-Gateway"
    dependencies: ["agent-01"]
    downstream: []
    phase: 0
    
  agent-03:
    name: "Merchant"
    dependencies: []
    downstream: []
    phase: 1
    
  agent-04:
    name: "Payment"
    dependencies: []
    downstream: ["agent-06", "agent-07", "agent-08", "agent-10"]
    phase: 1
    
  agent-05:
    name: "Channel"
    dependencies: []
    downstream: []
    phase: 1
    
  agent-06:
    name: "Account"
    dependencies: ["agent-04"]
    downstream: ["agent-07", "agent-09"]
    phase: 2
    
  agent-07:
    name: "Refund"
    dependencies: ["agent-04", "agent-06"]
    downstream: ["agent-09"]
    phase: 2
    
  agent-08:
    name: "Notify"
    dependencies: ["agent-04"]
    downstream: []
    phase: 2
    
  agent-09:
    name: "Reconcile"
    dependencies: ["agent-04", "agent-06", "agent-07"]
    downstream: []
    phase: 3
    
  agent-10:
    name: "Risk"
    dependencies: ["agent-04"]
    downstream: []
    phase: 3
    
  agent-11:
    name: "Admin"
    dependencies: ["agent-03", "agent-04", "agent-06", "agent-07"]
    downstream: []
    phase: 4
    
  agent-12:
    name: "Frontend"
    dependencies: ["agent-03", "agent-11"]
    downstream: []
    phase: 4

phases:
  phase-0:
    name: "基础设施"
    agents: ["agent-01", "agent-02"]
    auto-trigger: false  # 需要手动触发契约冻结
    
  phase-1:
    name: "核心服务"
    agents: ["agent-03", "agent-04", "agent-05"]
    auto-trigger: true   # 契约冻结后自动触发
    
  phase-2:
    name: "资金服务"
    agents: ["agent-06", "agent-07", "agent-08"]
    auto-trigger: true   # Phase 1 完成后自动触发
    
  phase-3:
    name: "运营服务"
    agents: ["agent-09", "agent-10"]
    auto-trigger: true   # Phase 2 完成后自动触发
    
  phase-4:
    name: "管理后台"
    agents: ["agent-11", "agent-12"]
    auto-trigger: true   # Phase 3 完成后自动触发
    
  phase-5:
    name: "集成测试"
    agents: ["integration-test"]
    auto-trigger: true   # 所有 Agent 完成后自动触发
    
  phase-6:
    name: "压力测试"
    agents: ["load-test"]
    auto-trigger: true   # 集成测试通过后自动触发
```

---

## 6. 通知系统

### 6.1 飞书/钉钉通知

```yaml
name: 📢 Notification Service

on:
  workflow_run:
    workflows: ["🤖 Agent-*"]
    types: [completed]

jobs:
  notify:
    runs-on: ubuntu-latest
    steps:
      - name: Prepare Message
        run: |
          WORKFLOW_NAME="${{ github.event.workflow_run.name }}"
          CONCLUSION="${{ github.event.workflow_run.conclusion }}"
          
          if [ "$CONCLUSION" == "success" ]; then
            STATUS="✅ 成功"
            COLOR="green"
          else
            STATUS="❌ 失败"
            COLOR="red"
          fi
          
          cat > message.json << EOF
          {
            "msg_type": "interactive",
            "card": {
              "config": {"wide_screen_mode": true},
              "header": {
                "title": {"tag": "plain_text", "content": "OOPay 开发进度"},
                "template": "$COLOR"
              },
              "elements": [
                {
                  "tag": "div",
                  "text": {
                    "tag": "lark_md",
                    "content": "**$WORKFLOW_NAME**\n状态: $STATUS\n时间: $(date '+%Y-%m-%d %H:%M:%S')"
                  }
                }
              ]
            }
          }
          EOF
      
      - name: Send to Feishu
        run: |
          curl -X POST \
            -H "Content-Type: application/json" \
            -d @message.json \
            "${{ secrets.FEISHU_WEBHOOK_URL }}"
```

---

## 7. 甘特图自动更新

### 7.1 自动更新 GANTT_CHART.md

```yaml
name: 📊 Update Gantt Chart

on:
  schedule:
    - cron: '0 * * * *'  # 每小时更新
  workflow_run:
    workflows: ["🤖 Agent-*"]
    types: [completed]

jobs:
  update-gantt:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Fetch Progress Data
        run: |
          # 获取所有 Agent 的进度
          echo "{
            \"agent-01\": {\"progress\": 100, \"status\": \"done\"},
            \"agent-02\": {\"progress\": 80, \"status\": "in_progress\"},
            ...
          }" > progress.json
      
      - name: Generate Mermaid
        run: |
          # 根据进度数据生成 Mermaid 甘特图
          cat > docs/GANTT_CHART_AUTO.md << 'EOF'
          # OOPay 自动更新甘特图
          
          \`\`\`mermaid
          gantt
              title OOPay 开发进度 (自动更新)
              dateFormat  YYYY-MM-DD
              
              section Phase 0
              Agent-01 Infra :done, a1, 2025-04-11, 3d
              Agent-02 Gateway :active, a2, after a1, 2d
              
              section Phase 1
              Agent-03 Merchant :done, a3, 2025-04-11, 5d
              Agent-04 Payment :active, a4, 2025-04-11, 5d
              ...
          \`\`\`
          EOF
      
      - name: Commit and Push
        run: |
          git config user.name "GitHub Actions"
          git config user.email "actions@github.com"
          git add docs/GANTT_CHART_AUTO.md
          git commit -m "docs: 自动更新甘特图 [skip ci]"
          git push
```

---

## 8. 执行流程

### 8.1 启动命令

```bash
# 1. 手动触发契约冻结 (开始整个流水线)
gh workflow run 00-contract-freeze.yml -f contract_version=v1.0

# 2. 查看所有工作流状态
gh run list --workflow="🤖 Agent-*"

# 3. 查看特定 Agent 状态
gh run list --workflow="🤖 Agent-04 - Payment"

# 4. 手动重试失败的 Agent
gh workflow run 04-agent-payment.yml -f contract_version=v1.0
```

### 8.2 自动化流程图

```
用户触发: gh workflow run 00-contract-freeze.yml
                │
                ▼
┌───────────────────────────────┐
│     契约冻结 + Tag 创建        │
└───────────────┬───────────────┘
                │
                ├──► 触发 Agent-01 ──► 完成后自动触发 Agent-02
                │
                ├──► 触发 Agent-03 ──► 完成后标记完成
                │
                ├──► 触发 Agent-04 ──► 完成后自动触发 Agent-06,07,08,10
                │
                └──► 触发 Agent-05 ──► 完成后标记完成
                │
                ▼ (State Monitor 每5分钟检查)
┌───────────────────────────────┐
│   检测 Phase 1 是否全部完成    │
└───────────────┬───────────────┘
                │ 是
                ▼
        自动触发 Phase 2
                │
                ├──► Agent-06 ──► 完成后触发 Agent-07,09
                ├──► Agent-07 ──► 完成后触发 Agent-09
                └──► Agent-08 ──► 完成后标记完成
                │
                ▼ (State Monitor 检查)
┌───────────────────────────────┐
│   检测 Phase 2 是否全部完成    │
└───────────────┬───────────────┘
                │ 是
                ▼
        自动触发 Phase 3
                │
                ├──► Agent-09
                └──► Agent-10
                │
                ▼
        ... (继续 Phase 4, 5)
                │
                ▼
┌───────────────────────────────┐
│     所有 Agent 完成           │
└───────────────┬───────────────┘
                │
                ▼
        自动触发集成测试
                │
                ▼
        自动触发压力测试
                │
                ▼
        🎉 发布完成
```

---

## 9. 监控 Dashboard

### 9.1 实时状态页

```html
<!-- 可以部署为 GitHub Pages -->
<h1>OOPay 开发流水线状态</h1>

<div id="status-board">
  <div class="phase" data-phase="0">
    <h3>Phase 0: 基础设施</h3>
    <div class="agent" data-agent="01">Agent-01: <span class="status">✅ Done</span></div>
    <div class="agent" data-agent="02">Agent-02: <span class="status">🏗️ In Progress</span></div>
  </div>
  
  <div class="phase" data-phase="1">
    <h3>Phase 1: 核心服务</h3>
    <div class="agent" data-agent="03">Agent-03: <span class="status">⏳ Waiting</span></div>
    <div class="agent" data-agent="04">Agent-04: <span class="status">⏳ Waiting</span></div>
    <div class="agent" data-agent="05">Agent-05: <span class="status">⏳ Waiting</span></div>
  </div>
</div>

<script>
  // 每 30 秒刷新状态
  setInterval(async () => {
    const response = await fetch('/api/status');
    const data = await response.json();
    updateBoard(data);
  }, 30000);
</script>
```

---

## 10. 总结

| 特性 | 手动方案 | 自动化方案 |
|------|---------|-----------|
| 触发方式 | 人工分配 | 自动触发 |
| 状态同步 | 手动更新 | 自动更新 |
| 下游通知 | 人工通知 | Webhook 自动触发 |
| 看板更新 | 手动拖动 | API 自动更新 |
| 失败重试 | 人工发现 | 自动重试/报警 |
| 进度追踪 | 人工统计 | 实时 Dashboard |

**启动命令**:
```bash
gh workflow run 00-contract-freeze.yml -f contract_version=v1.0
```

然后就可以去睡觉了，早上来看结果。🌙
