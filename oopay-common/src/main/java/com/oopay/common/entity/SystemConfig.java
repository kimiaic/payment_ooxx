package com.oopay.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置
 * 存储系统参数和配置项
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_config")
public class SystemConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置项编码（唯一标识）
     */
    private String configKey;

    /**
     * 配置项名称
     */
    private String configName;

    /**
     * 配置项值
     */
    private String configValue;

    /**
     * 配置项描述
     */
    private String description;

    /**
     * 分组编码
     */
    private String groupCode;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 数据类型：string-字符串 int-整数 decimal-小数 json-JSON数组 bool-布尔
     */
    private String dataType;

    /**
     * 是否可编辑 0-只读 1-可编辑
     */
    private Integer editable;

    /**
     * 是否加密存储 0-明文 1-加密
     */
    private Integer encrypted;

    /**
     * 配置项排序
     */
    private Integer sortOrder;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
