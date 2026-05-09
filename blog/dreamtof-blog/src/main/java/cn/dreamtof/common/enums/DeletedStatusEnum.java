package cn.dreamtof.common.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

/**
 * 删除状态枚举
 * 用于标记记录的逻辑删除状态
 */
@Getter
public enum DeletedStatusEnum {

    /**
     * 未删除
     */
    NOT_DELETED(0, "未删除"),

    /**
     * 已删除
     */
    DELETED(1, "已删除");

    /**
     * 数据库存储值（int 类型）
     */
    @EnumValue
    private final int code;

    /**
     * 显示文本
     */
    private final String text;

    DeletedStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 数据库存储的 code 值
     * @return 枚举值
     */
    public static DeletedStatusEnum getByCode(int code) {
        for (DeletedStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取显示文本
     *
     * @param code 数据库存储的 code 值
     * @return 显示文本
     */
    public static String getTextByCode(int code) {
        DeletedStatusEnum status = getByCode(code);
        return status != null ? status.getText() : null;
    }
}
