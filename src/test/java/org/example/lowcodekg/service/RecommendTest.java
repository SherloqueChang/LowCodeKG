package org.example.lowcodekg.service;

import org.example.lowcodekg.query.model.IR;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMatching;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.processor.TaskSplit;
import org.example.lowcodekg.query.service.util.ElasticSearchService;
import org.example.lowcodekg.query.service.llm.LLMService;
import org.example.lowcodekg.query.service.ir.IRGenerate;
import org.example.lowcodekg.query.service.util.retriever.TemplateRetrieve;
import org.example.lowcodekg.query.utils.FilePrintStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        // 设置输出到本地日志文件
        String logFilePath = "D://Master//log.txt";
//        String logFilePath = "/Users/chang/Documents/projects/dataset/log.txt";
        FilePrintStream filePrintStream = null;
        try {
            filePrintStream = new FilePrintStream(logFilePath);
            System.setOut(filePrintStream); // 替换 System.out
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String query = "实现博客文章置顶的功能";

        // 需求分解
        TaskGraph taskGraph = taskSplit.taskSplit(query).getData();

        // 基于IR的需求-资源匹配并重排序
        for(Task task : taskGraph.getTasks().values()) {
            System.out.println("task = " + task.toString());
            taskMatching.rerankResource(task);
        }

        // 任务合并
        Map<Task, List<Node>> resourceList = taskMerge.mergeTask(taskGraph, query).getData();
    }

    @Test
    void testUnit() {
        Task task = new Task();
        task.setName("实现博客列表按置顶排序逻辑");
        task.setDescription("在查询博客列表时，首先展示置顶的文章，然后按照创建时间或其他规则排序。");
        List<IR> irList = irGenerate.generateIR(
                task.getName() + ":" + task.getDescription(),
                "workflow")
                .getData();
        task.setIrList(irList);
        task.setIsWorkflow(true);
        task.setCategory(List.of("workflow"));

        List<Node> resourceList = templateRetrieve.queryBySubTask(task).getData();
        taskMatching.rerankResource(task);
    }
}
