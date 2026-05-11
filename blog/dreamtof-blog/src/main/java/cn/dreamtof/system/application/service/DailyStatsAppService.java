package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.DailyStats;
import cn.dreamtof.system.domain.repository.DailyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyStatsAppService {

    private final DailyStatsRepository repository;

    public DailyStats create(DailyStats entity) {
        return repository.create(entity);
    }

    public DailyStats update(DailyStats entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public DailyStats getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<DailyStats> listAll() {
        return repository.listAll();
    }

    public PageResult<DailyStats> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<DailyStats> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<DailyStats> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        return repository.listByDateRange(startDate, endDate);
    }
}
