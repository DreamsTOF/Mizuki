package cn.dreamtof.portfolio.application.assembler;

import cn.dreamtof.blog.portfolio.domain.model.entity.TimelineEvents;
import cn.dreamtof.portfolio.infrastructure.persistence.po.TimelineEventsPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 时间线事件表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface TimelineEventsAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TimelineEventsPO toPO(TimelineEvents entity);

    /**
     * PO 转 Entity (出库)
     */
    TimelineEvents toEntity(TimelineEventsPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TimelineEvents> toEntityList(List<TimelineEventsPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TimelineEventsPO> toPOList(List<TimelineEvents> entityList);
}