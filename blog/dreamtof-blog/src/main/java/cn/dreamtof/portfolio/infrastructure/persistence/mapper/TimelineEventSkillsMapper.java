package cn.dreamtof.portfolio.infrastructure.persistence.mapper;

import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.TimelineEventSkillsPO;
import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.TimelineEventSkillsPOPO;
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
public interface TimelineEventSkillsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TimelineEventSkillsPOPO toPO(TimelineEventSkillsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    TimelineEventSkillsPO toEntity(TimelineEventSkillsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TimelineEventSkillsPO> toEntityList(List<TimelineEventSkillsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TimelineEventSkillsPOPO> toPOList(List<TimelineEventSkillsPO> entityList);
}