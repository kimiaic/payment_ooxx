package com.oopay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 账户流水
 * 记录账户资金变动明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("account_transaction")
public class AccountTransaction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流水号（唯一标识）
     */
    private String txNo;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账户号
     */
    private String accountNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 商户号
     */
    private String merchantNo;

    /**
     * 币种 CNY/USDT
     */
    private String currency;

    /**
     * 变动金额（CNY单位：分，正数增加，负数减少）
     */
    private Long amount;

    /**
     * 变动金额（USDT，精度8位，正数增加，负数减少）
     */
    private BigDecimal usdtAmount;

    /**
     * 变动前余额（CNY单位：分）
     */
    private Long beforeBalance;

    /**
     * 变动前余额（USDT，精度8位）
     */
    private BigDecimal usdtBeforeBalance;

    /**
     * 变动后余额（CNY单位：分）
     */
    private Long afterBalance;

    /**
     * 变动后余额（USDT，精度8位）
     */
    private BigDecimal usdtAfterBalance;

    /**
     * 业务类型：1-充值 2-提现 3-支付 4-退款 5-手续费 6-结算 7-调账
     */
    private Integer bizType;

    /**
     * 业务订单号
     */
    private String bizOrderNo;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 交易方向：1-入账 2-出账
     */
    private Integer direction;

    /**
     * 冻结金额变动（CNY单位：分）
     */
    private Long frozenAmount;

    /**
     * 冻结金额变动（USDT，精度8位）
     */
    private BigDecimal usdtFrozenAmount;

    /**
     * 变动前冻结金额（CNY单位：分）
     */
    private Long beforeFrozen;

    /**
     * 变动前冻结金额（USDT，精度8位）
     */
    private BigDecimal usdtBeforeFrozen;

    /**
     * 变动后冻结金额（CNY单位：分）
     */
    private Long afterFrozen;

    /**
     * 变动后冻结金额（USDT，精度8位）
     */
    private BigDecimal usdtAfterFrozen;

    /**
     * 操作人ID（0表示系统）
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 状态 0-处理中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
