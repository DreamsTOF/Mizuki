package cn.dreamtof.portfolio.infrastructure.persistence.mapper;

import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.ProjectTagsPO;
import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.ProjectTagsPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 项目标签关联表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface ProjectTagsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ProjectTagsPOPO toPO(ProjectTagsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    ProjectTagsPO toEntity(ProjectTagsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<ProjectTagsPO> toEntityList(List<ProjectTagsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ProjectTagsPOPO> toPOList(List<ProjectTagsPO> entityList);
}