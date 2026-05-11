package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.entity.SearchLogs;
import cn.dreamtof.system.domain.repository.SearchLogsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchLogService {

    private final SearchLogsRepository searchLogsRepository;

    public SearchLogs recordSearch(SearchLogs entity) {
        Asserts.notNull(entity, "搜索记录实体不能为空");
        Asserts.notBlank(entity.getKeyword(), "搜索关键词不能为空");
        return searchLogsRepository.create(entity);
    }

    public List<Object[]> getHotKeywords(int limit) {
        return searchLogsRepository.getHotKeywords(limit);
    }
}
