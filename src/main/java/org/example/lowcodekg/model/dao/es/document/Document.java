package org.example.lowcodekg.model.dao.es.document;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.neo4j.core.schema.Id;

import java.util.List;

@Data
@org.springframework.data.elasticsearch.annotations.Document(indexName = "documents")
public class Document {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long neo4jId;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Keyword)
    private String cnName;

    @Field(type = FieldType.Keyword)
    private String fullName;

    @Field(type = FieldType.Keyword)
    private String label;

    @Field(type = FieldType.Keyword)
    private String templateUuid;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Dense_Vector, dims = 512)
    private float[] embedding;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String fileUrl;

    @Field(type = FieldType.Keyword)
    private String appKind;

    @Field(type = FieldType.Keyword)
    private String editorKind;

    @Field(type = FieldType.Keyword)
    private String tags;

}
