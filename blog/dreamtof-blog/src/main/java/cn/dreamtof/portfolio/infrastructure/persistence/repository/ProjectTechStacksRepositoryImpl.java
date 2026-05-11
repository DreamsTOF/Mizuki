package cn.dreamtof.portfolio.infrastructure.persistence.repository;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.application.assembler.ProjectTechStacksAssembler;
import cn.dreamtof.portfolio.domain.model.entity.ProjectTechStacks;
import cn.dreamtof.portfolio.domain.repository.ProjectTechStacksRepository;
import cn.dreamtof.portfolio.infrastructure.persistence.mapper.ProjectTechStacksMapper;
import cn.dreamtof.portfolio.infrastructure.persistence.po.ProjectTechStacksPO;
import cn.dreamtof.portfolio.infrastructure.persistence.po.table.ProjectTechStacksTableDef;
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
public class ProjectTechStacksRepositoryImpl extends ServiceImpl<ProjectTechStacksMapper, ProjectTechStacksPO> implements ProjectTechStacksRepository {

    private final ProjectTechStacksAssembler assembler;
    private static final ProjectTechStacksTableDef T = ProjectTechStacksTableDef.PROJECT_TECH_STACKS_PO;

    @Override
    public ProjectTechStacks create(ProjectTechStacks entity) {
        ProjectTechStacksPO po = assembler.toPO(entity);
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
    public ProjectTechStacks update(ProjectTechStacks entity) {
        ProjectTechStacksPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public ProjectTechStacks getById(UUID id) {
        ProjectTechStacksPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<ProjectTechStacks> listAll() {
        List<ProjectTechStacksPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<ProjectTechStacks> page(PageReq pageReq) {
        return PageResult.empty(pageReq.getPageNum(), pageReq.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<ProjectTechStacks> entities) {
        List<ProjectTechStacksPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<ProjectTechStacks> listByIds(List<UUID> ids) {
        List<ProjectTechStacksPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<ProjectTechStacks> listByProjectId(UUID projectId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.PROJECT_ID.eq(projectId));
        List<ProjectTechStacksPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public void removeByProjectId(UUID projectId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.PROJECT_ID.eq(projectId));
        super.removeByIds(super.list(qw).stream().map(ProjectTechStacksPO::getId).toList());
    }
}
