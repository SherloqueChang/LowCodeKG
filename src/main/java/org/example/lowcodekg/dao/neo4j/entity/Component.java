package org.example.lowcodekg.dao.neo4j.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

/**
 * Neo4j 实体节点：组件
 */
@Node("Component")
public class Component {

    @Id
    private Long id;

    @Property("name")
    private String name;

    /**
     * 组件对组件的依赖
     */
    @Relationship("Dependency")
    private Component relatedComponent;


}
