-- ============================================================
-- OOPay Flyway Migration V1 - Initial Schema
-- Database: MySQL 8.0+
-- Character Set: utf8mb4
-- ============================================================

-- ============================================================
-- 1. 商户表 (oopay_merchant)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_merchant (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    merchant_no         VARCHAR(32)     NOT NULL COMMENT '商户编号：M + 14 位数字',
    merchant_name       VARCHAR(128)    NOT NULL COMMENT '商户名称',
    merchant_type       TINYINT         NOT NULL DEFAULT 1 COMMENT '商户类型：1-企业，2-个人',

    -- 联系信息（加密存储）
    contact_name        VARCHAR(256)    COMMENT '联系人姓名（加密）',
    contact_phone       VARCHAR(256)    COMMENT '联系人手机号（加密）',
    contact_email       VARCHAR(256)    COMMENT '联系人邮箱',

    -- 密钥配置
    api_key             VARCHAR(256)    NOT NULL COMMENT 'API 密钥（加密）',
    api_secret          VARCHAR(512)    NOT NULL COMMENT 'API 密钥密文（加密）',

    -- 状态配置
    status              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常，2-冻结',
    audit_status        TINYINT         NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-拒绝',

    -- 风控配置
    risk_level          TINYINT         NOT NULL DEFAULT 1 COMMENT '风控等级：1-低，2-中，3-高',
    daily_limit         BIGINT          COMMENT '单日限额（分）',
    single_limit        BIGINT          COMMENT '单笔限额（分）',

    -- 回调配置
    notify_url          VARCHAR(512)    COMMENT '默认异步通知地址',

    -- 扩展字段
    remark              VARCHAR(512)    COMMENT '备注',
    ext_json            JSON            COMMENT '扩展配置 JSON',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    update_by           BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',
    version             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (id),
    UNIQUE KEY uk_merchant_no (merchant_no),
    INDEX idx_status (status),
    INDEX idx_audit_status (audit_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户信息表';

-- ============================================================
-- 2. 商户通道配置表 (oopay_merchant_channel)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_merchant_channel (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    merchant_no         VARCHAR(32)     NOT NULL COMMENT '商户编号',
    channel_id          BIGINT          NOT NULL COMMENT '通道 ID',
    channel_code        VARCHAR(32)     NOT NULL COMMENT '通道编码',
    pay_type            VARCHAR(32)     NOT NULL COMMENT '支付方式：WECHAT/ALIPAY/USDT',

    -- 费率配置
    fee_rate            DECIMAL(5,4)    NOT NULL DEFAULT 0.0000 COMMENT '费率，如 0.0060 = 0.6%',
    fee_type            TINYINT         NOT NULL DEFAULT 1 COMMENT '计费类型：1-百分比，2-固定金额',

    -- 限额配置
    min_amount          BIGINT          COMMENT '最小金额（分）',
    max_amount          BIGINT          COMMENT '最大金额（分）',
    daily_limit         BIGINT          COMMENT '日限额（分）',

    -- 优先级
    priority            INT             NOT NULL DEFAULT 0 COMMENT '优先级，数字越大越优先',

    -- 状态
    status              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',

    -- 扩展配置
    config_json         JSON            COMMENT '通道专属配置',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    update_by           BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',
    version             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (id),
    UNIQUE KEY uk_merchant_channel (merchant_no, channel_id, pay_type, deleted),
    INDEX idx_merchant_no (merchant_no),
    INDEX idx_channel_id (channel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户通道配置表';

-- ============================================================
-- 3. 订单表 (oopay_order)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_order (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    order_no            VARCHAR(32)     NOT NULL COMMENT '系统订单号：O + 14位日期 + 8位随机',
    merchant_no         VARCHAR(32)     NOT NULL COMMENT '商户编号',
    merchant_order_no   VARCHAR(64)     NOT NULL COMMENT '商户订单号',

    -- 支付信息
    pay_type            VARCHAR(32)     NOT NULL COMMENT '支付方式',
    channel_id          BIGINT          COMMENT '通道 ID',
    channel_order_no    VARCHAR(64)     COMMENT '通道订单号',

    -- 金额信息（CNY 使用 BIGINT 分，USDT 使用 DECIMAL）
    amount              BIGINT          NOT NULL COMMENT '订单金额（分）',
    currency            VARCHAR(8)      NOT NULL DEFAULT 'CNY' COMMENT '币种',

    -- USDT 专用字段
    usdt_amount         DECIMAL(20,8)   COMMENT 'USDT 金额',
    usdt_rate           DECIMAL(20,8)   COMMENT '下单时汇率',

    -- 费用信息
    merchant_fee        BIGINT          NOT NULL DEFAULT 0 COMMENT '商户手续费（分）',
    channel_fee         BIGINT          NOT NULL DEFAULT 0 COMMENT '通道手续费（分）',

    -- 状态信息
    status              VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT '订单状态',
    pay_status          TINYINT         NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-部分支付',

    -- 时间戳
    expired_time        DATETIME        COMMENT '过期时间',
    pay_time            DATETIME        COMMENT '支付时间',
    notify_time         DATETIME        COMMENT '通知时间',

    -- 客户端信息
    client_ip           VARCHAR(64)     COMMENT '客户端 IP',
    user_agent          VARCHAR(512)    COMMENT 'User-Agent',
    device_id           VARCHAR(64)     COMMENT '设备 ID',

    -- 商品信息
    subject             VARCHAR(256)    COMMENT '商品标题',
    body                VARCHAR(512)    COMMENT '商品描述',

    -- 回调配置
    notify_url          VARCHAR(512)    COMMENT '异步通知地址',
    return_url          VARCHAR(512)    COMMENT '同步跳转地址',

    -- 扩展字段
    attach              VARCHAR(512)    COMMENT '附加数据（原样返回）',
    ext_json            JSON            COMMENT '扩展字段',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    update_by           BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',
    version             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_merchant_order (merchant_no, merchant_order_no, deleted),
    INDEX idx_merchant_no (merchant_no),
    INDEX idx_channel_id (channel_id),
    INDEX idx_status (status),
    INDEX idx_pay_time (pay_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';

-- ============================================================
-- 4. 订单流水表 (oopay_order_log)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_order_log (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    order_no            VARCHAR(32)     NOT NULL COMMENT '订单号',

    -- 状态变更记录
    from_status         VARCHAR(16)     COMMENT '原状态',
    to_status           VARCHAR(16)     NOT NULL COMMENT '新状态',

    -- 操作信息
    action              VARCHAR(32)     NOT NULL COMMENT '操作类型：CREATE/PAY/CLOSE/REFUND',
    operator            VARCHAR(64)     COMMENT '操作人',
    operator_type       TINYINT         NOT NULL DEFAULT 1 COMMENT '操作人类型：1-系统，2-商户，3-管理员',

    -- 请求/响应记录
    request_data        TEXT            COMMENT '请求数据',
    response_data       TEXT            COMMENT '响应数据',

    -- 扩展信息
    remark              VARCHAR(512)    COMMENT '备注',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',

    PRIMARY KEY (id),
    INDEX idx_order_no (order_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单流水表';

-- ============================================================
-- 5. 账户表 (oopay_account)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_account (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    account_no          VARCHAR(32)     NOT NULL COMMENT '账户号：A + 14位数字',
    merchant_no         VARCHAR(32)     NOT NULL COMMENT '商户编号',
    account_type        TINYINT         NOT NULL DEFAULT 1 COMMENT '账户类型：1-余额账户，2-冻结账户',
    currency            VARCHAR(8)      NOT NULL DEFAULT 'CNY' COMMENT '币种',

    -- 余额信息
    balance             BIGINT          NOT NULL DEFAULT 0 COMMENT '可用余额（分）',
    frozen_balance      BIGINT          NOT NULL DEFAULT 0 COMMENT '冻结余额（分）',

    -- 限额配置
    single_in_limit     BIGINT          COMMENT '单笔入账限额',
    single_out_limit    BIGINT          COMMENT '单笔出账限额',
    daily_in_limit      BIGINT          COMMENT '日入账限额',
    daily_out_limit     BIGINT          COMMENT '日出账限额',

    -- 状态
    status              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0-冻结，1-正常',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    update_by           BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',
    version             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (id),
    UNIQUE KEY uk_account_no (account_no),
    UNIQUE KEY uk_merchant_currency (merchant_no, currency, account_type, deleted),
    INDEX idx_merchant_no (merchant_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资金账户表';

-- ============================================================
-- 6. 账户流水表 (oopay_account_flow)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_account_flow (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    flow_no             VARCHAR(32)     NOT NULL COMMENT '流水号：F + 14位日期 + 8位随机',
    account_no          VARCHAR(32)     NOT NULL COMMENT '账户号',
    merchant_no         VARCHAR(32)     NOT NULL COMMENT '商户编号',

    -- 交易信息
    trade_type          TINYINT         NOT NULL COMMENT '交易类型：1-充值，2-提现，3-支付，4-退款，5-手续费',
    trade_direction     TINYINT         NOT NULL COMMENT '方向：1-收入，2-支出',
    amount              BIGINT          NOT NULL COMMENT '交易金额（分）',
    balance             BIGINT          NOT NULL COMMENT '交易后余额（分）',

    -- 关联信息
    order_no            VARCHAR(32)     COMMENT '关联订单号',
    trade_no            VARCHAR(64)     COMMENT '关联交易号',

    -- 状态
    status              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功',

    -- 扩展信息
    remark              VARCHAR(512)    COMMENT '备注',
    ext_json            JSON            COMMENT '扩展字段',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',

    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_no (flow_no),
    INDEX idx_account_no (account_no),
    INDEX idx_merchant_no (merchant_no),
    INDEX idx_order_no (order_no),
    INDEX idx_create_time (create_time),
    INDEX idx_trade_type (trade_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户流水表';

-- ============================================================
-- 7. 支付通道表 (oopay_channel)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_channel (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    channel_code        VARCHAR(32)     NOT NULL COMMENT '通道编码',
    channel_name        VARCHAR(64)     NOT NULL COMMENT '通道名称',
    channel_type        TINYINT         NOT NULL COMMENT '通道类型：1-微信，2-支付宝，3-银联，4-USDT',
    pay_type            VARCHAR(32)     NOT NULL COMMENT '支付方式',

    -- 配置信息
    config_json         JSON            NOT NULL COMMENT '通道配置参数',

    -- 费率配置
    fee_rate            DECIMAL(5,4)    NOT NULL DEFAULT 0.0000 COMMENT '通道费率',

    -- 限额配置
    min_amount          BIGINT          COMMENT '最小金额（分）',
    max_amount          BIGINT          COMMENT '最大金额（分）',

    -- 状态
    status              TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    health_status       TINYINT         NOT NULL DEFAULT 1 COMMENT '健康状态：0-故障，1-正常，2-降级',

    -- 权重与优先级
    weight              INT             NOT NULL DEFAULT 100 COMMENT '权重',
    priority            INT             NOT NULL DEFAULT 0 COMMENT '优先级',

    -- 统计字段
    total_count         BIGINT          NOT NULL DEFAULT 0 COMMENT '总订单数',
    success_count       BIGINT          NOT NULL DEFAULT 0 COMMENT '成功订单数',
    fail_count          BIGINT          NOT NULL DEFAULT 0 COMMENT '失败订单数',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    update_by           BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',
    version             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_code (channel_code, deleted),
    INDEX idx_channel_type (channel_type),
    INDEX idx_pay_type (pay_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付通道表';

-- ============================================================
-- 8. 回调记录表 (oopay_notify_record)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_notify_record (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    notify_no           VARCHAR(32)     NOT NULL COMMENT '通知编号：N + 14位数字',
    order_no            VARCHAR(32)     NOT NULL COMMENT '订单号',
    merchant_no         VARCHAR(32)     NOT NULL COMMENT '商户编号',

    -- 通知内容
    notify_type         TINYINT         NOT NULL COMMENT '通知类型：1-支付成功，2-退款成功',
    notify_url          VARCHAR(512)    NOT NULL COMMENT '通知地址',
    notify_data         TEXT            NOT NULL COMMENT '通知内容',

    -- 通知结果
    notify_count        INT             NOT NULL DEFAULT 0 COMMENT '通知次数',
    last_notify_time    DATETIME        COMMENT '最后通知时间',
    response_data       TEXT            COMMENT '响应内容',
    response_code       INT             COMMENT 'HTTP 状态码',

    -- 状态
    status              TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0-待通知，1-通知成功，2-通知失败',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',

    PRIMARY KEY (id),
    UNIQUE KEY uk_notify_no (notify_no),
    INDEX idx_order_no (order_no),
    INDEX idx_merchant_no (merchant_no),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回调通知记录表';

-- ============================================================
-- 9. 对账记录表 (oopay_reconcile)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_reconcile (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    reconcile_date      DATE            NOT NULL COMMENT '对账日期',
    channel_id          BIGINT          NOT NULL COMMENT '通道 ID',

    -- 统计信息
    total_orders        INT             NOT NULL DEFAULT 0 COMMENT '系统订单总数',
    total_amount        BIGINT          NOT NULL DEFAULT 0 COMMENT '系统订单总金额（分）',
    channel_orders      INT             NOT NULL DEFAULT 0 COMMENT '通道订单总数',
    channel_amount      BIGINT          NOT NULL DEFAULT 0 COMMENT '通道订单总金额（分）',

    -- 差异统计
    diff_orders         INT             NOT NULL DEFAULT 0 COMMENT '差异订单数',
    diff_amount         BIGINT          NOT NULL DEFAULT 0 COMMENT '差异金额（分）',

    -- 状态
    status              TINYINT         NOT NULL DEFAULT 0 COMMENT '状态：0-未对账，1-对账中，2-对账完成，3-存在差异',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    update_by           BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',

    PRIMARY KEY (id),
    UNIQUE KEY uk_date_channel (reconcile_date, channel_id, deleted),
    INDEX idx_reconcile_date (reconcile_date),
    INDEX idx_channel_id (channel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账记录表';

-- ============================================================
-- 10. 对账差异表 (oopay_reconcile_diff)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_reconcile_diff (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    reconcile_id        BIGINT          NOT NULL COMMENT '对账记录 ID',
    order_no            VARCHAR(32)     NOT NULL COMMENT '订单号',
    channel_id          BIGINT          NOT NULL COMMENT '通道 ID',

    -- 差异类型
    diff_type           TINYINT         NOT NULL COMMENT '差异类型：1-系统有通道无，2-通道有系统无，3-金额不符，4-状态不符',

    -- 系统数据
    sys_amount          BIGINT          COMMENT '系统金额（分）',
    sys_status          VARCHAR(16)     COMMENT '系统状态',

    -- 通道数据
    channel_amount      BIGINT          COMMENT '通道金额（分）',
    channel_status      VARCHAR(16)     COMMENT '通道状态',

    -- 处理状态
    handle_status       TINYINT         NOT NULL DEFAULT 0 COMMENT '处理状态：0-未处理，1-已处理',
    handle_result       VARCHAR(512)    COMMENT '处理结果',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',

    PRIMARY KEY (id),
    INDEX idx_reconcile_id (reconcile_id),
    INDEX idx_order_no (order_no),
    INDEX idx_diff_type (diff_type),
    INDEX idx_handle_status (handle_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账差异明细表';

-- ============================================================
-- 11. 系统配置表 (oopay_config)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_config (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    config_key          VARCHAR(128)    NOT NULL COMMENT '配置键',
    config_value        TEXT            COMMENT '配置值',
    config_type         TINYINT         NOT NULL DEFAULT 1 COMMENT '类型：1-字符串，2-数字，3-JSON，4-布尔',

    -- 描述信息
    description         VARCHAR(512)    COMMENT '配置说明',
    category            VARCHAR(32)     COMMENT '配置分类',

    -- 状态
    is_editable         TINYINT         NOT NULL DEFAULT 1 COMMENT '是否可编辑：0-否，1-是',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by           BIGINT          DEFAULT NULL COMMENT '创建人 ID',
    update_by           BIGINT          DEFAULT NULL COMMENT '更新人 ID',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',
    version             INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key, deleted),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================
-- 12. 操作日志表 (oopay_operation_log)
-- ============================================================
CREATE TABLE IF NOT EXISTS oopay_operation_log (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,

    -- 操作信息
    module              VARCHAR(32)     NOT NULL COMMENT '操作模块',
    action              VARCHAR(32)     NOT NULL COMMENT '操作类型',
    description         VARCHAR(512)    COMMENT '操作描述',

    -- 操作用户
    user_id             BIGINT          COMMENT '操作用户 ID',
    user_name           VARCHAR(64)     COMMENT '操作用户名',
    user_type           TINYINT         COMMENT '用户类型：1-管理员，2-商户',

    -- 请求信息
    request_ip          VARCHAR(64)     COMMENT '请求 IP',
    request_url         VARCHAR(512)    COMMENT '请求 URL',
    request_method      VARCHAR(16)     COMMENT '请求方法',
    request_params      TEXT            COMMENT '请求参数',
    request_time        INT             COMMENT '请求耗时(ms)',

    -- 响应信息
    response_code       INT             COMMENT '响应码',
    response_data       TEXT            COMMENT '响应数据',

    -- 异常信息
    error_msg           TEXT            COMMENT '错误信息',

    -- 标准字段
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '软删除标志：0-正常，1-已删除',

    PRIMARY KEY (id),
    INDEX idx_module (module),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================
-- 复合索引优化
-- ============================================================
-- 订单表复合索引（商户查单场景）
CREATE INDEX idx_merchant_order_time ON oopay_order(merchant_no, create_time, status);

-- 账户流水复合索引
CREATE INDEX idx_account_flow_time ON oopay_account_flow(account_no, create_time, trade_type);

-- 回调记录复合索引
CREATE INDEX idx_notify_status_time ON oopay_notify_record(status, create_time);

-- ============================================================
-- 初始化数据
-- ============================================================

-- 系统配置初始化
INSERT INTO oopay_config (config_key, config_value, config_type, description, category) VALUES
('system.name', 'OOPay', 1, '系统名称', 'system'),
('system.version', '1.0.0', 1, '系统版本', 'system'),
('order.expire.minutes', '30', 2, '订单过期时间（分钟）', 'order'),
('notify.max.retry', '5', 2, '通知最大重试次数', 'notify'),
('notify.retry.interval', '1,2,4,8,16', 1, '通知重试间隔（分钟）', 'notify'),
('reconcile.time', '02:00', 1, '对账执行时间', 'reconcile'),
('usdt.rate.update.interval', '60', 2, 'USDT 汇率更新间隔（秒）', 'exchange'),
('security.sign.algorithm', 'HMAC-SHA256', 1, '签名算法', 'security'),
('security.sign.expire.seconds', '300', 2, '签名过期时间（秒）', 'security'),
('security.ip.whitelist.enabled', 'false', 4, 'IP 白名单开关', 'security'),
('admin.default.username', 'admin', 1, '默认管理员账号', 'admin'),
('admin.default.email', 'admin@oopay.local', 1, '默认管理员邮箱', 'admin');

-- 支付通道初始化
INSERT INTO oopay_channel (channel_code, channel_name, channel_type, pay_type, config_json, fee_rate, min_amount, max_amount, status) VALUES
('WECHAT_NATIVE', '微信支付-扫码', 1, 'WECHAT_NATIVE', '{"mchId": "", "appId": ""}', 0.0060, 100, 5000000, 1),
('WECHAT_JSAPI', '微信支付-公众号', 1, 'WECHAT_JSAPI', '{"mchId": "", "appId": ""}', 0.0060, 100, 5000000, 1),
('ALIPAY_QR', '支付宝-扫码', 2, 'ALIPAY_QR', '{"appId": "", "privateKey": ""}', 0.0060, 100, 5000000, 1),
('ALIPAY_WAP', '支付宝-手机', 2, 'ALIPAY_WAP', '{"appId": "", "privateKey": ""}', 0.0060, 100, 5000000, 1),
('USDT_TRC20', 'USDT-TRC20', 4, 'USDT_TRC20', '{"contract": "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"}', 0.0100, 10000, 1000000000, 1);

-- 默认管理员账号（密码需要 bcrypt 加密后存储，这里为占位）
-- INSERT INTO oopay_admin (username, password, status) VALUES ('admin', '$2a$12$...', 1);

