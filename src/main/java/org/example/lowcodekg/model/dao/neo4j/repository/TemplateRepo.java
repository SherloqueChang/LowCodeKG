package org.example.lowcodekg.model.dao.neo4j.repository;

import org.example.lowcodekg.model.dao.neo4j.entity.template.TemplateEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface TemplateRepo extends Neo4jRepository<TemplateEntity, Long> {


}
