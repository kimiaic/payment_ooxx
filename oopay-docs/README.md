# OOPay 支付系统

> 面向企业的全渠道聚合支付解决方案

## 项目简介

OOPay 是一个基于 xxpay4pro 架构理念、完全自主重写的企业级支付系统。系统在借鉴开源项目设计思路的基础上，针对核心业务场景进行了深度优化，剔除了核销端和代理商系统，专注于打造稳定、安全、可扩展的支付中台。

## 核心特性

- **多通道聚合**：支持银行卡、微信、支付宝、USDT 等多种支付方式
- **智能路由**：基于成功率、费率、响应时间的动态路由引擎
- **金融级安全**：多层防护机制，覆盖签名、加密、防重放、限流等
- **数据一致性**：Saga 事务 + 状态机 + 多级对账，确保资金安全
- **高可用架构**：微服务设计，支持水平扩展和故障转移

## 文档导航

```
docs/
├── prd/                    # 产品需求文档
│   ├── PRD-v1.0.md        # 产品需求主文档
│   ├── feature-list.md    # 功能清单
│   └── feature-fields.md  # 功能字段设计
├── architecture/          # 架构设计
│   └── architecture.md    # 系统架构、时序图、部署方案
├── db/                    # 数据库设计
│   └── db-schema.md       # 完整 DDL + 规范说明
├── security/              # 安全设计
│   └── security-design.md # 安全机制、实施清单、测试 Checklist
├── api/                   # API 文档
│   └── api-reference.md   # 完整接口参考
├── ops/                   # 运营手册
│   └── operations.md      # 日常运维、监控告警、故障处理
└── ui/                    # UI 设计
    └── ui-design.md       # 界面设计规范
```

## 快速开始

1. **了解需求**：从 `docs/prd/PRD-v1.0.md` 开始
2. **查看架构**：阅读 `docs/architecture/architecture.md` 理解系统设计
3. **部署准备**：参考 `docs/db/db-schema.md` 初始化数据库
4. **安全配置**：按 `docs/security/security-design.md` 配置安全策略

## 技术栈

- **后端**：Java / Spring Boot / Spring Cloud
- **数据库**：MySQL 8.0 + Redis 7.x
- **消息队列**：RabbitMQ / RocketMQ
- **监控**：Prometheus + Grafana + ELK

## 版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2025-04 | 初始版本，核心支付功能 |

---
*文档最后更新：2025-04-09*
