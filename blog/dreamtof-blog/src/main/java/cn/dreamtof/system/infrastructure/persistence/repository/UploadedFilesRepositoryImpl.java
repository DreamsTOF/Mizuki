package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.domain.model.valueobject.FilePageQuery;
import cn.dreamtof.system.domain.repository.UploadedFilesRepository;
import cn.dreamtof.system.infrastructure.persistence.converter.UploadedFilesPOConverter;
import cn.dreamtof.system.infrastructure.persistence.mapper.UploadedFilesMapper;
import cn.dreamtof.system.infrastructure.persistence.po.UploadedFilesPO;
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

    private final UploadedFilesPOConverter converter;

    @Override
    public UploadedFiles create(UploadedFiles entity) {
        UploadedFilesPO po = converter.toPO(entity);
        if (super.save(po)) {
            return converter.toEntity(po);
        }
        return null;
    }

    @Override
    public UploadedFiles update(UploadedFiles entity) {
        UploadedFilesPO po = converter.toPO(entity);
        if (super.updateById(po)) {
            return converter.toEntity(po);
        }
        return null;
    }

    @Override
    public UploadedFiles getById(UUID id) {
        UploadedFilesPO po = super.getById(id);
        return converter.toEntity(po);
    }

    @Override
    public UploadedFiles findByUrl(String url) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(UploadedFilesPO::getUrl).eq(url);
        qw.and(UploadedFilesPO::getDeletedAt).isNull();
        UploadedFilesPO po = super.getOne(qw);
        return converter.toEntity(po);
    }

    @Override
    public PageResult<UploadedFiles> page(FilePageQuery query) {
        Page<UploadedFilesPO> flexPage = Page.of(query.pageNum(), query.pageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.where(UploadedFilesPO::getDeletedAt).isNull();
        if (query.folder() != null) {
            qw.and(UploadedFilesPO::getFolder).eq(query.folder().getCode());
        }
        qw.orderBy(UploadedFilesPO::getCreatedAt, false);

        Page<UploadedFilesPO> resultPage = super.page(flexPage, qw);
        List<UploadedFiles> entityList = converter.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(UploadedFilesPO::getId).eq(id);
        return super.count(qw) > 0;
    }

    @Override
    public List<UploadedFiles> listByIds(List<UUID> ids) {
        List<UploadedFilesPO> poList = super.listByIds(ids);
        return converter.toEntityList(poList);
    }

    @Override
    public boolean softDeleteById(UUID id) {
        UploadedFilesPO po = super.getById(id);
        if (po == null) return false;
        po.setDeletedAt(DateUtils.offsetNow());
        return super.updateById(po);
    }
}
