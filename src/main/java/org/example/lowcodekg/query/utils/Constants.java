package org.example.lowcodekg.query.utils;

/**
 * @Description 定义项目内常量
 * @Author Sherloque
 * @Date 2025/3/23 21:02
 */
public final class Constants {

    /**
     * ES 索引名称
     */
    public static final String PAGE_INDEX_NAME = "page";
    public static final String WORKFLOW_INDEX_NAME = "workflow";
    public static final String DATA_OBJECT_INDEX_NAME = "data_object";
    public static final String BLOG_INDEX_NAME = "blog";
    public static final String EMALL_INDEX_NAME = "emall";
    public static final String HOTEL_INDEX_NAME = "hotel";

    public static final String RESOURCE_INDEX_NAME = HOTEL_INDEX_NAME;

    /**
     * 任务检索参数设置
     */
    // 初步检索
    public static final int MAX_RESULTS = 10;
    // 子任务检索
    public static final int MAX_DATA_OBJECT_NUM = 7;
    public static final int MAX_PAGE_NUM = 7;
    public static final int MAX_WORKFLOW_NUM = 7;
    public static final float MIN_SCORE = 0.9f;

    /**
     * 子任务推荐候选资源重排序后的个数
     */
    public static final int MAX_RESOURCE_RECOMMEND_NUM = 5;

    /**
     * 数据记录路径
     */
    public static final String BLOG_GROUND_TRUTH_JSON_FILE_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\blog_ground_truth.json";
    public static final String SAVE_BLOG_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\blog_output.json";
    public static final String BLOG_EVALUATE_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\blog_result.json";

    public static final String EM_GROUND_TRUTH_JSON_FILE_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\em_ground_truth.json";
    public static final String SAVE_EM_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\em_output.json";
    public static final String EM_EVALUATE_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\em_result.json";

    public static final String HOTEL_GROUND_TRUTH_JSON_FILE_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\hotel_ground_truth.json";
    public static final String SAVE_HOTEL_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\hotel_output.json";
    public static final String HOTEL_EVALUATE_RESULT_PATH = "D:\\Master\\LowCodeKG\\src\\main\\resources\\data\\hotel_result.json";

    public static final String logFilePath = "D:\\Master\\log.txt";

}
