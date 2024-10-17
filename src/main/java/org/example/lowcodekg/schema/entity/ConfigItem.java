package org.example.lowcodekg.schema.entity;

import lombok.Data;
import org.neo4j.graphdb.Label;

/**
 * 组件配置项
 * @author: cwh
 */
@Data
public class ConfigItem {
    private static final Label label = Label.label("ConfigItem");

    /**
     * 配置项标识符
     */
    private String code;

    /**
     * 配置项类型
     */
    private String type;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 配置项描述
     */
    private String description;
}