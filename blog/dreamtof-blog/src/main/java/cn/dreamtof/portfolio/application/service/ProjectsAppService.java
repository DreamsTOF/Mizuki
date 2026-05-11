package cn.dreamtof.portfolio.application.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.portfolio.api.vo.ProjectVO;
import cn.dreamtof.portfolio.application.assembler.ProjectsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.Projects;
import cn.dreamtof.portfolio.domain.service.ProjectDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectsAppService {

    private final ProjectDomainService projectDomainService;
    private final ProjectsAssembler assembler;
    private final TransactionTemplate transactionTemplate;

    public ProjectVO createProject(String title, String description, String image,
                                   String category, String status, String liveDemo,
                                   String sourceCode, String visitUrl,
                                   OffsetDateTime startDate, OffsetDateTime endDate,
                                   Boolean featured, Boolean showImage, Integer sortOrder,
                                   List<String> techStacks, List<String> tags) {
        Projects entity = Projects.create(title, description, image, category, status,
                liveDemo, sourceCode, visitUrl, startDate, endDate,
                featured, showImage, sortOrder);
        Projects created = projectDomainService.createProject(entity);
        if (techStacks != null && !techStacks.isEmpty()) {
            projectDomainService.syncTechStacks(created.getId(), techStacks);
        }
        if (tags != null && !tags.isEmpty()) {
            projectDomainService.syncTags(created.getId(), tags);
        }
        ProjectVO vo = toVO(created);
        vo.setTechStack(techStacks != null ? techStacks : List.of());
        vo.setTags(tags != null ? tags : List.of());
        log.info("项目创建完成, projectId={}, title={}", created.getId(), title);
        return vo;
    }

    public ProjectVO updateProject(UUID id, String title, String description, String image,
                                   String category, String status, String liveDemo,
                                   String sourceCode, String visitUrl,
                                   OffsetDateTime startDate, OffsetDateTime endDate,
                                   Boolean showImage, Integer sortOrder,
                                   List<String> techStacks, List<String> tags) {
        Projects existing = projectDomainService.getById(id);
        existing.update(title, description, image, category, status, liveDemo,
                sourceCode, visitUrl, startDate, endDate, showImage, sortOrder);
        Projects updated = projectDomainService.updateProject(existing);
        if (techStacks != null) {
            projectDomainService.syncTechStacks(updated.getId(), techStacks);
        }
        if (tags != null) {
            projectDomainService.syncTags(updated.getId(), tags);
        }
        ProjectVO vo = toVO(updated);
        vo.setTechStack(projectDomainService.getTechStackNames(updated.getId()));
        vo.setTags(projectDomainService.getTagNames(updated.getId()));
        log.info("项目更新完成, projectId={}", id);
        return vo;
    }

    public boolean deleteProject(UUID id) {
        return transactionTemplate.execute(status -> projectDomainService.deleteProject(id));
    }

    public ProjectVO toggleFeatured(UUID id, boolean featured) {
        Projects updated = projectDomainService.toggleFeatured(id, featured);
        ProjectVO vo = toVO(updated);
        vo.setTechStack(projectDomainService.getTechStackNames(updated.getId()));
        vo.setTags(projectDomainService.getTagNames(updated.getId()));
        return vo;
    }

    public ProjectVO getDetail(UUID id) {
        Projects entity = projectDomainService.getById(id);
        ProjectVO vo = toVO(entity);
        vo.setTechStack(projectDomainService.getTechStackNames(id));
        vo.setTags(projectDomainService.getTagNames(id));
        return vo;
    }

    public List<ProjectVO> listAll() {
        List<Projects> entities = projectDomainService.listAll();
        return toVOList(entities);
    }

    public List<ProjectVO> listByCategory(String category) {
        List<Projects> entities = projectDomainService.listByCategory(category);
        return toVOList(entities);
    }

    public List<String> listCategories() {
        return projectDomainService.listCategories();
    }

    public PageResult<ProjectVO> pageProjects(PageReq pageReq) {
        PageResult<Projects> pageResult = projectDomainService.page(pageReq);
        List<ProjectVO> voList = toVOList(pageResult.getRecords());
        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPages(),
                pageResult.getPageNum(), pageResult.getPageSize());
    }

    // ==========================================
    // 手动 VO 转换（字段名与 Entity 不一致，无法依赖 MapStruct 自动映射）
    // ==========================================

    private ProjectVO toVO(Projects entity) {
        ProjectVO vo = new ProjectVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setImage(entity.getImage());
        vo.setCategory(entity.getCategory());
        vo.setStatus(entity.getStatus());
        vo.setLiveDemo(entity.getLiveDemo());
        vo.setSourceCode(entity.getSourceCode());
        vo.setVisitUrl(entity.getVisitUrl());
        vo.setStartDate(entity.getStartDate() != null
                ? DateUtils.format(entity.getStartDate().toLocalDateTime()) : null);
        vo.setEndDate(entity.getEndDate() != null
                ? DateUtils.format(entity.getEndDate().toLocalDateTime()) : null);
        vo.setFeatured(entity.getFeatured());
        vo.setShowImage(entity.getShowImage());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private List<ProjectVO> toVOList(List<Projects> entities) {
        return entities.stream().map(this::toVO).toList();
    }
}
