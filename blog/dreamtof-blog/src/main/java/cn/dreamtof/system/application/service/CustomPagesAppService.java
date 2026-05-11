package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.api.vo.CustomPageVO;
import cn.dreamtof.system.application.assembler.CustomPagesAssembler;
import cn.dreamtof.system.domain.model.entity.CustomPages;
import cn.dreamtof.system.domain.repository.CustomPagesRepository;
import cn.dreamtof.system.domain.service.CustomPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomPagesAppService {

    private final CustomPagesRepository repository;
    private final CustomPageService customPageService;
    private final CustomPagesAssembler assembler;

    public CustomPageVO create(String pageKey, String title, String content,
                               String description, String coverImage,
                               Boolean hasCommentEnabled, Boolean hasEnabled) {
        CustomPages entity = CustomPages.create(pageKey, title, content, description, coverImage, hasCommentEnabled, hasEnabled);
        CustomPages created = customPageService.create(entity);
        return assembler.toVO(created);
    }

    public CustomPageVO update(UUID id, String title, String content,
                               String description, String coverImage,
                               Boolean hasCommentEnabled, Boolean hasEnabled) {
        CustomPages entity = customPageService.getById(id);
        entity.updateContent(title, content, description, coverImage, hasCommentEnabled, hasEnabled);
        CustomPages updated = repository.update(entity);
        return assembler.toVO(updated);
    }

    public boolean removeById(UUID id) {
        return customPageService.removeById(id);
    }

    public CustomPageVO getById(UUID id) {
        CustomPages entity = customPageService.getById(id);
        return assembler.toVO(entity);
    }

    public CustomPageVO getByPageKey(String pageKey) {
        CustomPages entity = customPageService.getByPageKey(pageKey);
        return assembler.toVO(entity);
    }

    public List<CustomPageVO> listAll() {
        List<CustomPages> entities = customPageService.listAll();
        return assembler.toVOList(entities);
    }

    public List<CustomPageVO> listEnabled() {
        List<CustomPages> entities = customPageService.listEnabled();
        return assembler.toVOList(entities);
    }

    public PageResult<CustomPageVO> page(PageReq pageReq) {
        PageResult<CustomPages> result = customPageService.page(pageReq);
        List<CustomPageVO> voList = assembler.toVOList(result.getRecords());
        return PageResult.of(voList, result.getTotal(), result.getPages(), result.getPageNum(), result.getPageSize());
    }

    public CursorResult<CustomPageVO> seek(UUID cursor, int limit) {
        CursorResult<CustomPages> result = repository.seek(cursor, limit);
        List<CustomPageVO> voList = assembler.toVOList(result.getRecords());
        return new CursorResult<>(voList, result.getNextCursor(), result.isHasNext());
    }
}
