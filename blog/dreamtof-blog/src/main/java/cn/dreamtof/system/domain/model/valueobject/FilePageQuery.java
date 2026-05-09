package cn.dreamtof.system.domain.model.valueobject;

import cn.dreamtof.system.domain.model.enums.UploadFolder;

public record FilePageQuery(
    int pageNum,
    int pageSize,
    UploadFolder folder
) {}
