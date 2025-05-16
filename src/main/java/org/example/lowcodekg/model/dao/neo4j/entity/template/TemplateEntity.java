package org.example.lowcodekg.model.dao.neo4j.entity.template;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;


/**
 * Neo4j 实体节点：模板
 */
@Node("Template")
@Data
@NoArgsConstructor
public class TemplateEntity {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 模板唯一标识
     */
    @Property("templateUuid")
    private String templateUuid;

    @Property("name")
    private String name;

    @Property("cnName")
    private String cnName;

    @Property("identifier")
    private String identifier;

    @Property("appKind")
    private String appKind;

    @Property("editorKind")
    private String editorKind;

    @Property("description")
    private String description;

    @Property("tags")
    private String tags;

    @Property("url")
    private String url;

    @Property("embedding")
    private List<Float> embedding;

}