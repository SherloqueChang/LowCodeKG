package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.Component;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComponentRepo extends Neo4jRepository<Component, Long> {

    Component findByName(String name);

    @Query("MATCH c:ComponentRepo) WHERE p.name CONTAINS key RETURN c")
    List<Component> findByNameMatch(@Param("key") String key);
}
