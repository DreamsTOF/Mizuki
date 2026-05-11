package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.system.domain.model.entity.PageViews;
import cn.dreamtof.system.domain.repository.PageViewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final PageViewsRepository pageViewsRepository;

    public PageViews recordPageView(PageViews entity) {
        Asserts.notNull(entity, "页面访问实体不能为空");
        Asserts.notBlank(entity.getPagePath(), "页面路径不能为空");
        return pageViewsRepository.create(entity);
    }

    public List<PageViews> getTopPages(int limit) {
        return pageViewsRepository.listTopPages(limit);
    }

    public long getPageViewCount(String pagePath) {
        return pageViewsRepository.countByPagePath(pagePath);
    }
}
