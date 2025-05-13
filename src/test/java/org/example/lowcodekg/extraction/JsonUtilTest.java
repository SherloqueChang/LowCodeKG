package org.example.lowcodekg.extraction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.example.lowcodekg.query.utils.JsonUtil;

import java.util.Map;

@SpringBootTest
public class JsonUtilTest {

    @Autowired
    private JsonUtil jsonUtil;

    @Test
    public void testSaveEntities() {
        String path = "D:\\Master\\data.json";
        jsonUtil.saveEntitiesToJson(path);
    }

    @Test
    public void testLoadEntities() {
        String path = "D:\\Master\\data.json";
        Map<String, Map<String, String>> entities = jsonUtil.loadEntitiesFromJson(path);
        for(String key : entities.keySet()) {
            System.out.println(key);
            System.out.println(entities.get(key).get("ir"));
        }
    }
}
