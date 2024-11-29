# 低代码知识图谱软件项目

面向低代码开发的知识图谱构建及检索服务

## 环境配置
JDK 17 \
Spring Boot 3.3.4 \
Maven 3.9.9

### Neo4j 配置
使用 docker 配置，neo4j版本为5.24

请提前准备一个文件夹 {your_path}，用来挂载容器内的目录
```
docker pull neo4j:5.24

docker run -d -p 7474:7474 -p 7687:7687 --name neo4j-5.24 -e "NEO4J_AUTH=neo4j/neo4j123456" -v {your_path}/data:/data -v {your_path}/logs:/logs -v {your_path}/conf:/var/lib/neo4j/conf -v {your_path}/import:/var/lib/neo4j/import neo4j:5.24
```

### ES 配置
使用 docker 配置 elasticsearch 的版本为8.15.0
```
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.15.0

docker run -d --name elasticsearch \
  -e ELASTIC_USERNAME=elastic \
  -e ELASTIC_PASSWORD=changeme \
  -e discovery.type=single-node \
  -p 9200:9200 \
  -p 9300:9300 \
  docker.elastic.co/elasticsearch/elasticsearch:8.15.0
```
需要对于 elasticsearch 容器的 Files/usr/share/elasticsearch/config/elasticsearch.yml 作如下配置
```
cluster.name: "docker-cluster"
network.host: 0.0.0.0

#----------------------- BEGIN SECURITY AUTO CONFIGURATION -----------------------
#
# The following settings, TLS certificates, and keys have been automatically      
# generated to configure Elasticsearch security features on 13-11-2024 07:02:45
#
# --------------------------------------------------------------------------------

# Enable security features
xpack.security.enabled: true

xpack.security.enrollment.enabled: true

# Enable encryption for HTTP API client connections, such as Kibana, Logstash, and Agents
xpack.security.http.ssl:
  enabled: false
  keystore.path: certs/http.p12

# Enable encryption and mutual authentication between cluster nodes
xpack.security.transport.ssl:
  enabled: true
  verification_mode: certificate
  keystore.path: certs/transport.p12
  truststore.path: certs/transport.p12
http.host: 0.0.0.0
#----------------------- END SECURITY AUTO CONFIGURATION -------------------------
```

## 项目结构
- controller: 前端请求处理，调用服务接口并返回
- dao: 数据访问层，提供 Neo4j 和 ES 的数据存取接口
- extraction: 知识挖掘及关联的插件集
- schema: 定义知识图谱的元模型，包括实体、关系、属性等。通过调用 dao 层接口实现数据持久化
- search: 组件及模板检索的接口定义及实现，基于 dao 层接口调用，向 controller 提供检索服务

## 运行
- 编译打包：mvn package -Dmaven.test.skip=true（跳过测试，可选）
- 执行插件：java -jar target/LowCodeKG-0.0.1-SNAPSHOT.jar -gen {yml_config_path}
- 启动服务：java -jar target/LowCodeKG-0.0.1-SNAPSHOT.jar -exec

config.yml 示例
```yaml

# 依次执行以下插件

# 开源组件库 Ant Design 文档解析
org.example.lowcodekg.extraction.component.AntMDExtractor: 
  - /Users/chang/Documents/projects/ant-design/components
# Java 项目代码解析
org.example.lowcodekg.extraction.java.JavaExtractor:
  - /Users/chang/Documents/projects/data_projects/aurora/aurora-springboot
  - /Users/chang/Documents/projects/data_projects/NBlog/blog-api
```

## 知识图谱 Schema
![img_v4.png](src/main/resources/static/schema_v4.png)

### 查询子图示例
![query_example.png](src/main/resources/static/query_example.png)

## 数据源
- [蚂蚁开源组件库](https://github.com/ant-design/ant-design)
- [博客系统](https://github.com/Naccl/NBlog)