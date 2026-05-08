package cn.dreamtof.core.base;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

// 创作时间能力
public interface CreatedTimeAudit {
    void setCreatedAt(OffsetDateTime time);

}

