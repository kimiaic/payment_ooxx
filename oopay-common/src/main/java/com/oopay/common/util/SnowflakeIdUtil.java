package com.oopay.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 雪花ID生成器
 * 分布式唯一ID生成工具
 */
public class SnowflakeIdUtil {

    /**
     * 开始时间戳（2024-01-01）
     */
    private static final long START_TIMESTAMP = 1704067200000L;

    /**
     * 序列号占用位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID占用位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心ID占用位数
     */
    private static final long DATA_CENTER_ID_BITS = 5L;

    /**
     * 最大序列号
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 最大机器ID
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 最大数据中心ID
     */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 数据中心ID左移位数
     */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 机器ID左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    private final long workerId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static SnowflakeIdUtil instance;

    static {
        long workerId = getDefaultWorkerId();
        long dataCenterId = 0L;
        instance = new SnowflakeIdUtil(workerId, dataCenterId);
    }

    public SnowflakeIdUtil(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID 超出范围");
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("DataCenter ID 超出范围");
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 生成唯一ID
     */
    public synchronized long nextId() {
        long timestamp = currentTime();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 获取下一个时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTime();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     */
    private long currentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 获取默认机器ID（基于IP地址）
     */
    private static long getDefaultWorkerId() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            int workerId = ipAddress.hashCode() & 0x1F;
            return Math.abs(workerId);
        } catch (UnknownHostException e) {
            return 0L;
        }
    }

    /**
     * 获取单例实例
     */
    public static SnowflakeIdUtil getInstance() {
        return instance;
    }

    /**
     * 生成ID（静态方法）
     */
    public static long generateId() {
        return instance.nextId();
    }

    /**
     * 生成字符串ID
     */
    public static String generateIdStr() {
        return String.valueOf(generateId());
    }
}
