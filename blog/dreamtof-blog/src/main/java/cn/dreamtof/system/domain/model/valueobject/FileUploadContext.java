package cn.dreamtof.system.domain.model.valueobject;

import java.io.InputStream;

public record FileUploadContext(
    InputStream inputStream,
    String originalName,
    String mimeType,
    long fileSize
) {}
