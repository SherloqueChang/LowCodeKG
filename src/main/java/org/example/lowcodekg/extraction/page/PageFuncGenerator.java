package org.example.lowcodekg.extraction.page;

import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;

import java.util.Map;
import java.util.Optional;

import static org.example.lowcodekg.query.utils.Constants.DESCRIPTION_FILE_PATH;

/**
 * 生成页面实体功能描述
 */
public class PageFuncGenerator extends KnowledgeExtractor {

    @Override
    public void extraction() {
        try {

            Map<String, Map<String, String>> descriptionMap = funcGenerateService.loadEntitiesFromJson(DESCRIPTION_FILE_PATH);

            String nodeCypher = """
                MATCH (n:PageTemplate)
                RETURN n
                """;
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(nodeCypher);
            while(result.hasNext()) {
                Node node = result.next().get("n").asNode();
                Optional<PageEntity> pageEntityOptional = pageRepo.findById(node.id());
                pageEntityOptional.ifPresent(pageEntity -> {
                    String description = descriptionMap.get(pageEntity.getFullName()).get("description");
                    String ir = descriptionMap.get(pageEntity.getFullName()).get("ir");
                    funcGenerateService.genPageFuncFromJson(pageEntity, description, ir);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
