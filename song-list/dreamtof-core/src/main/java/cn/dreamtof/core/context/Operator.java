package cn.dreamtof.core.context;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 审计专用操作人对象
 * <p>
 * 这是一个纯粹的 DTO，用于在 Infrastructure 层和 API 层之间传递用户信息，
 * 从而避免 Infrastructure 层直接依赖具体的 User 实体。
 * </p>
 */
@Data
@Builder
public class Operator implements Serializable {
    /**
     * 用户 ID
     * <p>
     * 使用 Serializable 类型以兼容不同的 ID 生成策略：
     * - Long: 雪花算法、数据库自增
     * - String: UUID, ULID, MongoDB ObjectId
     * </p>
     */
    private Serializable id;
    /**
     * 用户名
     */

    private String name;

    /**
     * 用户 IP
     */
    private String ip;

    /**
     * 租户 ID
     * <p>
     * 使用 Serializable 类型以兼容不同的 ID 生成策略：
     * - Long: 雪花算法、数据库自增
     * - String: UUID, ULID, MongoDB ObjectId
     * </p>
     */
    private Serializable tenantId;

    /**
     * 客户端唯一设备标识 (浏览器指纹/移动端 UUID)
     * 用于匿名用户识别、防刷、限流
     */
    private String deviceId;


    private String useAgent;
}
