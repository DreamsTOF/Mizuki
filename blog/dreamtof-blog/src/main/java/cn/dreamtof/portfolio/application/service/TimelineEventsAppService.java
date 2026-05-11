package cn.dreamtof.portfolio.application.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.portfolio.api.vo.TimelineEventVO;
import cn.dreamtof.portfolio.application.assembler.TimelineEventsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEventAchievements;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEventLinks;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEvents;
import cn.dreamtof.portfolio.domain.service.TimelineDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineEventsAppService {

    private final TimelineDomainService timelineDomainService;
    private final TimelineEventsAssembler assembler;
    private final TransactionTemplate transactionTemplate;

    public TimelineEventVO createEvent(String title, String description, String type,
                                       String icon, String color,
                                       OffsetDateTime startDate, OffsetDateTime endDate,
                                       String location, String organization, String position,
                                       Boolean featured,
                                       List<String> skillNames,
                                       List<TimelineEventLinks> links,
                                       List<TimelineEventAchievements> achievements) {
        TimelineEvents entity = TimelineEvents.create(title, description, type, icon, color,
                startDate, endDate, location, organization, position, featured);
        TimelineEvents created = timelineDomainService.createEvent(entity);
        syncSubEntities(created.getId(), skillNames, links, achievements);
        log.info("时间线事件创建完成, eventId={}, title={}", created.getId(), title);
        return enrichVO(created);
    }

    public TimelineEventVO updateEvent(UUID id, String title, String description, String type,
                                       String icon, String color,
                                       OffsetDateTime startDate, OffsetDateTime endDate,
                                       String location, String organization, String position,
                                       Boolean featured,
                                       List<String> skillNames,
                                       List<TimelineEventLinks> links,
                                       List<TimelineEventAchievements> achievements) {
        TimelineEvents existing = timelineDomainService.getById(id);
        existing.update(title, description, type, icon, color,
                startDate, endDate, location, organization, position, featured);
        TimelineEvents updated = timelineDomainService.updateEvent(existing);
        syncSubEntities(updated.getId(), skillNames, links, achievements);
        log.info("时间线事件更新完成, eventId={}", id);
        return enrichVO(updated);
    }

    public boolean deleteEvent(UUID id) {
        return transactionTemplate.execute(status -> timelineDomainService.deleteEvent(id));
    }

    public TimelineEventVO getDetail(UUID id) {
        TimelineEvents entity = timelineDomainService.getById(id);
        return enrichVO(entity);
    }

    public List<TimelineEventVO> listAll() {
        List<TimelineEvents> entities = timelineDomainService.listAll();
        return toVOList(entities);
    }

    public List<TimelineEventVO> listByEventType(String eventType) {
        List<TimelineEvents> entities = timelineDomainService.listByEventType(eventType);
        return toVOList(entities);
    }

    public List<String> listEventTypes() {
        return timelineDomainService.listEventTypes();
    }

    public PageResult<TimelineEventVO> pageEvents(PageReq pageReq) {
        PageResult<TimelineEvents> pageResult = timelineDomainService.page(pageReq);
        List<TimelineEventVO> voList = toVOList(pageResult.getRecords());
        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPages(),
                pageResult.getPageNum(), pageResult.getPageSize());
    }

    private void syncSubEntities(UUID eventId, List<String> skillNames,
                                 List<TimelineEventLinks> links,
                                 List<TimelineEventAchievements> achievements) {
        if (skillNames != null) {
            timelineDomainService.syncSkills(eventId, skillNames);
        }
        if (links != null) {
            timelineDomainService.syncLinks(eventId, links);
        }
        if (achievements != null) {
            timelineDomainService.syncAchievements(eventId, achievements);
        }
    }

    private TimelineEventVO enrichVO(TimelineEvents entity) {
        TimelineEventVO vo = toVO(entity);
        vo.setSkills(timelineDomainService.getSkillNames(entity.getId()));

        List<TimelineEventLinks> links = timelineDomainService.getLinks(entity.getId());
        List<TimelineEventVO.TimelineLinkVO> linkVOs = new ArrayList<>(links.size());
        for (TimelineEventLinks link : links) {
            linkVOs.add(new TimelineEventVO.TimelineLinkVO(
                    link.getName(), link.getUrl(), link.getLinkType()));
        }
        vo.setLinks(linkVOs);

        List<TimelineEventAchievements> achs = timelineDomainService.getAchievements(entity.getId());
        List<String> achNames = new ArrayList<>(achs.size());
        for (TimelineEventAchievements ach : achs) {
            achNames.add(ach.getAchievement());
        }
        vo.setAchievements(achNames);
        return vo;
    }

    // ==========================================
    // 手动 VO 转换（字段名与 Entity 不一致，无法依赖 MapStruct 自动映射）
    // ==========================================

    private TimelineEventVO toVO(TimelineEvents entity) {
        TimelineEventVO vo = new TimelineEventVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setType(entity.getType());
        vo.setIcon(entity.getIcon());
        vo.setColor(entity.getColor());
        vo.setStartDate(entity.getStartDate() != null
                ? DateUtils.format(entity.getStartDate().toLocalDateTime()) : null);
        vo.setEndDate(entity.getEndDate() != null
                ? DateUtils.format(entity.getEndDate().toLocalDateTime()) : null);
        vo.setLocation(entity.getLocation());
        vo.setOrganization(entity.getOrganization());
        vo.setPosition(entity.getPosition());
        vo.setFeatured(entity.getFeatured());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private List<TimelineEventVO> toVOList(List<TimelineEvents> entities) {
        return entities.stream().map(this::toVO).toList();
    }
}
