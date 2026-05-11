package cn.dreamtof.content.domain.service;

import cn.dreamtof.content.domain.model.entity.DiaryEntries;
import cn.dreamtof.content.domain.repository.DiaryEntriesRepository;
import cn.dreamtof.core.exception.Asserts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryDomainService {

    private final DiaryEntriesRepository diaryEntriesRepository;

    public DiaryEntries createDiary(String content, OffsetDateTime entryDate,
                                    String images, String location,
                                    String mood, String tags) {
        DiaryEntries entity = DiaryEntries.create(content, entryDate, images, location, mood, tags);
        return diaryEntriesRepository.create(entity);
    }

    public DiaryEntries updateDiary(UUID id, String content, OffsetDateTime entryDate,
                                    String images, String location,
                                    String mood, String tags) {
        DiaryEntries existing = diaryEntriesRepository.getById(id);
        Asserts.notNull(existing, "日记不存在");
        existing.update(content, entryDate, images, location, mood, tags);
        return diaryEntriesRepository.update(existing);
    }

    public boolean deleteDiary(UUID id) {
        DiaryEntries existing = diaryEntriesRepository.getById(id);
        Asserts.notNull(existing, "日记不存在");
        existing.markDeleted();
        diaryEntriesRepository.update(existing);
        log.info("日记软删除完成, diaryId={}", id);
        return true;
    }

    public List<DiaryEntries> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        Asserts.notNull(startDate, "开始日期不能为空");
        Asserts.notNull(endDate, "结束日期不能为空");
        return diaryEntriesRepository.listByDateRange(startDate, endDate);
    }

    public DiaryEntries getById(UUID id) {
        DiaryEntries entity = diaryEntriesRepository.getById(id);
        Asserts.notNull(entity, "日记不存在");
        return entity;
    }
}
