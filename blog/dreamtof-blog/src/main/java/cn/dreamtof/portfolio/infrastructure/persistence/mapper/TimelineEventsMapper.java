package cn.dreamtof.portfolio.infrastructure.persistence.mapper;

import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.TimelineEventsPO;
import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.TimelineEventsPOPO;
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
public interface TimelineEventsPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TimelineEventsPOPO toPO(TimelineEventsPO entity);

    /**
     * PO 转 Entity (出库)
     */
    TimelineEventsPO toEntity(TimelineEventsPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TimelineEventsPO> toEntityList(List<TimelineEventsPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TimelineEventsPOPO> toPOList(List<TimelineEventsPO> entityList);
}