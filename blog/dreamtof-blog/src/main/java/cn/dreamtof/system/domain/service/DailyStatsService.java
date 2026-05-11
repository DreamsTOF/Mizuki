package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.system.domain.model.entity.DailyStats;
import cn.dreamtof.system.domain.repository.DailyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyStatsService {

    private final DailyStatsRepository dailyStatsRepository;

    public DailyStats rebuildDailyStats(OffsetDateTime statDate) {
        Asserts.notNull(statDate, "统计日期不能为空");
        DailyStats existing = dailyStatsRepository.getByStatDate(statDate);
        if (existing != null) {
            return dailyStatsRepository.update(existing);
        }
        DailyStats entity = new DailyStats();
        entity.setStatDate(statDate);
        return dailyStatsRepository.create(entity);
    }

    public List<DailyStats> getStatsByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        Asserts.notNull(startDate, "开始日期不能为空");
        Asserts.notNull(endDate, "结束日期不能为空");
        return dailyStatsRepository.listByDateRange(startDate, endDate);
    }
}
