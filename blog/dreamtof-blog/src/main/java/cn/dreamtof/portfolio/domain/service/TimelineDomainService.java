package cn.dreamtof.portfolio.domain.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEventAchievements;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEventLinks;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEventSkills;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEvents;
import cn.dreamtof.portfolio.domain.repository.TimelineEventAchievementsRepository;
import cn.dreamtof.portfolio.domain.repository.TimelineEventLinksRepository;
import cn.dreamtof.portfolio.domain.repository.TimelineEventSkillsRepository;
import cn.dreamtof.portfolio.domain.repository.TimelineEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineDomainService {

    private final TimelineEventsRepository eventsRepository;
    private final TimelineEventSkillsRepository skillsRepository;
    private final TimelineEventLinksRepository linksRepository;
    private final TimelineEventAchievementsRepository achievementsRepository;

    public TimelineEvents createEvent(TimelineEvents entity) {
        Asserts.notNull(entity, "时间线事件实体不能为空");
        return eventsRepository.create(entity);
    }

    public TimelineEvents updateEvent(TimelineEvents entity) {
        Asserts.notNull(entity, "时间线事件实体不能为空");
        Asserts.notNull(entity.getId(), "事件ID不能为空");
        TimelineEvents existing = eventsRepository.getById(entity.getId());
        Asserts.notNull(existing, "时间线事件不存在");
        return eventsRepository.update(entity);
    }

    public boolean deleteEvent(UUID id) {
        TimelineEvents existing = eventsRepository.getById(id);
        Asserts.notNull(existing, "时间线事件不存在");
        existing.markDeleted();
        eventsRepository.update(existing);
        skillsRepository.removeByTimelineEventId(id);
        linksRepository.removeByTimelineEventId(id);
        achievementsRepository.removeByTimelineEventId(id);
        log.info("时间线事件软删除完成, eventId={}", id);
        return true;
    }

    public TimelineEvents getById(UUID id) {
        TimelineEvents entity = eventsRepository.getById(id);
        Asserts.notNull(entity, "时间线事件不存在");
        return entity;
    }

    public List<TimelineEvents> listByEventType(String eventType) {
        return eventsRepository.listByEventType(eventType);
    }

    public List<String> listEventTypes() {
        return eventsRepository.listEventTypes();
    }

    public PageResult<TimelineEvents> page(PageReq pageReq) {
        return eventsRepository.page(pageReq);
    }

    public List<TimelineEvents> listAll() {
        return eventsRepository.listAll();
    }

    public void syncSkills(UUID eventId, List<String> skillNames) {
        Asserts.notNull(eventId, "事件ID不能为空");
        skillsRepository.removeByTimelineEventId(eventId);
        if (skillNames == null || skillNames.isEmpty()) {
            return;
        }
        List<TimelineEventSkills> newSkills = new ArrayList<>(skillNames.size());
        for (String skillName : skillNames) {
            newSkills.add(TimelineEventSkills.create(eventId, skillName));
        }
        skillsRepository.saveBatch(newSkills);
    }

    public List<String> getSkillNames(UUID eventId) {
        List<TimelineEventSkills> skills = skillsRepository.listByTimelineEventId(eventId);
        List<String> names = new ArrayList<>(skills.size());
        for (TimelineEventSkills skill : skills) {
            names.add(skill.getSkillName());
        }
        return names;
    }

    public void syncLinks(UUID eventId, List<TimelineEventLinks> links) {
        Asserts.notNull(eventId, "事件ID不能为空");
        linksRepository.removeByTimelineEventId(eventId);
        if (links == null || links.isEmpty()) {
            return;
        }
        List<TimelineEventLinks> newLinks = new ArrayList<>(links.size());
        for (TimelineEventLinks link : links) {
            newLinks.add(TimelineEventLinks.create(eventId, link.getName(), link.getUrl(), link.getLinkType()));
        }
        linksRepository.saveBatch(newLinks);
    }

    public List<TimelineEventLinks> getLinks(UUID eventId) {
        return linksRepository.listByTimelineEventId(eventId);
    }

    public void syncAchievements(UUID eventId, List<TimelineEventAchievements> achievements) {
        Asserts.notNull(eventId, "事件ID不能为空");
        achievementsRepository.removeByTimelineEventId(eventId);
        if (achievements == null || achievements.isEmpty()) {
            return;
        }
        List<TimelineEventAchievements> newList = new ArrayList<>(achievements.size());
        for (TimelineEventAchievements ach : achievements) {
            newList.add(TimelineEventAchievements.create(eventId, ach.getAchievement(), ach.getSortOrder()));
        }
        achievementsRepository.saveBatch(newList);
    }

    public List<TimelineEventAchievements> getAchievements(UUID eventId) {
        return achievementsRepository.listByTimelineEventId(eventId);
    }
}
