package org.example.lowcodekg.query.service.processor.impl;

import org.example.lowcodekg.model.dao.neo4j.entity.template.TemplateEntity;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.service.processor.QueryService;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryServiceImpl implements QueryService {

    @Autowired
    private TemplateRetrieve templateRetrieve;

    @Override
    public Result<List<TemplateEntity>> queryTemplate(String query, String platform) {
        try {
            List<TemplateEntity> templates = templateRetrieve.queryByNL(query).getData();
            return Result.build(templates, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error in queryTemplate: " + e.getMessage());
            return Result.build(null, ResultCodeEnum.FAIL);
        }

    }
}
