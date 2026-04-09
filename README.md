# OOPay 支付系统

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](./CHANGELOG.md)
[![Status](https://img.shields.io/badge/status-design-green.svg)]()
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)]()

> 面向企业的全渠道聚合支付解决方案

## 项目简介

OOPay 是一个基于 xxpay4pro 架构理念、完全自主重写的企业级支付系统。系统在借鉴开源项目设计思路的基础上，针对核心业务场景进行了深度优化，**剔除了核销端和代理商系统**，专注于打造稳定、安全、可扩展的支付中台。

## 核心特性

- **多通道聚合**：支持微信、支付宝、USDT 等多种支付方式
- **智能路由**：基于成功率、费率、响应时间的动态路由引擎
- **金融级安全**：多层防护机制，覆盖签名、加密、防重放、限流等
- **数据一致性**：Saga 事务 + 状态机 + 多级对账，确保资金安全
- **高可用架构**：微服务设计，支持水平扩展和故障转移

## 文档导航

```
oopay-docs/
├── README.md                    # 本文档 - 项目总览
├── docs/
│   ├── prd/                     # 产品需求文档
│   │   ├── PRD-v1.0.md         # 产品需求主文档
│   │   ├── feature-list.md     # 功能清单 (58个功能)
│   │   └── feature-fields.md   # 功能字段设计
│   ├── architecture/            # 架构设计
│   │   └── architecture.md     # 系统架构、时序图、部署方案
│   ├── db/                      # 数据库设计
│   │   └── db-schema.md        # 12张表 DDL + 规范
│   ├── security/                # 安全设计
│   │   └── security-design.md  # 安全机制 + P0/P1/P2清单
│   ├── api/                     # API 文档
│   │   └── api-reference.md    # 完整接口参考
│   ├── ops/                     # 运营手册
│   │   └── operations.md       # 运维、监控、故障处理
│   └── ui/                      # UI 设计
│       └── ui-design.md        # 界面原型和规范
├── CONTRIBUTING.md              # 开发规范
└── CHANGELOG.md                 # 版本记录
```

## 快速开始

### 1. 了解需求
```bash
# 产品需求
open oopay-docs/docs/prd/PRD-v1.0.md

# 功能清单
open oopay-docs/docs/prd/feature-list.md
```

### 2. 查看架构
```bash
# 系统架构、时序图、部署方案
open oopay-docs/docs/architecture/architecture.md
```

### 3. 开发准备
```bash
# 数据库初始化
open oopay-docs/docs/db/db-schema.md

# 安全配置
open oopay-docs/docs/security/security-design.md

# 开发规范
open oopay-docs/CONTRIBUTING.md
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21 / Spring Boot 3.x / Spring Cloud |
| 数据库 | MySQL 8.0 / Redis 7.x |
| 消息队列 | RabbitMQ |
| 监控 | Prometheus + Grafana + ELK |
| 网关 | Spring Cloud Gateway |

## 版本记录

详见 [CHANGELOG.md](./CHANGELOG.md)

| 版本 | 日期 | 状态 | 说明 |
|------|------|------|------|
| v1.0 | 2025-04 | 设计中 | 核心支付功能设计完成 |

## 贡献指南

详见 [CONTRIBUTING.md](./CONTRIBUTING.md)

## 许可证

MIT License

---
*最后更新：2025-04-09*
