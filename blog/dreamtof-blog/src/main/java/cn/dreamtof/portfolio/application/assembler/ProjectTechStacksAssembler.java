package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.blog.portfolio.domain.model.entity.ProjectTechStacks;
import cn.dreamtof.portfolio.infrastructure.persistence.po.ProjectTechStacksPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 项目技术栈关联表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface ProjectTechStacksAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ProjectTechStacksPO toPO(ProjectTechStacks entity);

    /**
     * PO 转 Entity (出库)
     */
    ProjectTechStacks toEntity(ProjectTechStacksPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<ProjectTechStacks> toEntityList(List<ProjectTechStacksPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ProjectTechStacksPO> toPOList(List<ProjectTechStacks> entityList);
}