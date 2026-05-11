package cn.dreamtof.device.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.device.application.assembler.DevicesAssembler;
import cn.dreamtof.device.domain.model.entity.Devices;
import cn.dreamtof.device.domain.repository.DevicesRepository;
import cn.dreamtof.device.infrastructure.persistence.mapper.DevicesMapper;
import cn.dreamtof.device.infrastructure.persistence.po.DevicesPO;
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
public class DevicesRepositoryImpl extends ServiceImpl<DevicesMapper, DevicesPO> implements DevicesRepository {

    private final DevicesAssembler assembler;

    @Override
    public Devices create(Devices entity) {
        DevicesPO po = assembler.toPO(entity);
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
    public Devices update(Devices entity) {
        DevicesPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Devices getById(UUID id) {
        DevicesPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Devices> listAll() {
        List<DevicesPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Devices> page(PageReq pageReq) {
        Page<DevicesPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<DevicesPO> resultPage = super.page(flexPage, queryWrapper);
        List<Devices> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Devices> entities) {
        List<DevicesPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(DevicesPO::getId).eq(id).exists();
    }

    @Override
    public List<Devices> listByIds(List<UUID> ids) {
        List<DevicesPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Devices> seek(UUID cursor, int limit) {
        List<DevicesPO> poList = queryChain()
                .where(DevicesPO::getId).gt(cursor)
                .orderBy(DevicesPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<DevicesPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<Devices> listByCategoryId(UUID categoryId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(DevicesPO::getCategoryId).eq(categoryId);
        List<DevicesPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }

    @Override
    public long countByCategoryId(UUID categoryId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(DevicesPO::getCategoryId).eq(categoryId);
        return super.count(queryWrapper);
    }
}
