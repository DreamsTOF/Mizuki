package cn.dreamtof.system.application.assembler;

import cn.dreamtof.system.api.vo.UploadedFileVO;
import cn.dreamtof.system.domain.model.entity.UploadedFiles;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UploadedFileVOAssembler {

    UploadedFileVO toVO(UploadedFiles entity);

    List<UploadedFileVO> toVOList(List<UploadedFiles> entities);
}
