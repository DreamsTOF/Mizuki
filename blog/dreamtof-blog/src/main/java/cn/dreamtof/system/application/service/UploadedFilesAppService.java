package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.api.request.UploadedFilesPageReq;
import cn.dreamtof.system.api.vo.UploadedFileVO;
import cn.dreamtof.system.application.assembler.UploadedFileVOAssembler;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.model.enums.UploadFolder;
import cn.dreamtof.system.domain.model.errorcode.FileUploadErrorCode;
import cn.dreamtof.system.domain.model.valueobject.FilePageQuery;
import cn.dreamtof.system.domain.model.valueobject.FileUploadContext;
import cn.dreamtof.system.domain.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadedFilesAppService {

    private final FileUploadService fileUploadService;
    private final UploadedFileVOAssembler voAssembler;

    public UploadedFileVO upload(MultipartFile multipartFile, String folderCode) {
        Asserts.notNull(multipartFile, "上传文件不能为空");
        Asserts.isTrue(!multipartFile.isEmpty(), "上传文件不能为空");

        UploadFolder folder = UploadFolder.findByCode(folderCode);
        Asserts.notNull(folder, FileUploadErrorCode.INVALID_FOLDER);

        try {
            FileUploadContext context = new FileUploadContext(
                    multipartFile.getInputStream(),
                    multipartFile.getOriginalFilename(),
                    multipartFile.getContentType(),
                    multipartFile.getSize()
            );
            UploadedFiles entity = fileUploadService.upload(context, folder);
            return voAssembler.toVO(entity);
        } catch (IOException e) {
            throw new cn.dreamtof.core.exception.BusinessException(FileUploadErrorCode.UPLOAD_FAILED, e.getMessage());
        }
    }

    public List<UploadedFileVO> uploadBatch(MultipartFile[] multipartFiles, String folderCode) {
        Asserts.notNull(multipartFiles, "上传文件不能为空");

        UploadFolder folder = UploadFolder.findByCode(folderCode);
        Asserts.notNull(folder, FileUploadErrorCode.INVALID_FOLDER);

        List<FileUploadContext> contexts = new ArrayList<>();
        for (MultipartFile mf : multipartFiles) {
            if (mf != null && !mf.isEmpty()) {
                try {
                    contexts.add(new FileUploadContext(
                            mf.getInputStream(),
                            mf.getOriginalFilename(),
                            mf.getContentType(),
                            mf.getSize()
                    ));
                } catch (IOException e) {
                    log.warn("Failed to read multipart file: {}", mf.getOriginalFilename());
                }
            }
        }

        List<UploadedFiles> entities = fileUploadService.uploadBatch(contexts, folder);
        return voAssembler.toVOList(entities);
    }

    public void deleteById(UUID id) {
        fileUploadService.deleteById(id);
    }

    public void deleteByUrl(String url) {
        fileUploadService.deleteByUrl(url);
    }

    public PageResult<UploadedFileVO> pageByFolder(UploadedFilesPageReq pageReq) {
        UploadFolder folder = UploadFolder.findByCode(pageReq.getFolder());
        FilePageQuery query = new FilePageQuery(pageReq.getPageNum(), pageReq.getPageSize(), folder);
        PageResult<UploadedFiles> entityPage = fileUploadService.pageByFolder(query);
        List<UploadedFileVO> voList = voAssembler.toVOList(entityPage.getRecords());
        return PageResult.of(voList, entityPage.getTotal(), entityPage.getPages(), entityPage.getPageNum(), entityPage.getPageSize());
    }

    public UploadedFileVO getDetail(UUID id) {
        UploadedFiles entity = fileUploadService.findById(id);
        Asserts.notNull(entity, FileUploadErrorCode.FILE_NOT_FOUND);
        return voAssembler.toVO(entity);
    }
}
