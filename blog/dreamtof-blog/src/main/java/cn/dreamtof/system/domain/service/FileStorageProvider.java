package cn.dreamtof.system.domain.service;

import cn.dreamtof.system.domain.model.enums.UploadFolder;

public interface FileStorageProvider {

    String store(UploadFolder folder, String storedName, byte[] content);

    byte[] readBytes(String storagePath);

    boolean deletePhysical(String storagePath);

    String generateUrl(UploadFolder folder, String storedName);

    String generateThumbnail(UploadFolder folder, String storedName, byte[] content, int width, int height);
}
