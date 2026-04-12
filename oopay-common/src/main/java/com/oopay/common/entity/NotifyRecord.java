package com.oopay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知记录
 * 存储异步通知日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notify_record")
public class NotifyRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 通知记录编号
     */
    private String notifyNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 商户号
     */
    private String merchantNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商户订单号
     */
    private String merchantOrderNo;

    /**
     * 通知类型：1-支付通知 2-退款通知 3-结算通知 4-转账通知
     */
    private Integer notifyType;

    /**
     * 通知状态：0-待通知 1-通知成功 2-通知失败 3-通知中
     */
    private Integer status;

    /**
     * 通知URL
     */
    private String notifyUrl;

    /**
     * 通知内容（JSON格式）
     */
    private String notifyBody;

    /**
     * 响应内容
     */
    private String responseBody;

    /**
     * 通知次数
     */
    private Integer notifyCount;

    /**
     * 最大通知次数
     */
    private Integer maxNotifyCount;

    /**
     * 首次通知时间
     */
    private LocalDateTime firstNotifyTime;

    /**
     * 最后通知时间
     */
    private LocalDateTime lastNotifyTime;

    /**
     * 下次通知时间
     */
    private LocalDateTime nextNotifyTime;

    /**
     * 通知间隔策略（秒，逗号分隔，如：15,30,60,300,900）
     */
    private String notifyInterval;

    /**
     * 响应状态码
     */
    private Integer httpStatus;

    /**
     * 响应耗时（毫秒）
     */
    private Integer responseTime;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 备用通知URL
     */
    private String backupNotifyUrl;

    /**
     * 备注
     */
    private String remark;
}
