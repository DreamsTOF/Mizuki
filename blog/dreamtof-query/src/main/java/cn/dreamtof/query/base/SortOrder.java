package cn.dreamtof.query.base;

import lombok.Getter;

/**
 * 排序规则枚举
 */
@Getter
public enum SortOrder {

    ASCEND("升序", "asc"),
    DESCEND("降序", "desc");

    private final String text;

    private final String value;

    SortOrder(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static SortOrder getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (SortOrder anEnum : SortOrder.values()) {
            if (anEnum.value.equalsIgnoreCase(value)) {
                return anEnum;
            }
        }
        return null;
    }
}