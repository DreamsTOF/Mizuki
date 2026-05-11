package cn.dreamtof.content.infrastructure.persistence.repository;

import cn.dreamtof.content.application.assembler.TagsAssembler;
import cn.dreamtof.content.domain.model.entity.Tags;
import cn.dreamtof.content.domain.repository.TagsRepository;
import cn.dreamtof.content.infrastructure.persistence.mapper.TagsMapper;
import cn.dreamtof.content.infrastructure.persistence.po.TagsPO;
import cn.dreamtof.content.infrastructure.persistence.po.table.TagsTableDef;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
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
public class TagsRepositoryImpl extends ServiceImpl<TagsMapper, TagsPO> implements TagsRepository {

    private final TagsAssembler assembler;

    private static final TagsTableDef T = TagsTableDef.TAGS_PO;

    @Override
    public Tags create(Tags entity) {
        TagsPO po = assembler.toPO(entity);
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
    public Tags update(Tags entity) {
        TagsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Tags getById(UUID id) {
        TagsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Tags> listAll() {
        List<TagsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Tags> page(PageReq pageReq) {
        Page<TagsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy(T.CREATED_AT.desc());
        Page<TagsPO> resultPage = super.page(flexPage, qw);
        List<Tags> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Tags> entities) {
        List<TagsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<Tags> listByIds(List<UUID> ids) {
        List<TagsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Tags> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<TagsPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<TagsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public Tags findBySlug(String slug) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.SLUG.eq(slug));
        qw.limit(1);
        TagsPO po = super.getOne(qw);
        return assembler.toEntity(po);
    }

    @Override
    public Tags findByName(String name) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.NAME.eq(name));
        qw.limit(1);
        TagsPO po = super.getOne(qw);
        return assembler.toEntity(po);
    }
}
