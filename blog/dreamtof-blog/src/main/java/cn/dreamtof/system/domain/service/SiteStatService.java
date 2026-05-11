package cn.dreamtof.system.domain.service;

import cn.dreamtof.system.api.vo.SiteStatVO;
import cn.dreamtof.system.domain.repository.PageViewsRepository;
import cn.dreamtof.system.domain.repository.DailyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteStatService {

    private final PageViewsRepository pageViewsRepository;
    private final DailyStatsRepository dailyStatsRepository;

    public SiteStatVO getSiteStats() {
        SiteStatVO vo = new SiteStatVO();
        vo.setTotalPageViews(pageViewsRepository.listAll().size());
        log.info("站点统计查询完成, totalPageViews={}", vo.getTotalPageViews());
        return vo;
    }
}
