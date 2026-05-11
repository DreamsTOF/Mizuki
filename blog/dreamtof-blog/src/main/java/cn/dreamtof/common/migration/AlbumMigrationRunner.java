package cn.dreamtof.common.migration;

import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.core.utils.JsonUtils;
import cn.dreamtof.media.infrastructure.persistence.mapper.AlbumPhotosMapper;
import cn.dreamtof.media.infrastructure.persistence.mapper.AlbumsMapper;
import cn.dreamtof.media.infrastructure.persistence.po.AlbumPhotosPO;
import cn.dreamtof.media.infrastructure.persistence.po.AlbumsPO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 相册文件系统迁移工具
 * <p>
 * 将前端构建时扫描的 {@code public/images/albums/} 目录下的相册数据
 * 导入到数据库中。通过启动参数 {@code --migrate-albums} 触发。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlbumMigrationRunner implements ApplicationRunner {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp",
            ".svg", ".avif", ".bmp", ".tiff", ".tif"
    );

    private static final Map<String, String> MIME_MAP = new LinkedHashMap<>();

    static {
        MIME_MAP.put(".jpg", "image/jpeg");
        MIME_MAP.put(".jpeg", "image/jpeg");
        MIME_MAP.put(".png", "image/png");
        MIME_MAP.put(".gif", "image/gif");
        MIME_MAP.put(".webp", "image/webp");
        MIME_MAP.put(".svg", "image/svg+xml");
        MIME_MAP.put(".avif", "image/avif");
        MIME_MAP.put(".bmp", "image/bmp");
        MIME_MAP.put(".tiff", "image/tiff");
        MIME_MAP.put(".tif", "image/tiff");
    }

    // 封面文件名（含扩展名）
    private static final Set<String> COVER_FILENAMES = Set.of("cover.jpg", "cover.webp");

    private final AlbumsMapper albumsMapper;
    private final AlbumPhotosMapper albumPhotosMapper;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("migrate-albums")) {
            return;
        }

        log.info("====== 开始相册数据迁移 ======");

        // 计算前端 albums 目录路径：相对于 user.dir（即 dreamtof-blog 目录）向上两级到项目根
        String albumsDir = Paths.get(System.getProperty("user.dir"))
                .resolve("../Mizuki/public/images/albums")
                .normalize()
                .toString();

        File dir = new File(albumsDir);
        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("相册目录不存在: {}", albumsDir);
            log.warn("请确保在项目根目录（Mizuki/）下运行，当前 user.dir={}", System.getProperty("user.dir"));
            return;
        }

        File[] folders = dir.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            log.warn("相册目录下没有子文件夹: {}", albumsDir);
            return;
        }

        int totalAlbums = 0;
        int totalPhotos = 0;

        for (File folder : folders) {
            // 跳过 README 等非相册目录
            if (folder.getName().startsWith(".")) {
                continue;
            }
            try {
                AlbumMigrationResult result = migrateAlbum(folder);
                if (result != null) {
                    totalAlbums++;
                    totalPhotos += result.photoCount;
                }
            } catch (Exception e) {
                log.error("  相册 [{}] 迁移失败: {}", folder.getName(), e.getMessage(), e);
            }
        }

        log.info("====== 相册数据迁移完成: {} 个相册, {} 张照片 ======", totalAlbums, totalPhotos);
    }

    /**
     * 迁移单个相册文件夹
     */
    private AlbumMigrationResult migrateAlbum(File folder) throws IOException {
        String albumKey = folder.getName();
        Path folderPath = folder.toPath();

        // 1. 读取并解析 info.json
        Path infoPath = folderPath.resolve("info.json");
        if (!Files.exists(infoPath)) {
            log.warn("  相册 [{}] 缺少 info.json，跳过", albumKey);
            return null;
        }

        String infoContent = Files.readString(infoPath);
        JsonNode infoJson;
        try {
            infoJson = JsonUtils.parseTree(infoContent);
        } catch (Exception e) {
            log.warn("  相册 [{}] 的 info.json 格式错误: {}", albumKey, e.getMessage());
            return null;
        }

        if (infoJson == null) {
            return null;
        }

        // 2. 跳过隐藏相册
        JsonNode hiddenNode = infoJson.get("hidden");
        if (hiddenNode != null && hiddenNode.asBoolean()) {
            log.info("  相册 [{}] 已设置为隐藏，跳过迁移", albumKey);
            return null;
        }

        // 3. 跳过外链模式相册（无本地文件）
        JsonNode modeNode = infoJson.get("mode");
        if (modeNode != null && "external".equals(modeNode.asText())) {
            log.info("  相册 [{}] 为外链模式，跳过迁移", albumKey);
            return null;
        }

        // 4. 检查是否已存在（防止重复迁移）
        AlbumsPO existing = albumsMapper.selectOneByQuery(
                QueryWrapper.create().eq(AlbumsPO::getAlbumKey, albumKey)
        );
        if (existing != null) {
            log.info("  相册 [{}] 已存在于数据库中，跳过", albumKey);
            return null;
        }

        // 5. 解析封面
        String coverUrl = resolveCover(folderPath, albumKey);

        // 6. 构建 AlbumsPO
        AlbumsPO album = buildAlbum(albumKey, infoJson, coverUrl);
        albumsMapper.insert(album);

        // 7. 扫描图片文件并创建 AlbumPhotosPO
        List<AlbumPhotosPO> photos = scanPhotos(folderPath, albumKey, album.getId());
        if (!photos.isEmpty()) {
            for (AlbumPhotosPO photo : photos) {
                albumPhotosMapper.insert(photo);
            }
        }

        log.debug("  相册 [{}] -> {} 张照片, 封面={}", albumKey, photos.size(), coverUrl);
        AlbumMigrationResult result = new AlbumMigrationResult();
        result.albumTitle = album.getTitle();
        result.photoCount = photos.size();
        return result;
    }

    /**
     * 解析封面图片路径，优先使用 webp 格式
     */
    private String resolveCover(Path folderPath, String albumKey) {
        Path webpCover = folderPath.resolve("cover.webp");
        if (Files.exists(webpCover)) {
            return "/images/albums/" + albumKey + "/cover.webp";
        }
        Path jpgCover = folderPath.resolve("cover.jpg");
        if (Files.exists(jpgCover)) {
            return "/images/albums/" + albumKey + "/cover.jpg";
        }
        return null;
    }

    /**
     * 构建相册持久化对象
     */
    private AlbumsPO buildAlbum(String albumKey, JsonNode infoJson, String coverUrl) {
        AlbumsPO.AlbumsPOBuilder builder = AlbumsPO.builder()
                .albumKey(albumKey)
                .title(getJsonText(infoJson, "title", albumKey))
                .description(getJsonText(infoJson, "description", ""))
                .location(getJsonText(infoJson, "location", ""))
                .layout(getJsonText(infoJson, "layout", "grid"))
                .cover(coverUrl);

        // 解析 tags
        JsonNode tagsNode = infoJson.get("tags");
        if (tagsNode instanceof ArrayNode tagsArray && tagsArray.size() > 0) {
            List<String> tagList = new ArrayList<>();
            for (JsonNode tag : tagsArray) {
                tagList.add(tag.asText());
            }
            // 序列化为 JSON 字符串，UniversalJsonTypeHandler 会处理 String 类型
            builder.tags(JsonUtils.toJsonString(tagList));
        }

        // 解析 columns
        JsonNode columnsNode = infoJson.get("columns");
        if (columnsNode != null && columnsNode.isInt()) {
            builder.columns(columnsNode.asInt());
        }

        // 解析 date
        JsonNode dateNode = infoJson.get("date");
        if (dateNode != null && !dateNode.asText().isBlank()) {
            try {
                OffsetDateTime date = LocalDate.parse(dateNode.asText(), DateTimeFormatter.ISO_LOCAL_DATE)
                        .atStartOfDay(DEFAULT_ZONE)
                        .toOffsetDateTime();
                builder.date(date);
            } catch (Exception e) {
                log.warn("  相册 [{}] 日期格式解析失败: {}", albumKey, dateNode.asText());
            }
        }

        OffsetDateTime now = OffsetDateTime.now(DEFAULT_ZONE);
        return builder
                .version(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 扫描文件夹中的图片文件，排除封面
     */
    private List<AlbumPhotosPO> scanPhotos(Path folderPath, String albumKey, UUID albumId) throws IOException {
        List<AlbumPhotosPO> photos = new ArrayList<>();

        File[] files = folderPath.toFile().listFiles();
        if (files == null) {
            return photos;
        }

        // 收集所有合法图片文件（排除封面）
        List<File> imageFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            String fileName = file.getName().toLowerCase();
            String ext = getExtension(fileName);
            if (!IMAGE_EXTENSIONS.contains(ext)) {
                continue;
            }
            if (COVER_FILENAMES.contains(fileName)) {
                continue;
            }
            imageFiles.add(file);
        }

        // 排序以确保一致的迁移顺序
        imageFiles.sort(Comparator.comparing(File::getName));

        OffsetDateTime now = OffsetDateTime.now(DEFAULT_ZONE);

        for (File imageFile : imageFiles) {
            String fileName = imageFile.getName();
            String ext = getExtension(fileName);

            AlbumPhotosPO photo = AlbumPhotosPO.builder()
                    .albumId(albumId)
                    .filename(fileName)
                    .url("/images/albums/" + albumKey + "/" + fileName)
                    .size(Files.size(imageFile.toPath()))
                    .mimeType(MIME_MAP.getOrDefault(ext, "application/octet-stream"))
                    .hasCover(false)
                    .version(0)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            photos.add(photo);
        }

        return photos;
    }

    /**
     * 获取文件扩展名（小写，含点号）
     */
    private static String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex < 0 ? "" : fileName.substring(dotIndex);
    }

    /**
     * 从 JsonNode 安全获取文本字段
     */
    private static String getJsonText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        String text = value.asText();
        return text != null ? text : defaultValue;
    }

    /**
     * 迁移结果 DTO
     */
    private static class AlbumMigrationResult {
        String albumTitle;
        int photoCount;
    }
}
