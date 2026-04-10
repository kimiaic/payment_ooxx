#!/bin/bash
# 批量生成纯设计阶段工作流

AGENTS=(
  "02:Infra-Gateway:API网关设计、限流、路由"
  "03:Merchant-Service:商户服务设计、接口定义"
  "04:Payment-Core:支付核心设计、订单状态机"
  "05:Channel-Manager:通道管理设计、路由策略"
  "06:Account-Service:账户服务设计、余额管理"
  "07:Refund-Service:退款服务设计、退款流程"
  "08:Notify-Service:通知服务设计、回调机制"
  "09:Reconcile-Service:对账服务设计、日终处理"
  "10:Risk-Service:风控服务设计、规则引擎"
  "11:Admin-Backend:管理后台设计、运营接口"
  "12:Frontend:前端设计、管理界面"
)

for agent in "${AGENTS[@]}"; do
  IFS=':' read -r ID NAME DESC <<< "$agent"
  FILE="${ID}-agent-$(echo $NAME | tr '[:upper:]' '[:lower:]' | tr ' ' '-').yml"
  
  # 提取下游依赖
  DOWNSTREAM=""
  case $ID in
    01) DOWNSTREAM="Agent-02 (Gateway)" ;;
    02) DOWNSTREAM="Agent-03,04,05 (Merchant,Payment,Channel)" ;;
    03) DOWNSTREAM="Agent-11,12 (Admin,Frontend)" ;;
    04) DOWNSTREAM="Agent-06,07,08,10 (Account,Refund,Notify,Risk)" ;;
    05) DOWNSTREAM="Agent-04 (Payment)" ;;
    06) DOWNSTREAM="Agent-07,09 (Refund,Reconcile)" ;;
    07) DOWNSTREAM="Agent-09 (Reconcile)" ;;
    08) DOWNSTREAM="Agent-09 (Reconcile)" ;;
    09) DOWNSTREAM="Agent-11 (Admin)" ;;
    10) DOWNSTREAM="Agent-09 (Reconcile)" ;;
    11) DOWNSTREAM="Agent-12 (Frontend)" ;;
    12) DOWNSTREAM="集成测试" ;;
  esac
  
  cat > "$FILE" << EOF
name: 🤖 Agent-${ID} - ${NAME} (Design Phase)

on:
  workflow_dispatch:
    inputs:
      contract_version:
        description: '契约版本'
        required: true
        default: 'v1.0'
      trigger_source:
        description: '触发来源'
        required: false
        default: 'manual'

env:
  AGENT_ID: "${ID}"
  AGENT_NAME: "${NAME}"
  CONTRACT_VERSION: \${{ github.event.inputs.contract_version }}

jobs:
  analyze:
    runs-on: ubuntu-latest
    outputs:
      start_time: \${{ steps.timestamp.outputs.time }}
    steps:
      - uses: actions/checkout@v4
      - id: timestamp
        run: echo "time=\$(date -Iseconds)" >> \$GITHUB_OUTPUT
      - run: |
          echo "📋 Agent-${ID} (${NAME}) - 需求分析"
          echo "设计内容: ${DESC}"
          echo "✅ 需求分析完成"

  design:
    needs: analyze
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: |
          echo "📝 生成设计文档..."
          mkdir -p designs/agent-${ID}
          cat > designs/agent-${ID}/design.md << 'DESIGN'
          # Agent-${ID} ${NAME} 设计文档
          
          ## 模块职责
          - ${DESC}
          
          ## 设计要点
          1. 接口定义
          2. 数据模型
          3. 流程设计
          4. Mock 实现
          
          ## 生成时间
          $(date)
          DESIGN
          echo "✅ 设计文档完成"

  contract:
    needs: design
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: |
          echo "☕ 生成接口定义..."
          mkdir -p designs/agent-${ID}/interfaces
          echo "// Agent-${ID} 接口定义" > designs/agent-${ID}/interfaces/Service.java
          echo "✅ 接口定义完成"

  mock:
    needs: contract
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: |
          echo "🎭 生成 Mock 实现..."
          mkdir -p designs/agent-${ID}/mocks
          echo "// Agent-${ID} Mock 实现" > designs/agent-${ID}/mocks/MockService.java
          echo "✅ Mock 实现完成"

  submit:
    needs: [analyze, design, contract, mock]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          token: \${{ secrets.GITHUB_TOKEN }}
      - run: |
          git config user.name "GitHub Actions"
          git config user.email "actions@github.com"
          mkdir -p designs/agent-${ID}/{interfaces,mocks}
          echo "# Agent-${ID} Design" > designs/agent-${ID}/design.md
          git add designs/
          git commit -m "design(${ID}): ${NAME} - \$CONTRACT_VERSION" || true
          git push origin HEAD:design/agent-${ID}-\$CONTRACT_VERSION || true

  finalize:
    needs: [analyze, design, contract, mock, submit]
    runs-on: ubuntu-latest
    if: always()
    steps:
      - run: |
          echo "✅ Agent-${ID} (${NAME}) 设计阶段完成"
          echo "📊 状态: \${{ needs.submit.result }}"
EOF

  echo "✅ 生成: $FILE"
done

echo "✅ 所有工作流生成完成"
