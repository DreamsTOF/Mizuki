package cn.dreamtof.portfolio.infrastructure.persistence.mapper;

import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.ProjectsPO;
import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.ProjectsPOPO;
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
public interface ProjectsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ProjectsPOPO toPO(ProjectsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    ProjectsPO toEntity(ProjectsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<ProjectsPO> toEntityList(List<ProjectsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ProjectsPOPO> toPOList(List<ProjectsPO> entityList);
}