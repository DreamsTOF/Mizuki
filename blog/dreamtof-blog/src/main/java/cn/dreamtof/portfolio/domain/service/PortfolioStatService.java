package cn.dreamtof.portfolio.domain.service;

import cn.dreamtof.portfolio.api.vo.PortfolioStatVO;
import cn.dreamtof.portfolio.domain.repository.ProjectsRepository;
import cn.dreamtof.portfolio.domain.repository.SkillsRepository;
import cn.dreamtof.portfolio.domain.repository.TimelineEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioStatService {

    private final ProjectsRepository projectsRepository;
    private final SkillsRepository skillsRepository;
    private final TimelineEventsRepository timelineEventsRepository;

    public PortfolioStatVO getStats() {
        PortfolioStatVO vo = new PortfolioStatVO();
        vo.setProjectCount(projectsRepository.listAll().size());
        vo.setFeaturedProjectCount(projectsRepository.listAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getFeatured()))
                .count());
        vo.setSkillCount(skillsRepository.listAll().size());
        vo.setTimelineEventCount(timelineEventsRepository.listAll().size());
        log.info("作品集统计查询完成, projectCount={}, skillCount={}, timelineEventCount={}",
                vo.getProjectCount(), vo.getSkillCount(), vo.getTimelineEventCount());
        return vo;
    }
}
