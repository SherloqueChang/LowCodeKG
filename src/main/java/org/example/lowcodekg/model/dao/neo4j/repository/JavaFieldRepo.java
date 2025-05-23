package org.example.lowcodekg.model.dao.neo4j.repository;

import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaFieldEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JavaFieldRepo extends Neo4jRepository<JavaFieldEntity, Long> {

    @Query("MATCH (s:JavaField) WHERE id(s)=$sid " +
            "MATCH (e:JavaClass) WHERE id(e)=$eid " +
            "CREATE (s)-[:FIELD_TYPE]->(e)")
    void createRelationOfFieldType(Long sid, Long eid);
}
