package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.application.assembler.UploadedFilesAssembler;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.repository.UploadedFilesRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.UploadedFilesMapper;
import cn.dreamtof.system.infrastructure.persistence.po.UploadedFilesPO;
import cn.dreamtof.system.infrastructure.persistence.po.table.UploadedFilesTableDef;
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
public class UploadedFilesRepositoryImpl extends ServiceImpl<UploadedFilesMapper, UploadedFilesPO> implements UploadedFilesRepository {

    private final UploadedFilesAssembler assembler;

    private static final UploadedFilesTableDef T = UploadedFilesTableDef.UPLOADED_FILES_PO;

    @Override
    public UploadedFiles create(UploadedFiles entity) {
        UploadedFilesPO po = assembler.toPO(entity);
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
    public UploadedFiles update(UploadedFiles entity) {
        UploadedFilesPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public UploadedFiles getById(UUID id) {
        UploadedFilesPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<UploadedFiles> listAll() {
        List<UploadedFilesPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<UploadedFiles> pageByFolder(String folder, PageReq pageReq) {
        Page<UploadedFilesPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.FOLDER.eq(folder));
        qw.orderBy(T.CREATED_AT.desc());
        Page<UploadedFilesPO> resultPage = super.page(flexPage, qw);
        List<UploadedFiles> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<UploadedFiles> entities) {
        List<UploadedFilesPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<UploadedFiles> listByIds(List<UUID> ids) {
        List<UploadedFilesPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<UploadedFiles> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<UploadedFilesPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<UploadedFilesPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public UploadedFiles findByUrl(String url) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.URL.eq(url));
        qw.limit(1);
        UploadedFilesPO po = super.getOne(qw);
        return assembler.toEntity(po);
    }
}
