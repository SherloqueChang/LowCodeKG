package org.example.lowcodekg.schema.entity;

import lombok.Data;
import org.example.lowcodekg.dao.neo4j.entity.ComponentEntity;
import org.example.lowcodekg.dao.neo4j.entity.ConfigItemEntity;
import org.example.lowcodekg.dao.neo4j.repository.ComponentRepo;
import org.example.lowcodekg.dao.neo4j.repository.ConfigItemRepo;
import org.example.lowcodekg.schema.constant.ComponentCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;
import org.neo4j.graphdb.Label;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 低代码组件实体类
 */
@Data
public class Component {

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件类别，包括场景标签、功能分类等
     */
    private Category category;

    /**
     * 组件描述
     */
    private String description;

    /**
     * 组件关联的配置型
     */
    private List<ConfigItem> containedConfigItems;

    /**
     * 组件依赖的其他组件（outgoing）
     */
    private List<Component> relatedComponents;


    /**
     * 将数据对象持久化到Neo4j
     */
    public void storeInNeo4j(ComponentRepo componentRepo, ConfigItemRepo configItemRepo) {
        try {
            // 设置组件实体属性
            ComponentEntity entity = new ComponentEntity();
            entity.setName(name);
            entity.setCategory(category.toString());
            entity.setDescription(description);

            // 组件关联的配置型列表
            List<ConfigItemEntity> configItemEntities = new ArrayList<>();
            for(ConfigItem configItem: containedConfigItems) {
                // 设置配置项实体属性
                ConfigItemEntity configEntity = new ConfigItemEntity();
                configEntity.setCode(configItem.getCode());
                configEntity.setType(configItem.getType());
                configEntity.setDefaultValue(configItem.getDefaultValue());
                configEntity.setDescription(configItem.getDescription());
                configItemEntities.add(configEntity);
                // 存储配置项节点
                configItem.storeInNeo4j(configItemRepo);
            }
            entity.getContainedConfigItemEntities().addAll(configItemEntities);

            // Neo4j 存储数据对象
            componentRepo.save(entity);
            configItemRepo.saveAll(entity.getContainedConfigItemEntities());

        } catch (Exception e) {
            System.err.println("Error in storeInNeo4j: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将数据对象持久化到ES
     */
    public void storeInES() {
        try {

        } catch (Exception e) {
            System.err.println("Error in storeInES: " + e.getMessage());
            e.printStackTrace();
        }
    }

}