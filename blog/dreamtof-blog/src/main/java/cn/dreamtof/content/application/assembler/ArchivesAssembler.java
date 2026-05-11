package cn.dreamtof.content.application.assembler;

import cn.dreamtof.content.api.vo.ArchiveVO;
import cn.dreamtof.content.domain.model.entity.Archives;
import cn.dreamtof.content.infrastructure.persistence.po.ArchivesPO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ArchivesAssembler {

    ArchivesPO toPO(Archives entity);

    Archives toEntity(ArchivesPO po);

    List<Archives> toEntityList(List<ArchivesPO> poList);

    List<ArchivesPO> toPOList(List<Archives> entityList);

    ArchiveVO toVO(Archives entity);

    List<ArchiveVO> toVOList(List<Archives> entities);
}
