package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.application.assembler.NavLinksAssembler;
import cn.dreamtof.system.domain.model.entity.NavLinks;
import cn.dreamtof.system.domain.repository.NavLinksRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.NavLinksMapper;
import cn.dreamtof.system.infrastructure.persistence.po.NavLinksPO;
import cn.dreamtof.system.infrastructure.persistence.po.table.NavLinksTableDef;
import com.mybatisflex.core.paginate.Page;
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
public class NavLinksRepositoryImpl extends ServiceImpl<NavLinksMapper, NavLinksPO> implements NavLinksRepository {

    private final NavLinksAssembler assembler;
    private static final NavLinksTableDef T = NavLinksTableDef.NAV_LINKS_PO;

    @Override
    public NavLinks create(NavLinks entity) {
        NavLinksPO po = assembler.toPO(entity);
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
    public NavLinks update(NavLinks entity) {
        NavLinksPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public NavLinks getById(UUID id) {
        NavLinksPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<NavLinks> listAll() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        List<NavLinksPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<NavLinks> page(PageReq pageReq) {
        Page<NavLinksPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        Page<NavLinksPO> resultPage = super.page(flexPage, qw);
        List<NavLinks> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<NavLinks> entities) {
        List<NavLinksPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<NavLinks> listByIds(List<UUID> ids) {
        List<NavLinksPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<NavLinks> listByPosition(String position) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.POSITION.eq(position));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        List<NavLinksPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<NavLinks> listByParentId(UUID parentId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.PARENT_ID.eq(parentId));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        List<NavLinksPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<NavLinks> listEnabled() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.HAS_ENABLED.eq(true));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        List<NavLinksPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }
}
