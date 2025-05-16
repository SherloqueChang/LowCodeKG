package org.example.lowcodekg.query.service.processor;

import org.example.lowcodekg.model.dao.neo4j.entity.template.TemplateEntity;
import org.example.lowcodekg.model.result.Result;

import java.util.List;

public interface QueryService {

    Result<List<TemplateEntity>> queryTemplate(String query, String platform);
}
