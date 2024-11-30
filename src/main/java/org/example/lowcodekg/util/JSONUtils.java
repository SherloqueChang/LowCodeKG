package org.example.lowcodekg.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.List;

/**
 * 工具类，操作 JSON 格式数据
 */
public class JSONUtils {

    /**
     * 读取路径下的记录了描述信息的 JSON 文件，以 fullName 作为 key 返回 Map
     */
    public static Map<String, JSONObject> loadJsonFile(String path) {
        File file = new File(path);

        StringBuilder jsonContent = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[1024];
            int length;
            while((length = reader.read(buffer)) != -1) {
                jsonContent.append(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<JSONObject> jsonList = JSON.parseObject(jsonContent.toString(), List.class);
        return jsonList.stream().collect(java.util.stream.Collectors.toMap(json -> json.getString("fullName"), json -> json));
    }

    public static void main(String[] args) {
        Map<String, JSONObject> jsonMap = loadJsonFile("/Users/chang/Documents/projects/LowCodeKG/src/main/resources/data/javaInfo.json");
//        System.out.println(jsonMap.get("top.naccl.util.MailUtils").getString("description"));
        jsonMap.forEach((key, value) -> System.out.println(value.getString("fullName") + "\n" + value.getString("description") + "\n"));
    }
}
