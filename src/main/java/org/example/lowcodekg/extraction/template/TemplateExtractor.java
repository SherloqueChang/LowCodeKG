package org.example.lowcodekg.extraction.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.model.dao.neo4j.entity.template.TemplateEntity;
import org.example.lowcodekg.query.service.util.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.example.lowcodekg.query.utils.Constants.DEFAULT_INDEX_NAME;

public class TemplateExtractor extends KnowledgeExtractor {

    @Override
    public void extraction() {
        try {
            elasticSearchService.createIndex(Document.class, DEFAULT_INDEX_NAME);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(String filePath: this.getDataDir()) {
            try {
                // 获取目录下所有文件夹
                File directory = new File(filePath);
                File[] subDirectories = directory.listFiles(File::isDirectory);
                
                if (subDirectories != null) {
                    for (File subDir : subDirectories) {
                        // each entity
                        System.out.println("Processing subdirectory: " + subDir.getName());
                        Collection<File> jsonFiles = FileUtils.listFiles(
                            subDir,
                            new String[]{"json"},
                            true
                        );
                        parseTemplateEntity(jsonFiles);

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in Template extraction");
            }
        }
    }

    private TemplateEntity parseTemplateEntity(Collection<File> files) {
        TemplateEntity entity = new TemplateEntity();
        String url = "";
        for(File file : files) {
            try {
                if("data.json".equals(file.getName())) {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    JSONObject jsonObject = JSON.parseObject(content);
                    url = jsonObject.getString("file_url");
                    entity.setUrl(url);
                } else {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    JSONObject jsonObject = JSON.parseObject(content);
                    try {
                        entity.setTemplateUuid(jsonObject.getString("templateUuid") != null ? jsonObject.getString("templateUuid") : "");
                        entity.setName(jsonObject.getString("name") != null ? jsonObject.getString("name") : "");
                        entity.setCnName(jsonObject.getString("cnName") != null ? jsonObject.getString("cnName") : "");
                        entity.setIdentifier(jsonObject.getString("identifier") != null ? jsonObject.getString("identifier") : "");
                        entity.setAppKind(jsonObject.getString("appKind") != null ? jsonObject.getString("appKind") : "");
                        entity.setTags(jsonObject.getString("tags") != null ? jsonObject.getString("tags") : "");
                        entity.setEditorKind(jsonObject.getString("editorKind") != null ? jsonObject.getString("editorKind") : "");
                        // for index
                        String description = entity.getName() + ":" + jsonObject.getString("description") != null ? jsonObject.getString("description") : "";
                        entity.setDescription(description);
                    } catch (Exception e) {
                        System.err.println("Error parsing JSON fields in file: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading JSON file: " + file.getAbsolutePath());
                e.printStackTrace();
            }
            entity = templateRepo.save(entity);
        }
        // es index
        try {
            entity.setEmbedding(EmbeddingUtil.embedText(entity.getDescription()));
            Document document = FormatUtil.templateToDocument(entity);
            elasticSearchService.indexDocument(document, DEFAULT_INDEX_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in Template ES Indexing:" + files);
        }

        return entity;
    }
}
