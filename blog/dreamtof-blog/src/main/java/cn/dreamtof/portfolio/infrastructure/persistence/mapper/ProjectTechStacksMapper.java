package cn.dreamtof.portfolio.infrastructure.persistence.mapper;

import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.ProjectTechStacksPO;
import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.ProjectTechStacksPOPO;
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
public interface ProjectTechStacksPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ProjectTechStacksPOPO toPO(ProjectTechStacksPO entity);

    /**
     * PO 转 Entity (出库)
     */
    ProjectTechStacksPO toEntity(ProjectTechStacksPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<ProjectTechStacksPO> toEntityList(List<ProjectTechStacksPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ProjectTechStacksPOPO> toPOList(List<ProjectTechStacksPO> entityList);
}