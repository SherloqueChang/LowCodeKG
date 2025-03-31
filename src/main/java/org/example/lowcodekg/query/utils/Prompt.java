package org.example.lowcodekg.query.utils;

/**
 * @Description 与大模型交互的Prompt设计
 * @Author Sherloque
 * @Date 2025/3/23 17:56
 */
public interface Prompt {

    /**
     * 总结工作流功能信息
     */
    public static final String WORKFLOW_SUMMARIZE_PROMPT = """
            You are an expert in software development and code analysis. 
            Your task is to analyze a given code snippet and provide a concise and brief summary of its functionality in Chinese. 
            Please focus on the core logic and purpose of the code, and present your summary in the following JSON format:
            ```json
            {
                "functionality": ""
            }
            ```
            
            Here is the code snippet for analysis:
            {code}
            """;

    /**
     * 总结页面功能信息
     */
    public static final String PAGE_SUMMARIZE_PROMPT = """
            You are an expert in software development and code analysis. 
            Your task is to analyze a given code snippet in a **Front-End** project and provide a concise and brief summary of its functionality in Chinese. 
            Please focus on the core functionality of the code, and present your summary in the following JSON format:
            ```json
            {
                "functionality": ""
            }
            ```
            
            In your analysis, please pay attention to the following keywords: 
            {keywords}. 
            These keywords are provided to help you better understand the context and focus of the code functionality.
            
            Here is the code snippet for analysis:
            {code}
            """;

    /**
     * 任务拆分Prompt
     */
    public final static String TaskSplitPrompt = """
            You are an expert in Software Engineering with extensive experience in analyzing complex software projects and source code. 
            
            Your task is to break down the given task description into multiple logical subtasks based on the provided code snippets and descriptions. Focus on identifying appropriate split points within the function. 
            
            For example, for a "user login" task, the process can be decomposed into: 
            1. User fills in login credentials. 
            2. Login request is sent. 
            3. User credentials are verified. 
            4. Login success response is returned. 
            
            **Provided Code:** 
            {code} 
            
            **Task Description:** 
            {task} 
            
            ### **Output Format (in Chinese):** 
            Please return the subtasks in the following JSON format: 
            ```json 
            { 
                "subtasks": [ 
                    { 
                        "id": 1, 
                        "name": "子任务1", 
                        "description": "子任务1的详细描述" 
                    }, 
                    { 
                        "id": 2, 
                        "name": "子任务2", 
                        "description": "子任务2的详细描述" 
                    } 
                ] 
            } 
     
            Ensure that:
             - The output is in Chinese.
             - Each subtask is logical, granular, and sequentially meaningful.
             - The breakdown should follow the flow of functionality implementation.     
            """;

    /**
     * 从任务描述中抽取IR列表
     */
    public static final String TASK_TO_IR_PROMPT = """
            **Role:**
            You are an expert in Natural Language Processing specializing in structured information extraction. 
            Your task is to analyze input text and decompose it into discrete operational components using the DSL schema.
            
            **Task**:
            Extract ALL actionable instructions from the input text and map them to a list of DSL objects. Each DSL must represent a SINGLE atomic operation with explicit contextual dependencies.
            
            **Guidelines:**
            
            1. **DSL Field Definitions**:
               - `action`: *Required* - The core verb/operation (e.g., "filter", "sort", "export").
               - `object`: *Required* - The entity being manipulated (e.g., "raw_data", "user_logs").
               - `target`: *Optional* - The output destination/format (e.g., "CSV_file", "database_table").
               - `condition`: *Optional* - Environmental constraints or prerequisites (e.g., "if_errors_detected", "during_nightly_maintenance").
            
            2. **Input**:
               - You will be provided with a natural language description that describes a functional requirement.
            
            3. **Output**:
               - Your task is to identify and extract the relevant information from the description and organize it into DSL objects.
               - Return the extracted DSL objects in the following JSON format:
            
            ```json
            {
                "IR": [
                    {
                        "action": "<action_1>",
                        "object": "<object_1>",
                        "target": "<target_1>",
                        "condition": "<condition_1>"
                    },
                    {
                        "action": "<action_2>",
                        "object": "<object_2>",
                        "target": "<target_2>",
                        "condition": "<condition_2>"
                    },
                    ...
                ]
            }
            ```
            
            4. **Example**:
               - **Natural Language Description**:
                 ```
                 The system needs to validate the user input. If the input is correct, it should store the data in the database. If the input is incorrect, it should display an error message.
                 ```
               - **Expected JSON Output**:
                 ```json
                 {
                     "IR": [
                         {
                             "action": "validate",
                             "object": "user input",
                             "target": "validation result",
                             "condition": "N/A"
                         },
                         {
                             "action": "store",
                             "object": "data",
                             "target": "database",
                             "condition": "input is correct"
                         },
                         {
                             "action": "display",
                             "object": "error message",
                             "target": "user interface",
                             "condition": "input is incorrect"
                         }
                     ]
                 }
                 ```
            
            5. **Instructions**:
               - Carefully read the natural language description to identify actions, objects, targets, and conditions.
               - Ensure that each extracted DSL object accurately reflects the information in the description.
               - If any component (action, object, target, condition) is not explicitly mentioned, use "N/A" or an appropriate placeholder.
               - The JSON output must strictly follow the provided format.
               - The language of the results you return should be consistent with the language used for the given description.
            
            **Task Description (Input):**
            {Task}
            
            **JSON Output (Expected Result):**
            ```json
            {
                "IR": [
                    {
                        "action": "<action_1>",
                        "object": "<object_1>",
                        "target": "<target_1>",
                        "condition": "<condition_1>"
                    },
                    ...
                ]
            }
            ```
            """;

