package org.example.lowcodekg.schema.relation;

import org.neo4j.graphdb.RelationshipType;

public class RelationType {

    public static final RelationshipType CONTAIN = RelationshipType.withName("CONTAIN");

    public static final RelationshipType INVOKE = RelationshipType.withName("INVOKE");
}
