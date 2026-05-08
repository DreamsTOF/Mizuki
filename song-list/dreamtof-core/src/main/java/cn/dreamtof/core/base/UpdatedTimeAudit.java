package cn.dreamtof.core.base;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public interface UpdatedTimeAudit {
    void setUpdatedAt(OffsetDateTime time);

}
