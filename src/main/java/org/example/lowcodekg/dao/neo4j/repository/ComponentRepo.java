package org.example.lowcodekg.dao.neo4j.repository;

import org.example.lowcodekg.dao.neo4j.entity.Component;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "component", path = "component")
public interface ComponentRepo extends Neo4jRepository<Component, Long> {

    @RestResource(path = "name", rel = "name")
    Component findByName(String name);

    @Query("MATCH c:ComponentRepo) WHERE p.name CONTAINS key RETURN c")
    List<Component> findByNameMatch(@Param("key") String key);
}
