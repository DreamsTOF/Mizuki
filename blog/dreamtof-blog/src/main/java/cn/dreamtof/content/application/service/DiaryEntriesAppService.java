package cn.dreamtof.content.application.service;

import cn.dreamtof.content.api.vo.DiaryVO;
import cn.dreamtof.content.application.assembler.DiaryEntriesAssembler;
import cn.dreamtof.content.domain.model.entity.DiaryEntries;
import cn.dreamtof.content.domain.service.DiaryDomainService;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.core.utils.JsonUtils;
import cn.dreamtof.content.domain.repository.DiaryEntriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryEntriesAppService {

    private final DiaryDomainService diaryDomainService;
    private final DiaryEntriesRepository diaryEntriesRepository;
    private final DiaryEntriesAssembler assembler;

    public DiaryVO createDiary(String content, OffsetDateTime entryDate,
                               String images, String location,
                               String mood, String tags) {
        DiaryEntries created = diaryDomainService.createDiary(content, entryDate, images, location, mood, tags);
        log.info("日记创建完成, diaryId={}", created.getId());
        return toVO(created);
    }

    public DiaryVO updateDiary(UUID id, String content, OffsetDateTime entryDate,
                               String images, String location,
                               String mood, String tags) {
        DiaryEntries updated = diaryDomainService.updateDiary(id, content, entryDate, images, location, mood, tags);
        log.info("日记更新完成, diaryId={}", id);
        return toVO(updated);
    }

    public boolean deleteDiary(UUID id) {
        boolean result = diaryDomainService.deleteDiary(id);
        log.info("日记删除完成, diaryId={}", id);
        return result;
    }

    public DiaryVO getById(UUID id) {
        DiaryEntries entity = diaryDomainService.getById(id);
        return toVO(entity);
    }

    public List<DiaryVO> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<DiaryEntries> entities = diaryDomainService.listByDateRange(startDate, endDate);
        return toVOList(entities);
    }

    public PageResult<DiaryVO> pageDiaries(PageReq pageReq) {
        PageResult<DiaryEntries> pageResult = diaryEntriesRepository.page(pageReq);
        List<DiaryVO> voList = toVOList(pageResult.getRecords());
        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPages(), pageResult.getPageNum(), pageResult.getPageSize());
    }

    // ==========================================
    // 手动 VO 转换（字段名与 Entity 不一致，无法依赖 MapStruct 自动映射）
    // ==========================================

    private DiaryVO toVO(DiaryEntries entity) {
        DiaryVO vo = new DiaryVO();
        vo.setId(entity.getId());
        vo.setContent(entity.getContent());
        vo.setDate(entity.getDate() != null
                ? DateUtils.format(entity.getDate().toLocalDateTime()) : null);
        vo.setImages(JsonUtils.parseList(entity.getImages(), String.class));
        vo.setLocation(entity.getLocation());
        vo.setMood(entity.getMood());
        vo.setTags(JsonUtils.parseList(entity.getTags(), String.class));
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private List<DiaryVO> toVOList(List<DiaryEntries> entities) {
        return entities.stream().map(this::toVO).toList();
    }
}
