package org.example.lowcodekg.service;


import org.example.lowcodekg.model.dao.neo4j.entity.template.TemplateEntity;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.service.processor.QueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TemplateQueryTest {

    @Autowired
    private QueryService queryService;

    @Test
    public void test() {
        String query = "密码登录模板";

        Result<List<TemplateEntity>> result = queryService.queryTemplate(query, "");

        if(result.getCode() == ResultCodeEnum.SUCCESS.getCode()) {
            List<TemplateEntity> data = result.getData();
            for (TemplateEntity templateEntity : data) {
                System.out.println(templateEntity);
            }
        }

    }
}
