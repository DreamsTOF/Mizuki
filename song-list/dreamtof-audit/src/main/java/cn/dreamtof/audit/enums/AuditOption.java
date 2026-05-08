package cn.dreamtof.audit.enums;

/**
 * 审计选项枚举
 * 用于解决 boolean 类型无法区分 "未配置(继承全局)" 和 "显式关闭" 的问题。
 */
public enum AuditOption {
    /**
     * 强制开启 (覆盖全局配置)
     */
    TRUE,

    /**
     * 强制关闭 (覆盖全局配置)
     */
    FALSE,

    /**
     * 跟随全局配置 (默认)
     */
    DEFAULT;

    /**
     * 解析最终状态
     * @param globalConfigValue 全局配置的值
     * @return 最终是否开启
     */
    public boolean resolve(boolean globalConfigValue) {
        if (this == TRUE) return true;
        if (this == FALSE) return false;
        return globalConfigValue;
    }
}
