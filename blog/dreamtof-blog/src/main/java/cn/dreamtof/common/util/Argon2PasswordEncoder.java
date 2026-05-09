package cn.dreamtof.auth.infrastructure.security;

import org.springframework.stereotype.Component;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;


/**
 * Argon2 密码编码器（替代 BCrypt）
 * <p>
 * Argon2 是密码哈希竞赛的获胜者，相比 BCrypt 具有更好的抗 GPU 破解能力。
 * 配置参数：
 * - 内存成本：65536 KB (64 MB)
 * - 迭代次数：3
 * - 并行度：1
 * </p>
 *
 * @author dream
 * @since 2026-04-23
 */
@Component
public class Argon2PasswordEncoder {

    private final Argon2 argon2;

    // Argon2 配置参数
    private static final int MEMORY_COST = 65536; // 64 MB
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;

    public Argon2PasswordEncoder() {
        // 创建 Argon2 实例（Argon2id 模式，同时抵抗侧信道和 GPU 攻击）
        this.argon2 = Argon2Factory.create(
                Argon2Factory.Argon2Types.ARGON2id,
                16, // salt 长度（16字节）
                32  // hash 长度（32字节）
        );
    }

    /**
     * 对原始密码进行哈希加密
     *
     * @param rawPassword 原始密码（明文）
     * @return 加密后的哈希字符串（包含 salt、参数和 hash）
     */

    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        try {
            // 格式：$argon2id$v=19$m=65536,t=3,p=1$salt$hash
            return argon2.hash(
                    ITERATIONS,
                    MEMORY_COST,
                    PARALLELISM,
                    rawPassword.toCharArray()
            );
        } catch (Exception e) {
            throw new RuntimeException("Argon2 密码加密失败", e);
        }
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword    原始密码（明文）
     * @param encodedPassword 加密后的哈希字符串
     * @return true=匹配，false=不匹配
     */

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        try {
            return argon2.verify(encodedPassword, rawPassword.toCharArray());
        } catch (Exception e) {
            // 验证失败（格式错误或密码不匹配）
            return false;
        }
    }

    /**
     * 检查哈希字符串是否需要重新哈希（参数升级）
     *
     * @param encodedPassword 加密后的哈希字符串
     * @return true=需要重新哈希，false=参数匹配
     */
    public boolean needsRehash(String encodedPassword) {
        return argon2.needsRehash(encodedPassword, MEMORY_COST, ITERATIONS, PARALLELISM);
    }
}
