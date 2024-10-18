package org.example.lowcodekg.schema.relation;

import org.neo4j.graphdb.RelationshipType;
import org.springframework.data.neo4j.core.schema.Relationship;

public class RelationType {

    public static final RelationshipType CONTAIN = RelationshipType.withName("CONTAIN");

    public static final RelationshipType INVOKE = RelationshipType.withName("INVOKE");
}
