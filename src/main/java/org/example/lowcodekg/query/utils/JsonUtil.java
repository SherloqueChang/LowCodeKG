package org.example.lowcodekg.query.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 功能：
 * 1. 将数据库相关数据转存到本地
 * 2. 从本地读取数据用于知识图谱实体信息设置
 */
@Component
public class JsonUtil {
    
    @Autowired
    private Neo4jClient neo4jClient;

    /**
     * 从Neo4j数据库读取实体数据并存储到本地json文件
     * @param filePath 存储路径
     */
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
                jsonObject.put("ir", node.get("ir").asString());
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
            String dataObjectCypher = "MATCH (d:DataObject) RETURN d";
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
}
