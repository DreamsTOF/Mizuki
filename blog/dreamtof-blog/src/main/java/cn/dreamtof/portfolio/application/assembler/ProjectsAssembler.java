package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.blog.portfolio.domain.model.entity.Projects;
import cn.dreamtof.portfolio.infrastructure.persistence.po.ProjectsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 项目表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface ProjectsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ProjectsPO toPO(Projects entity);

    /**
     * PO 转 Entity (出库)
     */
    Projects toEntity(ProjectsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<Projects> toEntityList(List<ProjectsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ProjectsPO> toPOList(List<Projects> entityList);
}