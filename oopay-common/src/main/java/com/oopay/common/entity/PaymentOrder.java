package com.oopay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单
 * 存储所有支付交易订单信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_order")
public class PaymentOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 系统订单号（唯一标识）
     */
    private String orderNo;

    /**
     * 商户订单号
     */
    private String merchantOrderNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 商户号
     */
    private String merchantNo;

    /**
     * 支付方式：wxpay-微信 alipay-支付宝 usdt-USDT
     */
    private String payType;

    /**
     * 通道编码
     */
    private String channelCode;

    /**
     * 通道订单号
     */
    private String channelOrderNo;

    /**
     * 支付币种 CNY/USDT
     */
    private String currency;

    /**
     * 订单金额（CNY单位：分，USDT单位：元）
     */
    private Long amount;

    /**
     * 订单金额（USDT，精度8位）
     */
    private BigDecimal usdtAmount;

    /**
     * 手续费（CNY单位：分）
     */
    private Long fee;

    /**
     * 实际到账金额（CNY单位：分）
     */
    private Long actualAmount;

    /**
     * 订单标题/商品名称
     */
    private String subject;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 订单状态：0-待支付 1-支付中 2-支付成功 3-支付失败 4-已撤销 5-已退款
     */
    private Integer status;

    /**
     * 通知状态：0-未通知 1-通知成功 2-通知失败
     */
    private Integer notifyStatus;

    /**
     * 通知次数
     */
    private Integer notifyCount;

    /**
     * 最后通知时间
     */
    private LocalDateTime lastNotifyTime;

    /**
     * 用户/买家标识
     */
    private String buyerId;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 订单过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 回调通知URL
     */
    private String notifyUrl;

    /**
     * 跳转URL
     */
    private String returnUrl;

    /**
     * 二维码URL（扫码支付）
     */
    private String qrCodeUrl;

    /**
     * 预支付订单号（微信/支付宝）
     */
    private String prepayId;

    /**
     * 支付链接
     */
    private String payUrl;

    /**
     * USDT钱包地址
     */
    private String walletAddress;

    /**
     * USDT交易哈希
     */
    private String txHash;

    /**
     * 退款金额（CNY单位：分）
     */
    private Long refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 用户扩展数据（回调时原样返回）
     */
    private String extraData;

    /**
     * 请求日志ID
     */
    private Long requestLogId;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 备注
     */
    private String remark;
}
