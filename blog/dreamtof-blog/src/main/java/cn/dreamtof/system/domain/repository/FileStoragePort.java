package cn.dreamtof.system.domain.repository;

import java.io.InputStream;

public interface FileStoragePort {

    String store(InputStream inputStream, String folder, String storedName);

    void removePhysical(String storagePath);

    String resolveUrl(String folder, String storedName);
}
