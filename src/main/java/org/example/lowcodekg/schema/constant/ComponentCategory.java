package org.example.lowcodekg.schema.constant;

/**
 * 组件类别
 */
public enum ComponentCategory {

    WORK_FLOW("工作流"),

    UI_COMPONENT("UI组件");

    private String code;

    ComponentCategory(String code)
    {
        this.code = code;
    }

}
