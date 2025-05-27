package org.example.lowcodekg.service;

import org.apache.log4j.chainsaw.Main;
import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.evaluation.DataProcess;
import org.example.lowcodekg.query.service.evaluation.Evaluate;
import org.example.lowcodekg.query.service.processor.MainService;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.llm.LLMService;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.example.lowcodekg.query.utils.FilePrintStream;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.example.lowcodekg.query.utils.Constants.*;
import static org.example.lowcodekg.query.utils.FormatUtil.saveResult;

/**
 * 测试需求推荐模板相关功能
 */
@SpringBootTest
@ActiveProfiles("test")
public class RecommendTest {

    @Autowired
    private TaskSplit taskSplit;
    @Autowired
    private TaskMatching taskMatching;
    @Autowired
    private TaskMerge taskMerge;
    @Autowired
    private ElasticSearchService esService;
    @Autowired
    private IRGenerate irGenerate;
    @Autowired
    private TemplateRetrieve templateRetrieve;
    @Autowired
    private LLMService llmService;
    @Autowired
    private Evaluate evaluate;
    @Autowired
    private MainService mainService;

    //    @BeforeEach
    @Test
    void setUp() throws IOException {
//        esService.deleteIndex("test");;
//        esService.deleteAllIndices();
        // 确保索引存在
        esService.createDefaultIndex();
    }

    @Test
    void test() {
//        FormatUtil.setPrintStream(logFilePath);

        String query = "实现博客标签管理功能";

        List<Node> result = mainService.recommend(query).getData();
        for(Node node : result) {
            System.out.println(node.getFullName());
        }

//        saveResult(query, resourceList, SAVE_EM_RESULT_PATH);
    }


    @Test
    void testBlogQueryList() {
//        FormatUtil.setPrintStream(logFilePath);

        // load data and run
        Map<String, List<String>> groundTruth = DataProcess.getQueryResultMap(BLOG_GROUND_TRUTH_JSON_FILE_PATH);
        for (Map.Entry<String, List<String>> entry : groundTruth.entrySet()) {
            String query = entry.getKey();
            System.out.println("[Test] Processing query: " + query);
            try {
                List<Node> result = mainService.recommend(query).getData();
                System.out.println("[Info] Found " + result.size() + " results");
                System.out.println("[Results]:");
                for (Node node : result) {
                    System.out.println("  - " + node.getFullName());
                }
                System.out.println("----------------------------------------");
                saveResult(query, result, SAVE_BLOG_RESULT_PATH);
            } catch (Exception e) {
                System.out.println("[Error] Failed to process query: " + query);
                System.out.println("[Error] Exception: " + e.getMessage());
            }
        }

        // evaluate results
        evaluate.evaluate(BLOG_GROUND_TRUTH_JSON_FILE_PATH, SAVE_BLOG_RESULT_PATH);
    }

    @Test
    void testEvaluate() {
//        FormatUtil.setPrintStream(BLOG_EVALUATE_RESULT_PATH);
        evaluate.evaluate(BLOG_GROUND_TRUTH_JSON_FILE_PATH, SAVE_BLOG_RESULT_PATH);
    }
}
