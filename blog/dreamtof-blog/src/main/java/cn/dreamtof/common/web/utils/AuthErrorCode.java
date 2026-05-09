package cn.dreamtof.common.web.utils;

import cn.dreamtof.core.exception.ErrorCode;

/**
 * Auth 模块错误码
 * <p>
 * 401xx 段：认证与加密相关错误码（与前端 api.ts 约定）
 * - 40100: Token 过期/登录失效 → 前端跳转登录页
 * - 40101: 加密密钥过期 → 前端清除密钥并重新获取
 * </p>
 */
public class AuthErrorCode extends ErrorCode {

    // 40100: 基础 Token 过期，需要重回 10M 时间锁登录流程
    public static final AuthErrorCode TOKEN_EXPIRED = new AuthErrorCode(40100, "登录已过期，请重新登录");

    // 40101: 物理熔断。旧链已彻底失效（超过 random 窗口），前端必须触发重同步
    public static final AuthErrorCode CRYPTO_KEY_EXPIRED = new AuthErrorCode(40101, "安全会话已断开，正在自动重连");

    // 40102: 进化压力。由于概率熔断导致，计数器已在后端推进，前端必须单调递增 Counter 并立即触发进化
    public static final AuthErrorCode CRYPTO_EVOLUTION_PRESSURE = new AuthErrorCode(40102, "安全等级提升中，请重试");

    // 40103: 重放攻击/计数器过期。收到了已使用的 Counter，或者乱序程度超出了历史缓冲区
    public static final AuthErrorCode CRYPTO_STALE_COUNTER = new AuthErrorCode(40103, "安全校验失败，请求序列异常");

    // 40104: 布局定位失败。在 10 个 Header 中找不到合法的指纹，可能是日期参考头或密钥不匹配
    public static final AuthErrorCode CRYPTO_LAYOUT_ERROR = new AuthErrorCode(40104, "安全环境异常，请检查本地时钟");

    // 40105: 完整性校验失败。找到了 Counter 但解密 Body 失败，通常意味着 MSK 状态在多端发生了不可逆的撕裂
    public static final AuthErrorCode CRYPTO_INTEGRITY_FAILED = new AuthErrorCode(40105, "数据完整性校验失败");

    // 40106: 加密版本不匹配。客户端 Header 数量不足 16 个，需要升级客户端
    public static final AuthErrorCode CRYPTO_VERSION_MISMATCH = new AuthErrorCode(40106, "加密协议版本不匹配，请刷新页面");

    // 40107: ZKP 验证失败。Schnorr 证明无效或公钥不匹配，断开连接
    public static final AuthErrorCode ZKP_VERIFICATION_FAILED = new AuthErrorCode(40107, "安全验证失败，正在重新连接");

    // 40108: 挑战响应超时。挑战 R 已过期，客户端未在下一个请求中提交证明
    public static final AuthErrorCode CHALLENGE_TIMEOUT = new AuthErrorCode(40108, "安全挑战超时，正在重新连接");

    // 40109: 挑战响应不匹配。客户端提交的挑战证明与服务端计算值不一致
    public static final AuthErrorCode CHALLENGE_MISMATCH = new AuthErrorCode(40109, "安全挑战验证失败，正在重新连接");

    protected AuthErrorCode(int code, String message) {
        super(code, message);
    }
}
