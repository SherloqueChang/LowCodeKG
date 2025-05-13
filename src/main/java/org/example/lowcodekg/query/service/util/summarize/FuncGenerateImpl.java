package org.example.lowcodekg.query.service.util.summarize;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.dao.neo4j.entity.java.JavaClassEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.java.WorkflowEntity;
import org.example.lowcodekg.model.dao.neo4j.entity.page.PageEntity;
import org.example.lowcodekg.model.dao.neo4j.repository.JavaClassRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.PageRepo;
import org.example.lowcodekg.model.dao.neo4j.repository.WorkflowRepo;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.service.util.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.utils.JsonUtil;
import org.example.lowcodekg.service.LLMGenerateService;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

import static org.example.lowcodekg.query.utils.Constants.*;
import static org.example.lowcodekg.query.utils.Prompt.PAGE_SUMMARIZE_PROMPT;
import static org.example.lowcodekg.query.utils.Prompt.WORKFLOW_SUMMARIZE_PROMPT;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/23 15:47
 */
@Service
public class FuncGenerateImpl implements FuncGenerate {

    private static final String DESCRIPTION_FILE_PATH = "";

    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private LLMGenerateService llmGenerateService;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private ElasticSearchService esService;
    @Autowired
    private WorkflowRepo workflowRepo;
    @Autowired
    private PageRepo pageRepo;
    @Autowired
    private JavaClassRepo classRepo;
    @Autowired
    private IRGenerate irGenerate;

    @Autowired
    private JsonUtil  jsonUtil;

