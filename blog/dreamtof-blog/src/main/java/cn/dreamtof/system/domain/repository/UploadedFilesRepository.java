package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;

import java.util.List;
import java.util.UUID;

public interface UploadedFilesRepository {

    UploadedFiles create(UploadedFiles entity);

    boolean removeById(UUID id);

    UploadedFiles update(UploadedFiles entity);

    UploadedFiles getById(UUID id);

    List<UploadedFiles> listAll();

    PageResult<UploadedFiles> pageByFolder(String folder, PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<UploadedFiles> entities);

    boolean existsById(UUID id);

    List<UploadedFiles> listByIds(List<UUID> ids);

    CursorResult<UploadedFiles> seek(UUID cursor, int limit);

    UploadedFiles findByUrl(String url);
}
