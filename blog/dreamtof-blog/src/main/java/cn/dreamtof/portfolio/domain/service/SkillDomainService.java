package cn.dreamtof.portfolio.domain.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.portfolio.domain.model.entity.Skills;
import cn.dreamtof.portfolio.domain.repository.SkillsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillDomainService {

    private final SkillsRepository skillsRepository;

    public Skills createSkill(Skills entity) {
        Asserts.notNull(entity, "技能实体不能为空");
        return skillsRepository.create(entity);
    }

    public Skills updateSkill(Skills entity) {
        Asserts.notNull(entity, "技能实体不能为空");
        Asserts.notNull(entity.getId(), "技能ID不能为空");
        Skills existing = skillsRepository.getById(entity.getId());
        Asserts.notNull(existing, "技能不存在");
        return skillsRepository.update(entity);
    }

    public boolean deleteSkill(UUID id) {
        Skills existing = skillsRepository.getById(id);
        Asserts.notNull(existing, "技能不存在");
        existing.markDeleted();
        skillsRepository.update(existing);
        log.info("技能软删除完成, skillId={}", id);
        return true;
    }

    public Skills getById(UUID id) {
        Skills entity = skillsRepository.getById(id);
        Asserts.notNull(entity, "技能不存在");
        return entity;
    }

    public List<Skills> listByCategory(String category) {
        return skillsRepository.listByCategory(category);
    }

    public List<String> listCategories() {
        return skillsRepository.listCategories();
    }

    public PageResult<Skills> page(PageReq pageReq) {
        return skillsRepository.page(pageReq);
    }

    public List<Skills> listAll() {
        return skillsRepository.listAll();
    }
}
