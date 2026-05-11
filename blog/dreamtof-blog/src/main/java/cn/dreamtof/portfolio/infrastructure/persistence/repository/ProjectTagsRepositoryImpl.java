package cn.dreamtof.portfolio.infrastructure.persistence.repository;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.application.assembler.ProjectTagsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.ProjectTags;
import cn.dreamtof.portfolio.domain.repository.ProjectTagsRepository;
import cn.dreamtof.portfolio.infrastructure.persistence.mapper.ProjectTagsMapper;
import cn.dreamtof.portfolio.infrastructure.persistence.po.ProjectTagsPO;
import cn.dreamtof.portfolio.infrastructure.persistence.po.table.ProjectTagsTableDef;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProjectTagsRepositoryImpl extends ServiceImpl<ProjectTagsMapper, ProjectTagsPO> implements ProjectTagsRepository {

    private final ProjectTagsAssembler assembler;
    private static final ProjectTagsTableDef T = ProjectTagsTableDef.PROJECT_TAGS_PO;

    @Override
    public ProjectTags create(ProjectTags entity) {
        ProjectTagsPO po = assembler.toPO(entity);
        if (super.save(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public boolean removeById(UUID id) {
        return super.removeById(id);
    }

    @Override
    public ProjectTags update(ProjectTags entity) {
        ProjectTagsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public ProjectTags getById(UUID id) {
        ProjectTagsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<ProjectTags> listAll() {
        List<ProjectTagsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<ProjectTags> page(PageReq pageReq) {
        return PageResult.empty(pageReq.getPageNum(), pageReq.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<ProjectTags> entities) {
        List<ProjectTagsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<ProjectTags> listByIds(List<UUID> ids) {
        List<ProjectTagsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<ProjectTags> listByProjectId(UUID projectId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.PROJECT_ID.eq(projectId));
        List<ProjectTagsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public void removeByProjectId(UUID projectId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.PROJECT_ID.eq(projectId));
        super.removeByIds(super.list(qw).stream().map(ProjectTagsPO::getId).toList());
    }
}
