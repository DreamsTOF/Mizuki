package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.api.vo.UploadedFileVO;
import cn.dreamtof.system.application.assembler.UploadedFilesAssembler;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.model.enums.UploadFolder;
import cn.dreamtof.system.domain.repository.UploadedFilesRepository;
import cn.dreamtof.system.domain.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadedFilesAppService {

    private final UploadedFilesRepository repository;
    private final FileUploadService fileUploadService;
    private final UploadedFilesAssembler assembler;

    public UploadedFileVO upload(MultipartFile file, String folder) {
        UploadFolder uploadFolder = UploadFolder.fromPath(folder);
        UploadedFiles entity = fileUploadService.upload(file, uploadFolder);
        return assembler.toVO(entity);
    }

    public List<UploadedFileVO> batchUpload(List<MultipartFile> files, String folder) {
        UploadFolder uploadFolder = UploadFolder.fromPath(folder);
        List<UploadedFiles> entities = fileUploadService.batchUpload(files, uploadFolder);
        return assembler.toVOList(entities);
    }

    public boolean removeById(UUID id) {
        return fileUploadService.deleteFile(id);
    }

    public boolean removeByIds(List<UUID> ids) {
        for (UUID id : ids) {
            fileUploadService.deleteFile(id);
        }
        return true;
    }

    public UploadedFileVO getDetail(UUID id) {
        UploadedFiles entity = repository.getById(id);
        return assembler.toVO(entity);
    }

    public PageResult<UploadedFileVO> pageByFolder(String folder, int pageNum, int pageSize) {
        cn.dreamtof.core.base.PageReq pageReq = new cn.dreamtof.core.base.PageReq();
        pageReq.setPageNum(pageNum);
        pageReq.setPageSize(pageSize);
        PageResult<UploadedFiles> result = fileUploadService.pageByFolder(folder, pageReq);
        List<UploadedFileVO> voList = assembler.toVOList(result.getRecords());
        return PageResult.of(voList, result.getTotal(), result.getPages(), result.getPageNum(), result.getPageSize());
    }

    public CursorResult<UploadedFileVO> seek(UUID cursor, int limit) {
        CursorResult<UploadedFiles> result = repository.seek(cursor, limit);
        List<UploadedFileVO> voList = assembler.toVOList(result.getRecords());
        return new CursorResult<>(voList, result.getNextCursor(), result.isHasNext());
    }
}