    /**
     * 从资源描述中抽取IR列表
     */
    public static final String RESOURCE_TO_IR_PROMPT = """
            **Role:**
            You are an expert in Natural Language Processing specializing in structured information extraction. 
            Your task is to analyze input text and code and decompose it into discrete operational components using the DSL schema.
           
            **Task**:
            Extract ALL actionable instructions from the input text or code and map them to a list of DSL objects. Each DSL must represent a SINGLE atomic operation with explicit contextual dependencies.
           
            **Guidelines:**
           
            1. **DSL Field Definitions**:
               - `action`: *Required* - The core verb/operation (e.g., "filter", "sort", "export").
               - `object`: *Required* - The entity being manipulated (e.g., "raw_data", "user_logs").
               - `target`: *Optional* - The output destination/format (e.g., "CSV_file", "database_table").
               - `condition`: *Optional* - Environmental constraints or prerequisites (e.g., "if_errors_detected", "during_nightly_maintenance").
          
            2. **Input**:
               - You will be provided with a natural language description that describes a functional requirement, and possibly relevant code concerning to the implementation.
           
            3. **Output**:
               - Your task is to identify and extract the relevant information from the description and organize it into DSL objects.
               - Return the extracted DSL objects in the following JSON format:
           
            ```json
            {
                "IR": [
                    {
                        "action": "<action_1>",
                        "object": "<object_1>",
                        "target": "<target_1>",
                        "condition": "<condition_1>"
                    },
                    {
                        "action": "<action_2>",
                        "object": "<object_2>",
                        "target": "<target_2>",
                        "condition": "<condition_2>"
                    },
                    ...
                ]
            }
            ```
           
            4. **Example**:
               - **Natural Language Description**:
                 ```
                 The system needs to validate the user input. If the input is correct, it should store the data in the database. If the input is incorrect, it should display an error message.
                 ```
               - **Expected JSON Output**:
                 ```json
                 {
                     "IR": [
                         {
                             "action": "validate",
                             "object": "user input",
                             "target": "validation result",
                             "condition": "N/A"
                         },
                         {
                             "action": "store",
                             "object": "data",
                             "target": "database",
                             "condition": "input is correct"
                         },
                         {
                             "action": "display",
                             "object": "error message",
                             "target": "user interface",
                             "condition": "input is incorrect"
                         }
                     ]
                 }
                 ```
           
            5. **Instructions**:
               - Carefully read the natural language description to identify actions, objects, targets, and conditions.
               - Ensure that each extracted DSL object accurately reflects the information in the description.
               - If any component (action, object, target, condition) is not explicitly mentioned, use "N/A" or an appropriate placeholder.
               - The JSON output must strictly follow the provided format.
               - The language of the results you return should be consistent with the language used for the given description.
           
            **Task Description (Input):**
            {Task}
           
            **JSON Output (Expected Result):**
            ```json
            {
                "IR": [
                    {
                        "action": "<action_1>",
                        "object": "<object_1>",
                        "target": "<target_1>",
                        "condition": "<condition_1>"
                    },
                    ...
                ]
            }
            ```
            """;

