package cn.dreamtof.portfolio.application.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.JsonUtils;
import cn.dreamtof.portfolio.api.vo.SkillVO;
import cn.dreamtof.portfolio.application.assembler.SkillsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.Skills;
import cn.dreamtof.portfolio.domain.service.SkillDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillsAppService {

    private final SkillDomainService skillDomainService;
    private final SkillsAssembler assembler;

    public SkillVO createSkill(String name, String description, String icon,
                               String category, String level,
                               Integer experienceYears, Integer experienceMonths,
                               String color, String projects, String certifications) {
        Skills entity = Skills.create(name, description, icon, category, level,
                experienceYears, experienceMonths, color, projects, certifications);
        Skills created = skillDomainService.createSkill(entity);
        log.info("技能创建完成, skillId={}, name={}", created.getId(), name);
        return toVO(created);
    }

    public SkillVO updateSkill(UUID id, String name, String description, String icon,
                               String category, String level,
                               Integer experienceYears, Integer experienceMonths,
                               String color, String projects, String certifications) {
        Skills existing = skillDomainService.getById(id);
        existing.update(name, description, icon, category, level,
                experienceYears, experienceMonths, color, projects, certifications);
        Skills updated = skillDomainService.updateSkill(existing);
        log.info("技能更新完成, skillId={}", id);
        return toVO(updated);
    }

    public boolean deleteSkill(UUID id) {
        return skillDomainService.deleteSkill(id);
    }

    public SkillVO getDetail(UUID id) {
        Skills entity = skillDomainService.getById(id);
        return toVO(entity);
    }

    public List<SkillVO> listAll() {
        List<Skills> entities = skillDomainService.listAll();
        return toVOList(entities);
    }

    public List<SkillVO> listByCategory(String category) {
        List<Skills> entities = skillDomainService.listByCategory(category);
        return toVOList(entities);
    }

    public List<String> listCategories() {
        return skillDomainService.listCategories();
    }

    public PageResult<SkillVO> pageSkills(PageReq pageReq) {
        PageResult<Skills> pageResult = skillDomainService.page(pageReq);
        List<SkillVO> voList = toVOList(pageResult.getRecords());
        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPages(),
                pageResult.getPageNum(), pageResult.getPageSize());
    }

    // ==========================================
    // 手动 VO 转换（字段名与 Entity 不一致，无法依赖 MapStruct 自动映射）
    // ==========================================

    private SkillVO toVO(Skills entity) {
        SkillVO vo = new SkillVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setIcon(entity.getIcon());
        vo.setCategory(entity.getCategory());
        vo.setLevel(entity.getLevel());
        vo.setExperience(new SkillVO.Experience(
                entity.getExperienceYears() != null ? entity.getExperienceYears() : 0,
                entity.getExperienceMonths() != null ? entity.getExperienceMonths() : 0
        ));
        vo.setProjects(JsonUtils.parseList(entity.getProjects(), String.class));
        vo.setCertifications(JsonUtils.parseList(entity.getCertifications(), String.class));
        vo.setColor(entity.getColor());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private List<SkillVO> toVOList(List<Skills> entities) {
        return entities.stream().map(this::toVO).toList();
    }
}
