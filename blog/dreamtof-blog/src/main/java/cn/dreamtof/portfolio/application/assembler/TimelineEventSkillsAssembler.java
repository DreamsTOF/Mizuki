package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.blog.portfolio.domain.model.entity.TimelineEventSkills;
import cn.dreamtof.portfolio.infrastructure.persistence.po.TimelineEventSkillsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 时间线技能关联表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface TimelineEventSkillsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TimelineEventSkillsPO toPO(TimelineEventSkills entity);

    /**
     * PO 转 Entity (出库)
     */
    TimelineEventSkills toEntity(TimelineEventSkillsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TimelineEventSkills> toEntityList(List<TimelineEventSkillsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TimelineEventSkillsPO> toPOList(List<TimelineEventSkills> entityList);
}