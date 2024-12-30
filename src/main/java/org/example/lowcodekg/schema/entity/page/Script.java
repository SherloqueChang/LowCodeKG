package org.example.lowcodekg.schema.entity.page;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.lowcodekg.dao.neo4j.entity.page.ScriptEntity;
import org.example.lowcodekg.dao.neo4j.entity.page.ScriptMethodEntity;
import org.example.lowcodekg.dao.neo4j.repository.ScriptMethodRepo;
import org.example.lowcodekg.dao.neo4j.repository.ScriptRepo;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Script {

    @Data
    @AllArgsConstructor
    public static class ScriptMethod {
        private String name;
        private List<String> params;
        private String content;
    }

    private String name;

    private String description;

    private String content;

    private JSONObject dataList;

    private List<ScriptMethod> methodList = new ArrayList<>();

    private String importsComponentList;

    public ScriptEntity createScriptEntity(ScriptRepo scriptRepo, ScriptMethodRepo scriptMethodRepo) {
        ScriptEntity scriptEntity = new ScriptEntity();
        scriptEntity.setName(name);
        scriptEntity.setDescription(description);
        scriptEntity.setContent(content);
        scriptEntity = scriptRepo.save(scriptEntity);

        if(!Objects.isNull(dataList))
            scriptEntity.setDataList(dataList.toJSONString());
        if(!Objects.isNull(importsComponentList))
            scriptEntity.setImportsComponentList(importsComponentList);
        // 创建 ScriptMethod 实体
        if(!Objects.isNull(methodList)) {
            for (ScriptMethod method : methodList) {
                ScriptMethodEntity methodEntity = new ScriptMethodEntity();
                methodEntity.setName(method.getName());
                methodEntity.setParams(method.getParams());
                methodEntity.setContent(method.getContent());
                methodEntity = scriptMethodRepo.save(methodEntity);
                scriptRepo.createRelationOfContainedMethod(scriptEntity.getId(), methodEntity.getId());
            }
        }
        return scriptEntity;
    }

}


