package cn.dreamtof.system.infrastructure.storage;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.enums.UploadFolder;
import cn.dreamtof.system.domain.service.FileStorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@Component
@Slf4j
public class LocalFileStorageProvider implements FileStorageProvider {

    @Value("${mizuki.storage.local.base-path:./uploads}")
    private String basePath;

    @Value("${mizuki.storage.local.url-prefix:/uploads}")
    private String urlPrefix;

    private static final int THUMBNAIL_WIDTH = 200;

    @Override
    public String store(UploadFolder folder, String storedName, byte[] content) {
        try {
            Path dirPath = Paths.get(basePath, folder.getPath());
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(storedName);
            Files.write(filePath, content);
            return filePath.toString();
        } catch (IOException e) {
            log.error("文件存储失败: folder={}, storedName={}", folder.getPath(), storedName, e);
            Asserts.fail(cn.dreamtof.system.domain.model.enums.FileErrorCode.FILE_UPLOAD_FAILED);
            return null;
        }
    }

    @Override
    public byte[] readBytes(String storagePath) {
        try {
            return Files.readAllBytes(Paths.get(storagePath));
        } catch (IOException e) {
            log.error("文件读取失败: storagePath={}", storagePath, e);
            throw new cn.dreamtof.core.exception.BusinessException(cn.dreamtof.system.domain.model.enums.FileErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public boolean deletePhysical(String storagePath) {
        try {
            Path path = Paths.get(storagePath);
            boolean deleted = Files.deleteIfExists(path);
            Path thumbDir = path.getParent().resolve("thumbnail");
            Path thumbPath = thumbDir.resolve("thumb_" + path.getFileName());
            Files.deleteIfExists(thumbPath);
            return deleted;
        } catch (IOException e) {
            log.error("文件物理删除失败: storagePath={}", storagePath, e);
            return false;
        }
    }

    @Override
    public String generateUrl(UploadFolder folder, String storedName) {
        return urlPrefix + "/" + folder.getPath() + "/" + storedName;
    }

    @Override
    public String generateThumbnail(UploadFolder folder, String storedName, byte[] content, int width, int height) {
        if (!folder.isImageExtension(extractExtension(storedName))) {
            return null;
        }
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(content);
            BufferedImage original = ImageIO.read(bais);
            if (original == null) {
                return null;
            }
            int thumbHeight = (int) ((double) THUMBNAIL_WIDTH / width * height);
            BufferedImage thumbnail = new BufferedImage(THUMBNAIL_WIDTH, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumbnail.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, THUMBNAIL_WIDTH, thumbHeight, null);
            g.dispose();

            Path thumbDir = Paths.get(basePath, folder.getPath(), "thumbnail");
            Files.createDirectories(thumbDir);
            Path thumbPath = thumbDir.resolve("thumb_" + storedName);

            String formatName = extractExtension(storedName);
            if ("jpg".equals(formatName)) {
                formatName = "jpeg";
            }
            writeImage(thumbnail, formatName, thumbPath);
            return urlPrefix + "/" + folder.getPath() + "/thumbnail/thumb_" + storedName;
        } catch (Exception e) {
            log.warn("缩略图生成失败: folder={}, storedName={}", folder.getPath(), storedName, e);
            return null;
        }
    }

    private void writeImage(BufferedImage image, String formatName, Path path) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        if (!writers.hasNext()) {
            ImageIO.write(image, "png", path.toFile());
            return;
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(path.toFile())) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT || param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.8f);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}
