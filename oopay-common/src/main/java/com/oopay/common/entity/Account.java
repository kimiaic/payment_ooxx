package com.oopay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 账户资金
 * 存储商户账户余额和冻结金额
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("account")
public class Account extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 账户号（唯一标识）
     */
    private String accountNo;

    /**
     * 账户类型：1-商户账户 2-平台账户 3-代理账户
     */
    private Integer accountType;

    /**
     * 商户ID（账户类型为商户时必填）
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
     * 可用余额（CNY单位：分）
     */
    private Long availableBalance;

    /**
     * 可用余额（USDT，精度8位）
     */
    private BigDecimal usdtAvailableBalance;

    /**
     * 冻结金额（CNY单位：分）
     */
    private Long frozenBalance;

    /**
     * 冻结金额（USDT，精度8位）
     */
    private BigDecimal usdtFrozenBalance;

    /**
     * 累计入账金额（CNY单位：分）
     */
    private Long totalInAmount;

    /**
     * 累计入账金额（USDT，精度8位）
     */
    private BigDecimal usdtTotalInAmount;

    /**
     * 累计出账金额（CNY单位：分）
     */
    private Long totalOutAmount;

    /**
     * 累计出账金额（USDT，精度8位）
     */
    private BigDecimal usdtTotalOutAmount;

    /**
     * 状态 0-冻结 1-正常
     */
    private Integer status;

    /**
     * 最后变动时间
     */
    private Long lastTxId;

    /**
     * 备注
     */
    private String remark;
}
