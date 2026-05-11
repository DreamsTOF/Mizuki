package cn.dreamtof.device.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.device.application.assembler.DeviceCategoriesAssembler;
import cn.dreamtof.device.domain.model.entity.DeviceCategories;
import cn.dreamtof.device.domain.repository.DeviceCategoriesRepository;
import cn.dreamtof.device.infrastructure.persistence.mapper.DeviceCategoriesMapper;
import cn.dreamtof.device.infrastructure.persistence.po.DeviceCategoriesPO;
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
public class DeviceCategoriesRepositoryImpl extends ServiceImpl<DeviceCategoriesMapper, DeviceCategoriesPO> implements DeviceCategoriesRepository {

    private final DeviceCategoriesAssembler assembler;

    @Override
    public DeviceCategories create(DeviceCategories entity) {
        DeviceCategoriesPO po = assembler.toPO(entity);
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
    public DeviceCategories update(DeviceCategories entity) {
        DeviceCategoriesPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public DeviceCategories getById(UUID id) {
        DeviceCategoriesPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<DeviceCategories> listAll() {
        List<DeviceCategoriesPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<DeviceCategories> page(PageReq pageReq) {
        Page<DeviceCategoriesPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<DeviceCategoriesPO> resultPage = super.page(flexPage, queryWrapper);
        List<DeviceCategories> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<DeviceCategories> entities) {
        List<DeviceCategoriesPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(DeviceCategoriesPO::getId).eq(id).exists();
    }

    @Override
    public List<DeviceCategories> listByIds(List<UUID> ids) {
        List<DeviceCategoriesPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<DeviceCategories> seek(UUID cursor, int limit) {
        List<DeviceCategoriesPO> poList = queryChain()
                .where(DeviceCategoriesPO::getId).gt(cursor)
                .orderBy(DeviceCategoriesPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<DeviceCategoriesPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }
}
