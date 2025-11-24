package com.szh.aicodebackend.model.enums;


import lombok.Getter;

@Getter
public enum CodeGenTypeEnum {
    HTML("HTML模式","html"),
    MULTI_FILE("多文件模式","multi_file");

    private final String text;

    private final String value;

    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value
     * @return
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
