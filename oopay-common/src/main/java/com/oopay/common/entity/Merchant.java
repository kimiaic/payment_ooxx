package com.oopay.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商户实体
 * 存储商户基本信息和认证状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 商户号（唯一标识）
     */
    private String merchantNo;

    /**
     * 商户名称
     */
    private String name;

    /**
     * 商户简称
     */
    private String shortName;

    /**
     * 商户类型 1-个人 2-企业
     */
    private Integer type;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人手机号（加密存储）
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 登录密码（bcrypt加密）
     */
    private String password;

    /**
     * 密钥（用于API签名）
     */
    private String secretKey;

    /**
     * 手续费率（万分之X，如30表示0.30%）
     */
    private Integer feeRate;

    /**
     * 状态 0-待审核 1-已启用 2-已禁用 3-已注销
     */
    private Integer status;

    /**
     * 认证状态 0-未认证 1-认证中 2-已认证 3-认证失败
     */
    private Integer authStatus;

    /**
     * 营业执照号
     */
    private String businessLicenseNo;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 注册地址
     */
    private String registerAddress;

    /**
     * 结算方式 1-T+0 2-T+1 3-T+7
     */
    private Integer settlementType;

    /**
     * 最小结算金额（单位：分）
     */
    private Long minSettlementAmount;

    /**
     * 结算银行账户名
     */
    private String settlementAccountName;

    /**
     * 结算银行账号（加密存储）
     */
    private String settlementAccountNo;

    /**
     * 结算银行编码
     */
    private String settlementBankCode;

    /**
     * 结算银行名称
     */
    private String settlementBankName;

    /**
     * 回调通知URL
     */
    private String notifyUrl;

    /**
     * 跳转URL
     */
    private String returnUrl;

    /**
     * 白名单IP（逗号分隔）
     */
    private String whiteListIp;

    /**
     * 每日限额（单位：分，0表示无限制）
     */
    private Long dailyLimit;

    /**
     * 单笔限额（单位：分，0表示无限制）
     */
    private Long singleLimit;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 备注
     */
    private String remark;
}
