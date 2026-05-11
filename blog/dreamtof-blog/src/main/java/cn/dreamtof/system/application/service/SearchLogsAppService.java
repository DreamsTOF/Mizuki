package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.SearchLogs;
import cn.dreamtof.system.domain.repository.SearchLogsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchLogsAppService {

    private final SearchLogsRepository repository;

    public SearchLogs create(SearchLogs entity) {
        return repository.create(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public SearchLogs getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<SearchLogs> listAll() {
        return repository.listAll();
    }

    public PageResult<SearchLogs> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<SearchLogs> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<Object[]> getHotKeywords(int limit) {
        return repository.getHotKeywords(limit);
    }
}
