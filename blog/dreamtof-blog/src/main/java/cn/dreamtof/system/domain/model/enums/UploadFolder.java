package cn.dreamtof.system.domain.model.enums;

import cn.dreamtof.core.exception.Asserts;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum UploadFolder {

    POSTS("posts", Set.of("jpg", "jpeg", "png", "webp", "gif"), 5 * 1024 * 1024),
    DIARY("diary", Set.of("jpg", "jpeg", "png", "webp", "gif"), 3 * 1024 * 1024),
    ALBUMS("albums", Set.of("jpg", "jpeg", "png", "webp", "gif"), 5 * 1024 * 1024),
    DEVICES("devices", Set.of("jpg", "jpeg", "png", "webp", "gif"), 5 * 1024 * 1024),
    AVATARS("avatars", Set.of("jpg", "jpeg", "png", "webp"), 2 * 1024 * 1024),
    ASSETS("assets", Set.of("jpg", "jpeg", "png", "webp", "svg", "ico"), 10 * 1024 * 1024);

    private final String path;
    private final Set<String> allowedExtensions;
    private final long maxSizeBytes;

    private static final Map<String, UploadFolder> PATH_MAP = Map.of(
            "posts", POSTS,
            "diary", DIARY,
            "albums", ALBUMS,
            "devices", DEVICES,
            "avatars", AVATARS,
            "assets", ASSETS
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private static final Map<String, Set<String>> EXTENSION_MIME_MAP = Map.of(
            "jpg", Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "png", Set.of("image/png"),
            "webp", Set.of("image/webp"),
            "gif", Set.of("image/gif"),
            "svg", Set.of("image/svg+xml"),
            "ico", Set.of("image/x-icon", "image/vnd.microsoft.icon")
    );

    private static final Map<String, byte[]> MAGIC_NUMBERS = Map.of(
            "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8},
            "jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8},
            "png", new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47},
            "gif", new byte[]{(byte) 0x47, (byte) 0x49, (byte) 0x46},
            "webp", new byte[]{(byte) 0x52, (byte) 0x49, (byte) 0x46},
            "svg", new byte[]{(byte) 0x3C},
            "ico", new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00}
    );

    public static UploadFolder fromPath(String path) {
        UploadFolder folder = PATH_MAP.get(path);
        Asserts.notNull(folder, "无效的目标目录: " + path);
        return folder;
    }

    public boolean isExtensionAllowed(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    public boolean isSizeAllowed(long fileSize) {
        return fileSize <= maxSizeBytes && fileSize > 0;
    }

    public boolean isImageFolder() {
        return this != ASSETS;
    }

    public boolean isMimeTypeAllowed(String mimeType, String extension) {
        Set<String> allowedMimes = EXTENSION_MIME_MAP.get(extension.toLowerCase());
        if (allowedMimes == null) {
            return false;
        }
        return allowedMimes.stream().anyMatch(mimeType::startsWith);
    }

    public boolean isImageExtension(String extension) {
        return IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    public static byte[] getMagicNumber(String extension) {
        return MAGIC_NUMBERS.get(extension.toLowerCase());
    }

    public static String extractExtension(String originalName) {
        Asserts.notBlank(originalName, "文件名不能为空");
        int dotIndex = originalName.lastIndexOf('.');
        Asserts.isTrue(dotIndex > 0, "文件名必须包含扩展名");
        return originalName.substring(dotIndex + 1).toLowerCase();
    }
}
