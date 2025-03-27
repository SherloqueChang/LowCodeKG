package org.example.lowcodekg.service;

import org.example.lowcodekg.model.dao.es.document.Document;
import org.example.lowcodekg.query.service.retriever.ElasticSearchService;
import org.example.lowcodekg.query.utils.EmbeddingUtil;
import org.example.lowcodekg.query.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ElasticSearchServiceTest {

    @Autowired
    private ElasticSearchService esService;

    @BeforeEach
    void setUp() throws IOException {
        esService.deleteIndex("test");
        // 确保索引存在
        String result = esService.createIndex(Document.class, "test");
        System.out.println("Index creation result: " + result);
    }

    @Test
    void testCreateIndex() throws IOException {
        // 测试重复创建索引
        String result = esService.createIndex(Document.class, "test");
        assertTrue(result.contains("已存在"));
    }

    @Test
    void testIndexAndSearchDocument() throws IOException, InterruptedException {
        // 创建测试文档
        Document doc1 = new Document();
        doc1.setId(UUID.randomUUID().toString());
        doc1.setName("Java Programming Guide");
        doc1.setContent("Java is a popular programming language. It is used for web development, Android apps, and enterprise software.");
        doc1.setNeo4jId(1L);
        float[] embedding1 = FormatUtil.ListToArray(EmbeddingUtil.embedText(doc1.getName() + "\n" + doc1.getContent()));
        doc1.setEmbedding(embedding1);

        Document doc2 = new Document();
        doc2.setId(UUID.randomUUID().toString());
        doc2.setName("Python Tutorial");
        doc2.setContent("Python is an easy to learn programming language. It's great for data science and machine learning.");
        float[] embedding2 = FormatUtil.ListToArray(EmbeddingUtil.embedText(doc2.getName() + "\n" + doc2.getContent()));
        doc2.setEmbedding(embedding2);

        // 索引文档
        esService.indexDocument(doc1, "test");
        esService.indexDocument(doc2, "test");

        // 等待索引刷新
        Thread.sleep(1000);

        // 测试文本搜索（自定义参数）
        List<Document> results2 = esService.searchByText("Java programming", 1, 0, "test");
        System.out.println(results2.get(0).getNeo4jId());

        // 测试向量搜索
        List<Document> results3 = esService.searchByVector(embedding1, 1, 0, "test");
        System.out.println(results3);

        // 测试向量搜索（自定义参数）
        List<Document> results4 = esService.searchByVector(embedding2, 2, 0.7, "test");
        System.out.println(results4);
    }

    @Test
    public void testCalculateSimilarity() {
        String text1 = "Hello, World!";
        String text2 = "Goodbye, World!";
        List<Float> vector1 = Arrays.asList(0.1f, 0.2f, 0.3f);
        List<Float> vector2 = Arrays.asList(0.4f, 0.5f, 0.6f);

        List<Float> embedding1 = EmbeddingUtil.embedText(text1);
        System.out.println(embedding1.size());
        List<Float> embedding2 = EmbeddingUtil.embedText(text2);
        double sim = EmbeddingUtil.calculateSimilarity(text1, text2);
        System.out.println(sim);

        double expectedSimilarity = 0.9746318461970763; // 预期的余弦相似度值
        double result = EmbeddingUtil.calculateSimilarity(text1, text2);

        System.out.println("余弦相似度：" + result);
    }
} 