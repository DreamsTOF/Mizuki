package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.enums.FileErrorCode;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.repository.UploadedFilesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileRecordService {

    private final UploadedFilesRepository repository;

    public UploadedFiles getById(UUID id) {
        UploadedFiles entity = repository.getById(id);
        Asserts.notNull(entity, FileErrorCode.FILE_RECORD_NOT_FOUND);
        return entity;
    }

    public PageResult<UploadedFiles> pageByFolder(String folder, PageReq pageReq) {
        return repository.pageByFolder(folder, pageReq);
    }

    public List<UploadedFiles> listByIds(List<UUID> ids) {
        return repository.listByIds(ids);
    }

    public UploadedFiles findByUrl(String url) {
        return repository.findByUrl(url);
    }

    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    public long countByFolder(String folder) {
        PageReq pageReq = new PageReq();
        pageReq.setPageNum(1);
        pageReq.setPageSize(1);
        PageResult<UploadedFiles> result = repository.pageByFolder(folder, pageReq);
        return result.getTotal();
    }
}
