package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.model.enums.UploadFolder;
import cn.dreamtof.system.domain.model.valueobject.FilePageQuery;
import cn.dreamtof.system.domain.model.valueobject.FileUploadContext;

import java.util.List;
import java.util.UUID;

public interface FileUploadService {

    UploadedFiles upload(FileUploadContext context, UploadFolder folder);

    List<UploadedFiles> uploadBatch(List<FileUploadContext> files, UploadFolder folder);

    void deleteById(UUID id);

    void deleteByUrl(String url);

    PageResult<UploadedFiles> pageByFolder(FilePageQuery query);

    UploadedFiles findById(UUID id);

    UploadedFiles findByUrl(String url);
}
