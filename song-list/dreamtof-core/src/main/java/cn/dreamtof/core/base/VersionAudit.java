package cn.dreamtof.core.base;

// 乐观锁版本能力
public interface VersionAudit {
    void setVersion(Integer version);
}
