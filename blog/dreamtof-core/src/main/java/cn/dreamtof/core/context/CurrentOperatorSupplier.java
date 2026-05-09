package cn.dreamtof.core.context;

/**
 * [SPI接口] 当前操作人提供者
 * <p>
 * 你的业务系统（API层）需要实现这个接口并注册为 Bean。
 * 例如：从 Sa-Token、Spring Security 或 Session 中获取当前登录用户并返回。
 * </p>
 */
public interface CurrentOperatorSupplier {
    /**
     * 获取当前操作人
     * @return Operator 对象，若未登录则返回 null
     */
    Operator getOperator();
}
