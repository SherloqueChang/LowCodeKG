package org.example.lowcodekg.schema.constant;

public enum SceneLabel {

    ECOMMERCE("电商"),

    FINANCE("金融"),

    HEALTH("健康"),

    EDUCATION("教育"),

    ENTERTAINMENT("娱乐"),

    OTHER("其他");

     private String label;

     SceneLabel(String label) {
         this.label = label;
     }
}
