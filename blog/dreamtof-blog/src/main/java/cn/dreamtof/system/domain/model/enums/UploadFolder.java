package cn.dreamtof.system.domain.model.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

import java.util.Set;

@Getter
public enum UploadFolder {

    POSTS("posts", Set.of("jpg", "jpeg", "png", "webp", "gif"), 5 * 1024 * 1024),
    ALBUMS("albums", Set.of("jpg", "jpeg", "png", "webp", "gif"), 5 * 1024 * 1024),
    DIARY("diary", Set.of("jpg", "jpeg", "png", "webp", "gif"), 3 * 1024 * 1024),
    AVATARS("avatars", Set.of("jpg", "jpeg", "png", "webp"), 2 * 1024 * 1024),
    ASSETS("assets", Set.of("jpg", "jpeg", "png", "webp", "svg", "ico"), 10 * 1024 * 1024);

    @EnumValue
    private final String code;
    private final Set<String> allowedExtensions;
    private final long maxSizeBytes;

    UploadFolder(String code, Set<String> allowedExtensions, long maxSizeBytes) {
        this.code = code;
        this.allowedExtensions = allowedExtensions;
        this.maxSizeBytes = maxSizeBytes;
    }

    public static UploadFolder findByCode(String code) {
        if (code == null) return null;
        for (UploadFolder f : values()) {
            if (f.code.equalsIgnoreCase(code)) return f;
        }
        return null;
    }

    public boolean isExtensionAllowed(String extension) {
        return extension != null && allowedExtensions.contains(extension.toLowerCase());
    }

    public boolean isSizeAllowed(long sizeBytes) {
        return sizeBytes > 0 && sizeBytes <= maxSizeBytes;
    }

    public String getStorageSubPath() {
        return code;
    }
}
