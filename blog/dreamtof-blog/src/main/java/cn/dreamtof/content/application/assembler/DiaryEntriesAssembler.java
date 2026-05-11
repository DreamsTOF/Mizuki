package cn.dreamtof.content.application.assembler;

import cn.dreamtof.content.api.vo.DiaryVO;
import cn.dreamtof.content.domain.model.entity.DiaryEntries;
import cn.dreamtof.content.infrastructure.persistence.po.DiaryEntriesPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DiaryEntriesAssembler {

    DiaryEntriesPO toPO(DiaryEntries entity);

    DiaryEntries toEntity(DiaryEntriesPO po);

    List<DiaryEntries> toEntityList(List<DiaryEntriesPO> poList);

    List<DiaryEntriesPO> toPOList(List<DiaryEntries> entityList);

    @Mapping(target = "date", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "tags", ignore = true)
    DiaryVO toVO(DiaryEntries entity);

    List<DiaryVO> toVOList(List<DiaryEntries> entities);
}
