package cn.dreamtof.portfolio.infrastructure.persistence.mapper;

import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.TimelineEventLinksPO;
import cn.dreamtof.blog.portfolio.infrastructure.persistence.po.TimelineEventLinksPOPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 时间线链接关联表 转换器
 * <p>
 * 职责：实现DTO ,VO, Entity 与 PO 之间的无损映射。
 * </p>
 */
@Mapper(componentModel = "spring")
public interface TimelineEventLinksPOAssembler {

    /**
     * Entity 转 PO (入库)
     */
    TimelineEventLinksPOPO toPO(TimelineEventLinksPO entity);

    /**
     * PO 转 Entity (出库)
     */
    TimelineEventLinksPO toEntity(TimelineEventLinksPOPO po);

    /**
     * 集合转换
     * PO 转 Entity (出库)
     */
    List<TimelineEventLinksPO> toEntityList(List<TimelineEventLinksPOPO> poList);

    /**
     * 集合转换
     * Entity 转 PO (入库)
     */
    List<TimelineEventLinksPOPO> toPOList(List<TimelineEventLinksPO> entityList);
}