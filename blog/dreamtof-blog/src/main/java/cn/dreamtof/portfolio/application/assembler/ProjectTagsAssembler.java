package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.blog.portfolio.domain.model.entity.ProjectTags;
import cn.dreamtof.portfolio.infrastructure.persistence.po.ProjectTagsPO;
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
public interface ProjectTagsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    ProjectTagsPO toPO(ProjectTags entity);

    /**
     * PO 转 Entity (出库)
     */
    ProjectTags toEntity(ProjectTagsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<ProjectTags> toEntityList(List<ProjectTagsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<ProjectTagsPO> toPOList(List<ProjectTags> entityList);
}