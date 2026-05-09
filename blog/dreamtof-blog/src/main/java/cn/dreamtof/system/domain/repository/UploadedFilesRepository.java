package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.model.valueobject.FilePageQuery;

import java.util.List;
import java.util.UUID;

public interface UploadedFilesRepository {

    UploadedFiles create(UploadedFiles entity);

    UploadedFiles update(UploadedFiles entity);

    UploadedFiles getById(UUID id);

    UploadedFiles findByUrl(String url);

    PageResult<UploadedFiles> page(FilePageQuery query);

    boolean existsById(UUID id);

    List<UploadedFiles> listByIds(List<UUID> ids);

    boolean softDeleteById(UUID id);
}
