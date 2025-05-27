package org.example.lowcodekg.query.service.evaluation;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.example.lowcodekg.query.model.Node;
import org.example.lowcodekg.query.service.processor.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.example.lowcodekg.query.utils.Constants.BLOG_EVALUATE_RESULT_PATH;


/**
 * 执行实验验证方法效果
 */
@Service
public class Evaluate {

    @Autowired
    private Metric metric;

    /**
     * 执行评估并返回整体评估结果
     * @Param resultPath 预测结果保存路径
     * @return double[] 包含[平均准确率, 平均召回率]
     */
    public double[] evaluate(String groundTruthPath, String resultPath) {
        try {
            // 创建评估结果列表
            List<JSONObject> evaluationResults = new ArrayList<>();
            
            // 加载真实结果
            Map<String, List<String>> groundTruth = DataProcess.getQueryResultMap(groundTruthPath);
            
            // 加载预测结果
            JSONObject predictedJson = loadPredictedResults(resultPath);

            double totalPrecision = 0.0;
            double totalRecall = 0.0;
            int validQueryCount = 0;

            // 获取预测结果数组
            JSONArray predictedArray = predictedJson.getJSONArray("predicted");
            
            // 处理每个查询
            for (int i = 0; i < predictedArray.size(); i++) {
                JSONObject queryResult = predictedArray.getJSONObject(i);
                String query = queryResult.getString("query");
                
                // 如果该查询在groundTruth中不存在，跳过
                if (!groundTruth.containsKey(query)) {
                    System.out.println("Skipping query: " + query + ", not found in ground truth.");
                    continue;
                }

                try {
                    // 合并所有任务的resources并去重
                    Set<String> predictedResources = new HashSet<>();
                    JSONArray tasks = queryResult.getJSONArray("resources");
                    for (int j = 0; j < tasks.size(); j++) {
                        String task = tasks.getString(j);
                        predictedResources.add(task);
                    }
                    List<String> predicted = new ArrayList<>(predictedResources);
                    
                    // 获取真实结果
                    List<String> groundTruthResult = groundTruth.get(query);
                    
                    // 计算当前查询的指标
                    double precision = metric.calculatePrecision(predicted, groundTruthResult);
                    double recall = metric.calculateRecall(predicted, groundTruthResult);
                    
                    // 累加结果
                    totalPrecision += precision;
                    totalRecall += recall;
                    validQueryCount++;

                    // 创建单个查询的评估结果JSON对象
                    JSONObject evaluationResult = new JSONObject();
                    evaluationResult.put("query", query);
                    evaluationResult.put("precision", precision);
                    evaluationResult.put("recall", recall);
                    evaluationResult.put("predicted", predicted);
                    evaluationResult.put("groundTruth", groundTruthResult);
                    
                    // 将查询结果添加到结果列表
                    evaluationResults.add(evaluationResult);

                } catch (Exception e) {
                    JSONObject errorResult = new JSONObject();
                    errorResult.put("query", query);
                    errorResult.put("error", e.getMessage());
                    evaluationResults.add(errorResult);
                }
            }

            // 创建总体评估结果JSON对象
            JSONObject overallResults = new JSONObject();
            if (validQueryCount > 0) {
                double avgPrecision = totalPrecision / validQueryCount;
                double avgRecall = totalRecall / validQueryCount;
                
                overallResults.put("avgPrecision", avgPrecision);
//                overallResults.put("avgRecall", avgRecall);
                overallResults.put("validQueryCount", validQueryCount);
                overallResults.put("queryResults", evaluationResults);
                
                // 将结果保存到文件
                try (FileWriter file = new FileWriter(BLOG_EVALUATE_RESULT_PATH)) {
                    file.write(overallResults.toJSONString());
                } catch (IOException e) {
                    System.err.println("Error saving results to file: " + e.getMessage());
                }
                
                // 打印总体评估结果
                System.out.println("\n=== Overall Evaluation Results ===");
                System.out.printf("Average Precision: %.4f\n", avgPrecision);
//                System.out.printf("Average Recall: %.4f\n", avgRecall);
                System.out.printf("Total Valid Queries: %d\n", validQueryCount);
                
                return new double[]{avgPrecision, avgRecall};
            }

            return new double[]{0.0, 0.0};

        } catch (Exception e) {
            System.err.println("Error in evaluate: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error in evaluate: " + e.getMessage());
        }
    }

    private JSONObject loadPredictedResults(String resultPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(resultPath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return JSONObject.parseObject(content.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error reading predicted results: " + e.getMessage());
        }
    }
}
