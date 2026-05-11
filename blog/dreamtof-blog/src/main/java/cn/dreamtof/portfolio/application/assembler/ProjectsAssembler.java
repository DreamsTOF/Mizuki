package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.portfolio.api.vo.ProjectVO;
import cn.dreamtof.portfolio.domain.model.entity.Projects;
import cn.dreamtof.portfolio.infrastructure.persistence.po.ProjectsPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectsAssembler {

    ProjectsPO toPO(Projects entity);

    Projects toEntity(ProjectsPO po);

    List<Projects> toEntityList(List<ProjectsPO> poList);

    List<ProjectsPO> toPOList(List<Projects> entityList);

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    ProjectVO toVO(Projects entity);

    List<ProjectVO> toVOList(List<Projects> entities);
}
