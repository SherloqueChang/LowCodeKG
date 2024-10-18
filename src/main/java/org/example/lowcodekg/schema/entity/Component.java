package org.example.lowcodekg.schema.entity;

import lombok.Data;
import org.example.lowcodekg.schema.constant.ComponentCategory;
import org.example.lowcodekg.schema.constant.SceneLabel;
import org.neo4j.graphdb.Label;

/**
 * 低代码组件实体类
 * @author cwh
 */
@Data
public class Component {
    /**
     * neo4j 实体标签
     */
    private static final Label label = Label.label("Component");

    /**
     * 组件名称
     */
    private String name;

    /**
     * 组件类别
     */
    private ComponentCategory category;

    /**
     * 组件场景标签
     */
    private SceneLabel sceneLabel;

    /**
     * 组件描述
     */
    private String description;


    /**
     * 将数据对象持久化到Neo4j
     */
    public void storeInNeo4j() {
        try {

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