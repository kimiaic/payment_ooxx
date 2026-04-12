package com.oopay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户通道配置
 * 配置商户可用的支付通道及费率
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_channel")
public class MerchantChannel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 商户号
     */
    private String merchantNo;

    /**
     * 通道编码
     */
    private String channelCode;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 支付方式：wxpay-微信 alipay-支付宝 usdt-USDT
     */
    private String payType;

    /**
     * 通道类型：native-扫码 jsapi-公众号/小程序 h5-H5支付 app-APP支付
     */
    private String channelType;

    /**
     * 商户在该通道的商户号
     */
    private String channelMerchantNo;

    /**
     * 商户费率（万分之X）
     */
    private Integer feeRate;

    /**
     * 固定手续费（单位：分）
     */
    private Long fixedFee;

    /**
     * 权重（用于路由选择，数值越大优先级越高）
     */
    private Integer weight;

    /**
     * 每日限额（单位：分，0表示无限制）
     */
    private Long dailyLimit;

    /**
     * 单笔限额（单位：分，0表示无限制）
     */
    private Long singleLimit;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 配置JSON（存储通道特有配置）
     */
    private String configJson;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
