package org.example.lowcodekg.query.service.processor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.common.config.DebugConfig;
import org.example.lowcodekg.model.result.Result;
import org.example.lowcodekg.model.result.ResultCodeEnum;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.model.Task;
import org.example.lowcodekg.query.model.TaskGraph;
import org.example.lowcodekg.query.service.processor.TaskMerge;
import org.example.lowcodekg.query.service.llm.LLMService;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.example.lowcodekg.query.utils.Prompt.RERANK_WITHIN_TASK_PROMPT;

/**
 * @Description
 * @Author Sherloque
 * @Date 2025/3/22 21:02
 */
@Service
public class TaskMergeImpl implements TaskMerge {
    @Autowired
    private DebugConfig debugConfig;
    @Autowired
    private LLMService llmService;

    @Override
    public Result<Map<Task, Set<Node>>> mergeTask(TaskGraph graph, String query) {
        try {
            JSONObject input = new JSONObject();
            input.put("task", query);
            JSONArray subTasks = new JSONArray();
            List<Task> sortedTasks = graph.topologicalSort();
            for(Task task : sortedTasks) {
                subTasks.add(buildSubTaskJson(task));
            }
            input.put("subTasks", subTasks);
            String prompt = RERANK_WITHIN_TASK_PROMPT.replace("{input}", input.toJSONString());
            String answer = FormatUtil.extractJson(llmService.chat(prompt));
            Map<Task, Set<Node>> result = filterResourcesByLLM(answer, sortedTasks);

//            Map<Task, Set<Node>> result = new HashMap<>();
//            for(Task task : graph.getTasks().values()) {
//                result.put(task, new HashSet<>(task.getResourceList()));
//            }

            if(debugConfig.isDebugMode()) {
                for(Task task : result.keySet()) {
                    System.out.println("Task: " + task.getName());
                    for(Node node : result.get(task)) {
                        System.out.println("Resource: " + node.getName());
                    }
                }
            }

            return Result.build(result, ResultCodeEnum.SUCCESS);

        } catch (Exception e) {
            System.err.println("Error in mergeTask: " + e.getMessage());
            throw new RuntimeException("Error in mergeTask: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> rerankWithinTask(Task task) {
        try {
            List<Node> nodeList = task.getResourceList();
            String taskInfo = task.getName() + ":" + task.getDescription();
            StringBuilder resourceInfo = new StringBuilder();
            for(Node node: nodeList) {
                resourceInfo.append(node.toString()).append("\n");
            }
            // LLM decision
            String prompt = RERANK_WITHIN_TASK_PROMPT
                    .replace("{task}", taskInfo)
                    .replace("{resourceList}", resourceInfo.toString());
            String answer = FormatUtil.extractJson(llmService.chat(prompt));
            if(debugConfig.isDebugMode()) {
                System.out.println("task: " + task.getDescription());
                System.out.println("rerankWithinTask answer:\n" + answer);
            }
            // select reranked resources by LLM


            return Result.build(null, ResultCodeEnum.SUCCESS);
        } catch (Exception e) {
            System.err.println("Error in rerankWithinTask: " + e.getMessage());
            throw new RuntimeException("Error in rerankWithinTask: " + e.getMessage());
        }
    }

    private JSONObject buildSubTaskJson(Task task) {
        JSONObject subTask = new JSONObject();
        subTask.put("name", task.getName());
        subTask.put("description", task.getDescription());

        JSONArray resourcesArray = new JSONArray();
        for (Node node : task.getResourceList()) {
            JSONObject resource = new JSONObject();
            resource.put("resourceName", node.getName());
            resource.put("resourceDescription", node.getDescription());
            resourcesArray.add(resource);
        }
        subTask.put("resources", resourcesArray);
        return subTask;
    }

    private Map<Task, Set<Node>> filterResourcesByLLM(String answer, List<Task> taskList) {
        try {
            Map<Task, Set<Node>> resultMap = new HashMap<>();
            JSONObject jsonObject = JSON.parseObject(answer);
            for(String subTaskName: jsonObject.keySet()) {
                Set<Node> result = new HashSet<>();
                // each subtask
                Task task = findTaskByName(taskList, subTaskName);
                JSONArray resourceList = jsonObject.getJSONArray(subTaskName);
                for(int i = 0; i < resourceList.size(); i++) {
                    // each resource
                    JSONObject resource = resourceList.getJSONObject(i);
                    String resourceName = resource.getString("resourceName");
                    if (!Objects.isNull(task)) {
                        Node node = findNodeByName(task.getResourceList(), resourceName);
                        if (!Objects.isNull(node)) {
                            result.add(node);
                        }
                    }
                }
                resultMap.put(task, result);
            }
            return resultMap;

        } catch (Exception e) {
            System.err.println("Error in filterResourcesByLLM: " + e.getMessage());
            throw new RuntimeException("Error in filterResourcesByLLM: " + e.getMessage());
        }
    }

    private Task findTaskByName(List<Task> taskList, String name) {
        for (Task task : taskList) {
            if (task.getName().equals(name)) {
                return task;
            }
        }
        return null;
    }

    private Node findNodeByName(List<Node> nodeList, String name) {
        for (Node node : nodeList) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }
}
