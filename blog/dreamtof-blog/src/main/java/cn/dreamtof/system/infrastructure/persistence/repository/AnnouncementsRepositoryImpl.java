package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.system.application.assembler.AnnouncementsAssembler;
import cn.dreamtof.system.domain.model.entity.Announcements;
import cn.dreamtof.system.domain.repository.AnnouncementsRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.AnnouncementsMapper;
import cn.dreamtof.system.infrastructure.persistence.po.AnnouncementsPO;
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
public class AnnouncementsRepositoryImpl extends ServiceImpl<AnnouncementsMapper, AnnouncementsPO> implements AnnouncementsRepository {

    private final AnnouncementsAssembler assembler;

    @Override
    public Announcements create(Announcements entity) {
        AnnouncementsPO po = assembler.toPO(entity);
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
    public Announcements update(Announcements entity) {
        AnnouncementsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Announcements getById(UUID id) {
        AnnouncementsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Announcements> listAll() {
        List<AnnouncementsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Announcements> page(PageReq pageReq) {
        Page<AnnouncementsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<AnnouncementsPO> resultPage = super.page(flexPage, queryWrapper);
        List<Announcements> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Announcements> entities) {
        List<AnnouncementsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(AnnouncementsPO::getId).eq(id).exists();
    }

    @Override
    public List<Announcements> listByIds(List<UUID> ids) {
        List<AnnouncementsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Announcements> seek(UUID cursor, int limit) {
        List<AnnouncementsPO> poList = queryChain()
                .where(AnnouncementsPO::getId).gt(cursor)
                .orderBy(AnnouncementsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<AnnouncementsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<Announcements> listActive() {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(AnnouncementsPO::getHasEnabled).eq(true);
        queryWrapper.and(AnnouncementsPO::getDeletedAt).isNull();
        queryWrapper.orderBy(AnnouncementsPO::getSortOrder).asc();
        List<AnnouncementsPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }
}
