package org.example.lowcodekg.extraction.document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

import org.example.lowcodekg.model.dao.neo4j.entity.page.ComponentEntity;
import org.example.lowcodekg.extraction.KnowledgeExtractor;
import org.example.lowcodekg.model.schema.entity.page.Component;
import org.springframework.stereotype.Service;

/**
 * Markdown文件解析 AntDesign专用
 * DataDir 以AntDesign的github仓库为输入, 需将@DataDir设置为[Folder:ant-design-master]的路径.
 * 使用 public void extraction() 对指定路径的ant仓库进行解析.
 * 抽取结果将生成一组 "RawData" 的列表; 每个 "RawData" 对象对应一个组件, 里面包含了文档中给出的对应信息.
 * 
 * "RawData" 中包含一个代码示例  "CodeDemo" 列表, 提供了若干代码应用实例.
 * "RawData" 中包含一个配置项    "RawConfigItem" 列表, 提供了该组件的配置项相关信息.
 * "RawData"        ---   public void convertToComponent(Component component)
 * "RawConfigItem"  ---   public void convertToConfigItem(ConfigItem config)
 * 使用上述函数将原始数据转换为schema中的对象.
 * 
 * @BugsOrUnfinished
 * 1. 文档中还有部分信息, 目前与Schema无关, 暂未做任何提取处理.
 * 2. 目前对于不同组件的.md文件中是否存在可能导致bug的差异, 正在人工检查中.
 *      # 已知bug: 组件参数部分，部分组件存在疑似"分层级的参数(?)", 当前版本全部没算进来.
 *      # 已知bug: 大多组件的config信息包含 "版本" 值, 但个别不包括, 目前会导致报错并跳过相关内容.
 *      # 已知bug: 个别组件在.md文档中给出的代码示例并没有对应的demo.md说明, 会导致IOException.
 * 3. 配置项信息中存在一些较复杂的内容形式, 目前统一以字符串形式保存.
 * 
 */
@Service
public class AntMDExtractor extends KnowledgeExtractor {

    // extract results
    public ArrayList<RawData> dataList = new ArrayList<RawData>();

    // private variables for extracting
    private String WorkDir;     // temp sub-directory for current component
    private int lineNum;        // temp counter for current line number


    @Override
    public void extraction() {
        parseData();
        // 转化为 Schema 对象
        for(RawData data : dataList) {
            Component component = data.convertToComponent();
            ComponentEntity componentEntity = component.storeInNeo4j(componentRepo, configItemRepo);
            componentRepo.setComponentDef(componentEntity.getId());
        }
        return;
    }

    private void parseComponent(String fileName, RawData data) {
        lineNum = 0;
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
            while (lineNum < lines.size()) {
                parseLine(lines, data);
                data.setLanguage("React");
                data.setSource("Ant-Design");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
    
    private void parseLine(List<String> lines, RawData data) {
        String line = lines.get(lineNum++);
        if (line.startsWith("category: ")) {
            // category / default = Components
            data.setCategory(line.substring(10));
            line.substring(10);
            return;
        }
        if (line.startsWith("group: ")) {
            // group / sceneLabel-functional
            data.setSceneLabel(line.substring(7));
        }
        if (line.startsWith("title: ")) {
            // title / name-EN
            data.setName(line.substring(7));
            return;
        }
        if (line.startsWith("subtitle")) {
            // subtitle / name-CN
            data.setName_CN(line.substring(10));
            return;
        }
        if (line.startsWith("description: ")) {
            // description / description-CN-concise
            data.setDescription(line.substring(13));
            return;
        }
        if (line.startsWith("## 何时使用")) {
            // ## 何时使用 / usage
            parseUsage(lines, data);
            return;
        }
        if (line.startsWith("## 代码演示")) {
            // ## 代码演示 / 代码样例
            parseDemo(lines, data);
            return;
        }
        if (line.startsWith("## API")) {
            // ## API / 组件配置项
            parseConfig(lines, data);
        }
    }

    private void parseUsage(List<String> lines, RawData data) {
        String line;
        while (lineNum < lines.size()) {
            line = lines.get(lineNum);
            if (!line.startsWith("##")) {
                data.setUsage(data.getUsage() + line);
            } else {
                parseLine(lines, data);
                return;
            }
            lineNum++;
        }
        return;
    }

    private void parseDemo(List<String> lines, RawData data) {
        String line;
        while (lineNum < lines.size()) {
            line = lines.get(lineNum);
            if (line.startsWith("##")) {
                parseLine(lines, data);
                return;
            }
            if (line.startsWith("<code src=") && line.indexOf(".tsx") >= 18) {
                String demoName = line.substring(18, line.indexOf(".tsx"));
                CodeDemo demo = new CodeDemo(demoName);
                demo.setName_CN(line.substring(line.indexOf(">", line.indexOf(".tsx", 0)) + 1,
                line.indexOf("</code>")));
                // parse demo files (Code & Description)
                demo.parseCode(WorkDir + "/demo/" + demoName + ".tsx");
                demo.parseDescription(WorkDir + "/demo/" + demoName + ".md");
                data.getCodeDemos().add(demo);
            }
            lineNum++;
        }
        return;
    }

    private void parseConfig(List<String> lines, RawData data) {
        String line;
        while (lineNum < lines.size()) {
            // TODO: 有一些异种“配置项”，主要是不足5列的部分。暂未专门有效区分。
            line = lines.get(lineNum);
            if (line.startsWith("## ")) {
                parseLine(lines, data);
                return;
            }
            if (line.startsWith("| ") && !line.startsWith("| 属性") && !line.startsWith("| 参数") && !line.startsWith("| 名称") && !line.startsWith("| Property") && !line.startsWith("| --- |")) {
                line = line.substring(1, line.length() - 2);
                String[] values = line.split(" \\|");

                if (values.length < 2) {
                    System.err.println("Error: Invalid configItem values: " + WorkDir + ": " + line);
                    lineNum++;
                    continue;
                }

                RawConfigItem config = new RawConfigItem();
                config.setName(values[0].substring(1));
                config.setDescription(values[1].substring(1));

                if (values.length >= 3) {
                    config.setType(values[2].substring(1));
                }
                if (values.length >= 5) {
                    config.setDefaultValue(values[3].substring(1));
                    config.setVersion(values[4].substring(1));
                }
                if (values.length >= 6) {
                    // System.err.println("Error: Invalid configItem values: " + WorkDir + ": " + line);
                }
                data.getConfigItems().add(config);
            }
            lineNum++;
        }
    }

    public void parseData() {
        for(String filePath: this.getDataDir()) {
            String path = filePath + "/components";
            File[] folders = new File(path).listFiles(File::isDirectory);
            for (int i = 0; i < folders.length; i++) {
                Path p = Paths.get(path, folders[i].getName(), "index.zh-CN.md");
                if (!Files.exists(p)) {
                    System.err.println(".md File Not Found (May not component): " + p.toString());
                    continue;
                } else {
                    RawData data = new RawData();
                    WorkDir = path + "/" + folders[i].getName();
                    parseComponent(WorkDir + "/index.zh-CN.md", data);
                    if (data.getName().equals("组件总览") || data.getName().equals("Util")) {
                        continue;
                    }
                    dataList.add(data);
                }
            }
        }
    }

}