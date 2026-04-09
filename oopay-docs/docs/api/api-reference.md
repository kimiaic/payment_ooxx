# OOPay API 参考文档

> **API 文档导航**
> - 📄 [商户 API](./api-reference.md) - 商户接入接口（当前文档）
> - 🎛️ [管理后台 API](./admin-api-reference.md) - 运营管理系统接口
> - 📊 [前后端对齐报告](./api-alignment-report.md) - 接口完整性检查

---

## 1. 接口规范

### 1.1 请求格式
- 协议：HTTPS
- 编码：UTF-8
- 格式：JSON
- Content-Type：`application/json`

### 1.2 请求头
| 头字段 | 必填 | 说明 |
|--------|------|------|
| X-Merchant-No | 是 | 商户编号 |
| X-Timestamp | 是 | 时间戳（秒） |
| X-Nonce | 是 | 随机字符串（32位） |
| X-Sign | 是 | 签名（HMAC-SHA256） |

### 1.3 签名算法
```
1. 参数按 key 字典序排序
2. 拼接：key1=value1&key2=value2...
3. 追加：&key={API_SECRET}
4. 计算：HMAC-SHA256(拼接字符串, API_SECRET)
5. 转大写
```

### 1.4 响应格式
```json
{
  "code": 200,
  "message": "SUCCESS",
  "data": {},
  "timestamp": 1712649600,
  "sign": "..."
}
```

**状态码：**
| Code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 签名错误 |
| 403 | 权限不足 |
| 429 | 请求过于频繁 |
| 500 | 系统错误 |

---

## 2. 支付接口

### 2.1 统一下单
```
POST /api/pay/create
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| merchantOrderNo | string | 是 | 商户订单号（64位） |
| amount | long | 是 | 金额（分） |
| currency | string | 否 | 币种，默认 CNY |
| payType | string | 是 | 支付方式 |
| subject | string | 是 | 商品标题 |
| body | string | 否 | 商品描述 |
| notifyUrl | string | 否 | 异步通知地址 |
| returnUrl | string | 否 | 同步跳转地址 |
| attach | string | 否 | 附加数据 |
| expiredTime | int | 否 | 过期时间（分钟），默认30 |

**响应参数：**
| 参数 | 类型 | 说明 |
|------|------|------|
| orderNo | string | 系统订单号 |
| merchantOrderNo | string | 商户订单号 |
| amount | long | 订单金额 |
| currency | string | 币种 |
| status | string | 订单状态 |
| payType | string | 支付方式 |
| payData | object | 支付参数（通道特定） |
| expiredTime | string | 过期时间 |

---

### 2.2 订单查询
```
POST /api/pay/query
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | string | 条件1 | 系统订单号 |
| merchantOrderNo | string | 条件1 | 商户订单号 |

**响应参数：**
| 参数 | 类型 | 说明 |
|------|------|------|
| orderNo | string | 系统订单号 |
| merchantOrderNo | string | 商户订单号 |
| amount | long | 订单金额 |
| status | string | 订单状态 |
| payStatus | int | 支付状态 |
| payTime | string | 支付时间 |
| channelOrderNo | string | 通道订单号 |

---

### 2.3 订单关闭
```
POST /api/pay/close
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | string | 条件1 | 系统订单号 |
| merchantOrderNo | string | 条件1 | 商户订单号 |

---

## 3. 退款接口

### 3.1 申请退款
```
POST /api/refund/create
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | string | 是 | 原订单号 |
| refundNo | string | 是 | 退款单号 |
| amount | long | 是 | 退款金额 |
| reason | string | 否 | 退款原因 |
| notifyUrl | string | 否 | 异步通知地址 |

---

### 3.2 退款查询
```
POST /api/refund/query
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refundNo | string | 是 | 退款单号 |

---

## 4. 账户接口

### 4.1 余额查询
```
POST /api/account/balance
```

**响应参数：**
| 参数 | 类型 | 说明 |
|------|------|------|
| accountNo | string | 账户号 |
| balance | long | 可用余额（分） |
| frozenBalance | long | 冻结余额（分） |
| currency | string | 币种 |

---

### 4.2 流水查询
```
POST /api/account/flow
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startTime | string | 否 | 开始时间 |
| endTime | string | 否 | 结束时间 |
| tradeType | int | 否 | 交易类型 |
| page | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认20 |

---

## 5. 异步通知

### 5.1 支付结果通知
```
POST {notifyUrl}
Content-Type: application/x-www-form-urlencoded
```

**通知参数：**
| 参数 | 类型 | 说明 |
|------|------|------|
| orderNo | string | 系统订单号 |
| merchantOrderNo | string | 商户订单号 |
| amount | long | 订单金额 |
| status | string | 订单状态 |
| payTime | string | 支付时间 |
| channelOrderNo | string | 通道订单号 |
| sign | string | 签名 |

**响应要求：**
- 成功：返回纯文本 `SUCCESS`
- 失败：返回其他内容或 HTTP 非 200，将触发重试

---

## 6. 错误码

| 错误码 | 说明 |
|--------|------|
| SUCCESS | 成功 |
| PARAM_ERROR | 参数错误 |
| SIGN_ERROR | 签名错误 |
| SIGN_EXPIRED | 签名过期 |
| NONCE_USED | Nonce 已使用 |
| IP_NOT_ALLOWED | IP 不在白名单 |
| RATE_LIMIT | 请求过于频繁 |
| ORDER_NOT_EXIST | 订单不存在 |
| ORDER_EXPIRED | 订单已过期 |
| ORDER_PAID | 订单已支付 |
| ORDER_CLOSED | 订单已关闭 |
| INSUFFICIENT_BALANCE | 余额不足 |
| CHANNEL_ERROR | 通道异常 |
| REFUND_FAILED | 退款失败 |
| SYSTEM_ERROR | 系统错误 |

---

*文档版本：v1.0*  
*最后更新：2025-04-09*
