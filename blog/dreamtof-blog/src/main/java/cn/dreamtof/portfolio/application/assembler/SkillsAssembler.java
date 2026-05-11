package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.portfolio.api.vo.SkillVO;
import cn.dreamtof.portfolio.domain.model.entity.Skills;
import cn.dreamtof.portfolio.infrastructure.persistence.po.SkillsPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SkillsAssembler {

    SkillsPO toPO(Skills entity);

    Skills toEntity(SkillsPO po);

    List<Skills> toEntityList(List<SkillsPO> poList);

    List<SkillsPO> toPOList(List<Skills> entityList);

    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "certifications", ignore = true)
    SkillVO toVO(Skills entity);

    List<SkillVO> toVOList(List<Skills> entities);
}
