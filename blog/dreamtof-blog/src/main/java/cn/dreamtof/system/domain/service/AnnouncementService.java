package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.entity.Announcements;
import cn.dreamtof.system.domain.repository.AnnouncementsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {

    private final AnnouncementsRepository announcementsRepository;

    public Announcements createAnnouncement(Announcements entity) {
        Asserts.notNull(entity, "公告实体不能为空");
        Asserts.notBlank(entity.getTitle(), "公告标题不能为空");
        return announcementsRepository.create(entity);
    }

    public Announcements updateAnnouncement(Announcements entity) {
        Asserts.notNull(entity, "公告实体不能为空");
        Asserts.notNull(entity.getId(), "公告ID不能为空");
        Announcements existing = announcementsRepository.getById(entity.getId());
        Asserts.notNull(existing, "公告不存在");
        return announcementsRepository.update(entity);
    }

    public boolean deleteAnnouncement(UUID id) {
        Announcements existing = announcementsRepository.getById(id);
        Asserts.notNull(existing, "公告不存在");
        boolean removed = announcementsRepository.removeById(id);
        log.info("公告删除完成, announcementId={}", id);
        return removed;
    }

    public List<Announcements> getActive() {
        return announcementsRepository.listActive();
    }

    public Announcements getById(UUID id) {
        Announcements entity = announcementsRepository.getById(id);
        Asserts.notNull(entity, "公告不存在");
        return entity;
    }

    public List<Announcements> listAll() {
        return announcementsRepository.listAll();
    }
}
