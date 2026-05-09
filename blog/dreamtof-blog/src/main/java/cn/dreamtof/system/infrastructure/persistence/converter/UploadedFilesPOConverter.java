package cn.dreamtof.system.infrastructure.persistence.converter;

import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.infrastructure.persistence.po.UploadedFilesPO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UploadedFilesPOConverter {

    UploadedFilesPO toPO(UploadedFiles entity);

    UploadedFiles toEntity(UploadedFilesPO po);

    List<UploadedFiles> toEntityList(List<UploadedFilesPO> poList);

    List<UploadedFilesPO> toPOList(List<UploadedFiles> entityList);
}
