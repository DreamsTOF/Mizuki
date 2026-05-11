package cn.dreamtof.system.application.assembler;

import cn.dreamtof.system.api.vo.UploadedFileVO;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import cn.dreamtof.system.infrastructure.persistence.po.UploadedFilesPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UploadedFilesAssembler {

    UploadedFilesPO toPO(UploadedFiles entity);

    UploadedFiles toEntity(UploadedFilesPO po);

    List<UploadedFiles> toEntityList(List<UploadedFilesPO> poList);

    List<UploadedFilesPO> toPOList(List<UploadedFiles> entityList);

    UploadedFileVO toVO(UploadedFiles entity);

    List<UploadedFileVO> toVOList(List<UploadedFiles> entities);
}