    @Override
    public void genDataObjectFunc(JavaClassEntity classEntity) {
        try {
            // functional description
            String prompt = WORKFLOW_SUMMARIZE_PROMPT.replace("{code}", classEntity.getContent());
            String result = FormatUtil.extractJson(llmGenerateService.generateAnswer(prompt));
            JSONObject jsonObject = JSONObject.parseObject(result);
            String description = jsonObject.getString("functionality");
            classEntity.setDescription(description);
            // convert to IR list
            List<IR> irList = irGenerate.generateIR(description, "DataObject").getData();
            classEntity.setIr(JSONObject.toJSONString(irList));
            JavaClassEntity entity = classRepo.save(classEntity);

            // create es index
//            entity.setEmbedding(EmbeddingUtil.embedText(JSONObject.toJSONString(irList)));
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, DATA_OBJECT_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in genDataObjectFunc");
        }
    }
    @Override
    public void genWorkflowFunc(WorkflowEntity workflowEntity) {
        try {
            String prompt = WORKFLOW_SUMMARIZE_PROMPT.replace("{code}", workflowEntity.getContent());
            String result = FormatUtil.extractJson(llmGenerateService.generateAnswer(prompt));
            JSONObject jsonObject = JSONObject.parseObject(result);
            String description = jsonObject.getString("functionality");
            workflowEntity.setDescription(description);
            // IR
            List<IR> irList = irGenerate.generateIR(description, "Workflow").getData();
            workflowEntity.setIr(JSONObject.toJSONString(irList));
            WorkflowEntity entity = workflowRepo.save(workflowEntity);

            // create es index
//            entity.setEmbedding(EmbeddingUtil.embedText(JSONObject.toJSONString(irList)));
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, WORKFLOW_INDEX_NAME);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("genWorkflowFunc error, " + workflowEntity.getName());
        }
    }

    @Override
    public void genPageFunc(PageEntity pageEntity) {
        try {

            Map<String, Map<String, String>> descriptionMap = loadEntitiesFromJson(DESCRIPTION_FILE_PATH);
            Map<String, String> properties = descriptionMap.get(pageEntity.getFullName());

            String formattedId = String.format("%d", pageEntity.getId());
            StringBuilder codeContent = new StringBuilder();
            Set<String> keywords = new HashSet<>();
            List<Node> nodeList = new ArrayList<>();

            // get component entity of page entity
            String nodeCypher = MessageFormat.format("""
                MATCH (p:PageTemplate)-[:CONTAIN]->(c:Component)
                where id(p)={0}
                RETURN c
                """, formattedId);
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(nodeCypher);
            while (result.hasNext()) {
                Node node = result.next().get("c").asNode();
                nodeList.add(node);
                // code content
                codeContent.append(node.asMap().get("content"));
                // textual literals
                keywords.add(node.asMap().get("text") + "\n");
            }

            // scan config item property of component entity
            nodeList.forEach(node -> {
                scanComponentProperty(node, keywords);
            });

            // construct final prompt
            String prompt = PAGE_SUMMARIZE_PROMPT
                    .replace("{code}", codeContent.toString())
                    .replace("{keywords}", keywords.toString());
            String res = FormatUtil.extractJson(llmGenerateService.generateAnswer(prompt));
            JSONObject jsonObject = JSONObject.parseObject(res);
            String description = jsonObject.getString("functionality");
            pageEntity.setDescription(description);
            List<IR> irList = irGenerate.generateIR(description, "DataObject").getData();
            pageEntity.setIr(JSONObject.toJSONString(irList));
            PageEntity entity = pageRepo.save(pageEntity);

            // create es index
//            entity.setEmbedding(EmbeddingUtil.embedText(JSONObject.toJSONString(irList)));
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, PAGE_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("generate PageFunctionality error, " + pageEntity.getFullName());
        }
    }

    @Override
    public void genPageFuncFromJson(PageEntity pageEntity, String description, String ir) {
        try {
            pageEntity.setDescription(description);
            pageEntity.setIr(ir);
            PageEntity entity = pageRepo.save(pageEntity);
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, PAGE_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("generate PageFunctionality error, " + pageEntity.getFullName());
        }
    }

    @Override
    public void genWorkflowFuncFromJson(WorkflowEntity workflowEntity, String description, String ir) {
        try {
            workflowEntity.setDescription(description);
            workflowEntity.setIr(ir);
            WorkflowEntity entity = workflowRepo.save(workflowEntity);
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, WORKFLOW_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("generate WorkflowFunctionality error, " + workflowEntity.getFullName());
        }
    }

    @Override
    public void genDataObjectFuncFromJson(JavaClassEntity classEntity, String description, String ir) {
        try {
            classEntity.setDescription(description);
            classEntity.setIr(ir);
            JavaClassEntity entity = classRepo.save(classEntity);
            entity.setEmbedding(EmbeddingUtil.embedText(description));
            Document document = FormatUtil.entityToDocument(entity);
            esService.indexDocument(document, DATA_OBJECT_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("generate DataObjectFunctionality error, " + classEntity.getFullName());
        }
    }

    /**
     * 从Neo4j数据库读取实体数据并存储到本地json文件
     * @param filePath 存储路径
     */
    @Override
    public void saveEntitiesToJson(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("[");
            boolean isFirst = true;

            // 查询PageTemplate实体
            String pageCypher = "MATCH (p:PageTemplate) RETURN p";
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result pageResult = runner.run(pageCypher);

            while (pageResult.hasNext()) {
                if (!isFirst) {
                    writer.write(",");
                }
                isFirst = false;
                Node node = pageResult.next().get("p").asNode();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", node.id());
                jsonObject.put("name", node.get("name").asString());
                jsonObject.put("fullName", node.get("fullName").asString());
                // 先获取description值，再判断是否为空
                var description = node.get("description");
                jsonObject.put("description", description.isNull() ? "" : description.asString());
                writer.write(jsonObject.toJSONString());
            }

            // 查询Workflow实体
            String workflowCypher = "MATCH (w:Workflow) RETURN w";
            Result workflowResult = runner.run(workflowCypher);

            while (workflowResult.hasNext()) {
                if (!isFirst) {
                    writer.write(",");
                }
                isFirst = false;
                Node node = workflowResult.next().get("w").asNode();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", node.id());
                jsonObject.put("name", node.get("name").asString());
                jsonObject.put("fullName", node.get("fullName").asString());
                // 先获取description值，再判断是否为空
                var description = node.get("description");
                jsonObject.put("description", description.isNull() ? "" : description.asString());
                writer.write(jsonObject.toJSONString());
            }

            // 查询DataObject实体
            String dataObjectCypher = "MATCH (d:JavaClass) WHERE d.type = 'DataObject' RETURN d";
            Result dataObjectResult = runner.run(dataObjectCypher);

            while (dataObjectResult.hasNext()) {
                if (!isFirst) {
                    writer.write(",");
                }
                isFirst = false;
                Node node = dataObjectResult.next().get("d").asNode();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", node.id());
                jsonObject.put("name", node.get("name").asString());
                jsonObject.put("fullName", node.get("fullName").asString());
                // 先获取description值，再判断是否为空
                var description = node.get("description");
                jsonObject.put("description", description.isNull() ? "" : description.asString());
                writer.write(jsonObject.toJSONString());
            }

            writer.write("]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从本地json文件读取实体数据
     * @param filePath json文件路径
     * @return 返回以fullName为key的实体数据Map
     */
    @Override
    public Map<String, Map<String, String>> loadEntitiesFromJson(String filePath) {
        Map<String, Map<String, String>> resultMap = new HashMap<>();

        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray jsonArray = JSONArray.parseArray(content);

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject entity = jsonArray.getJSONObject(i);
                String fullName = entity.getString("fullName");

                Map<String, String> properties = new HashMap<>();
                properties.put("id", String.valueOf(entity.getLong("id")));
                properties.put("name", entity.getString("name"));
                properties.put("fullName", fullName);
                properties.put("ir", entity.getString("ir") != null ? entity.getString("ir") : "");
                // 处理description为空的情况
                properties.put("description", entity.getString("description") != null ? entity.getString("description") : "");

                resultMap.put(fullName, properties);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultMap;
    }

    /**
     * 递归扫描组件的配置项属性值，判断是否添加到关键词列表中
     */
    private void scanComponentProperty(Node node, Set<String> keywords) {
        try {
            // current component entity contained config item
            String formattedId = String.format("%d", node.id());
            String nodeCypher = MessageFormat.format("""
                MATCH (p:Component)-[:CONTAIN]->(c:ConfigItem)
                where id(p)={0}
                RETURN c
                """, formattedId);
            QueryRunner runner = neo4jClient.getQueryRunner();
            Result result = runner.run(nodeCypher);
            while (result.hasNext()) {
                Node n = result.next().get("c").asNode();
                keywords.add(getConfigItemValue(n));
            }
            // child component
            nodeCypher = MessageFormat.format("""
                MATCH (p:Component)-[:PARENT_OF]->(c:Component)
                where id(p)={0}
                RETURN c
                """, formattedId);
            result = runner.run(nodeCypher);
            while (result.hasNext()) {
                Node n = result.next().get("c").asNode();
                scanComponentProperty(n, keywords);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("scanConfigItemProperty error, " + node.asMap().get("fullName"));
        }
    }

    private String getConfigItemValue(Node node) {
        try {
            String name = node.asMap().get("name").toString();
            String value = node.asMap().get("value").toString();
            if(name.contains("label") || name.contains("@")) {
                return value;
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }
}