    /**
     * 识别子任务之间的依赖关系
     */
    public static final String IDENTIFY_TASK_DEPENDENCY_PROMPT = """
            ## AI Assistant's Role
            You are an intelligent analytical assistant designed to identify potential dependencies between subtasks in a software development context. Your task is to analyze the provided subtasks and their details, determine dependencies between them, and explain why these dependencies exist.
            
            ## Input Format
            - **Overall Task Description**: A comprehensive summary of the entire task, derived from user requirements or product specifications.
            - **Subtask Details**: A list of subtasks, each described in natural language and structured as follows:
              - `id`: A unique identifier for the subtask.
              - `name`: The name of the subtask.
              - `description`: A detailed explanation of what the subtask accomplishes, typically based on user stories or technical specifications.
              - `DslList`: A list of intermediate representations (IRs) for the subtask, each containing:
                - `action` (Required): The core operation (e.g., "filter", "sort", "export").
                - `object` (Required): The entity being manipulated (e.g., "raw_data", "user_logs").
                - `target` (Optional): The output destination or format (e.g., "CSV_file", "database_table").
                - `condition` (Optional): Constraints or prerequisites (e.g., "if_errors_detected", "during_nightly_maintenance").
            
            ## Dependency Identification Criteria
            To determine dependencies between subtasks, consider the following factors:
            1. **Semantic Relationships**: Analyze the natural language descriptions to identify references to shared data, processes, or sequential operations.
            2. **IR Sequence Logic**: Examine the `DslList` fields, especially how `object` and `target` may indicate dependencies where one subtask produces an output that another subtask consumes.
            3. **Development Workflow**: Consider typical software development workflows, such as data processing pipelines, API integrations, or user interface sequences, to identify logical dependencies.
            
            ## Input Content
            The overall task description is:
            {query}
            
            And the detail of subtasks is as follow:
            {subTasks}
           
            ## Expected Output
            Your response should be a JSON object listing dependent subtask pairs, with explanations in Chinese. The format is:
            ```json
            {
                "Dependencies": [
                    {
                        "taskId1": "taskId2",
                        "dependency": "task2 需要使用 task1 产生的计算结果"
                    },
                    {
                        "taskId3": "taskId4",
                        "dependency": "task3 依赖 task4 生成的输入数据"
                    }
                ]
            }
            ```
            
            ### Additional Guidelines
            - The dependencies should be determined strictly based on the provided criteria.
            - Always return task **IDs**, not names.
            - If no dependencies are found, return an empty list (`"Dependencies": []`).
            - Explanations should be clear, precise, and written in **Chinese**.
            """;

    /**
     * 判断子任务的检索对象类型
     */
    public static final String TYPE_OF_RETRIEVED_ENTITY_PROMPT = """
            You are an experienced software engineer with expertise in analyzing development tasks and identifying relevant system objects. 
            
            ### Task Overview 
            You will be provided with: 
            1. A task description that outlines a specific development-related goal. 
            2. A sequence of operations that detail the steps involved in executing the task. 
            
            ### Your Objective 
            Your job is to determine which types of objects need to be retrieved for this task. The possible object types are: 
            - **Page**: Represents a UI page or interface that users interact with. 
            - **Workflow**: Represents a predefined sequence of steps or processes executed in the system. 
            - **DataObject**: Represents structured data entities that store and manage information. 
            
            Carefully analyze both the task description and the sequence of operations to determine which of the above object types are required. The task may involve multiple object types. 
            
            ### Input Content
            {Task}
            
            ### Response Format 
            Return your answer as a JSON object with boolean values indicating whether each object type is relevant: 
            
            ```json
            {
              "Page": true/false,
              "Workflow": true/false,
              "DataObject": true/false
            }
            
            Ensure your classification is based strictly on the provided information. If the task does not explicitly or implicitly require a certain object type, mark it as false.
            """;

    /**
     * 根据任务的上下游依赖对检索结果进行初步筛选
     */
    public static final String FILTER_BY_DEPENDENCY_PROMPT = """
            You are tasked with filtering a list of retrieved resources based on the dependencies of a software development task. 
            Your goal is to retain only the resources that are directly relevant to the task's **upstream and downstream dependencies**. 
            
            ## **Input Data:** 
            1. **Task Description**: A description of the user's software development task. 
            2. **Upstream Dependencies**: Tasks that must be completed before this task can begin. 
            3. **Downstream Dependencies**: Tasks that depend on the completion of this task. 
            4. **Retrieved Resources**: A list of resources retrieved for the task, formatted as follows: 
                Resource { id="", name="", label="", content="", description="" }     
           
            ## **Instructions:** 
             - Analyze the task description, upstream dependencies, and downstream dependencies.
            - Pay special attention to the input and output types of the dependent tasks.
             - A resource is considered relevant if it meets at least one of the following conditions:
                - It directly describes, implements, or supports an upstream/downstream task.
                - It provides critical information about the inputs/outputs of dependencies.
                - It includes APIs, tools, or libraries essential to the dependencies.
             - Exclude resources that do not contribute to understanding or executing the dependencies.
             - Return only the filtered resources in the JSON format:
            
            ## Input Content
            
            ### The task Description is:
            {task}
            
            ### The upstream dependencies is(may be null):
            {upstreamDependency}
            
            ### The downstream dependencies is(may be null):
            {downstreamDependency}
            
            ### The retrieved resource list is as follows:
            {nodeList}
            
            Please return the names of reserved tasks in the following JSON format.
            ```json
            {
              "resources": [
                {
                  "name": ""
                },
                ...
              ]
            }
            ```
            """;
}
