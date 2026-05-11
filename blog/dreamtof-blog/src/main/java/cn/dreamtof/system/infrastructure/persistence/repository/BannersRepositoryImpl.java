package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.application.assembler.BannersAssembler;
import cn.dreamtof.system.domain.model.entity.Banners;
import cn.dreamtof.system.domain.repository.BannersRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.BannersMapper;
import cn.dreamtof.system.infrastructure.persistence.po.BannersPO;
import cn.dreamtof.system.infrastructure.persistence.po.table.BannersTableDef;
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
public class BannersRepositoryImpl extends ServiceImpl<BannersMapper, BannersPO> implements BannersRepository {

    private final BannersAssembler assembler;
    private static final BannersTableDef T = BannersTableDef.BANNERS_PO;

    @Override
    public Banners create(Banners entity) {
        BannersPO po = assembler.toPO(entity);
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
    public Banners update(Banners entity) {
        BannersPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Banners getById(UUID id) {
        BannersPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Banners> listAll() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        List<BannersPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Banners> page(PageReq pageReq) {
        Page<BannersPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        Page<BannersPO> resultPage = super.page(flexPage, qw);
        List<Banners> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Banners> entities) {
        List<BannersPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<Banners> listByIds(List<UUID> ids) {
        List<BannersPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<Banners> listByPosition(String position) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.POSITION.eq(position));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        List<BannersPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<Banners> listEnabled() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.HAS_ENABLED.eq(true));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        List<BannersPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<Banners> listCarousel() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.HAS_CAROUSEL.eq(true));
        qw.where(T.HAS_ENABLED.eq(true));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        List<BannersPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }
}
